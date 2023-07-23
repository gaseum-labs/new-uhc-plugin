package org.gaseumlabs.uhcplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.structure.Structure
import java.util.concurrent.CompletableFuture

object WorldAnalyzer {
	data class Count(var count: Int)

	abstract class Analysis {
		abstract fun validate(): String?
		abstract fun generateReport(): List<Component>
	}

	data class BiomeAnalysis(
		val fullBiomes: List<Pair<Biome, Int>>,
		val centerbiomes: List<Pair<Biome, Int>>,
	) : Analysis() {
		val fullSamples: Int = fullBiomes.sumOf { (_, count) -> count }
		val centerSamples: Int  = fullBiomes.sumOf { (_, count) -> count }

		override fun validate(): String? {
			val centerOceansRatio = fullBiomes.sumOf { (biome, count) -> if (oceans.contains(biome)) count else 0 }.toFloat() / fullSamples
			if (centerOceansRatio > 0.1f) return "Oceans are ${"%.2f".format(centerOceansRatio * 100.0f)}% of the final radius, more than 10%"

			val totalOceansRatio = fullBiomes.sumOf { (biome, count) -> if (oceans.contains(biome)) count else 0 }.toFloat() / fullSamples
			if (totalOceansRatio > 0.1f) return "Oceans are ${"%.2f".format(totalOceansRatio * 100.0f)}% of the map, more than 10%"

			return null
		}

		override fun generateReport(): List<Component> {
			return fullBiomes.map { (biome, count) ->
				val percentage = count.toFloat() / fullSamples.toFloat()

				Component.text("${biome.name}: ", NamedTextColor.GOLD).append(
					Component.text("%.2f".format(percentage * 100.0f) + "%", NamedTextColor.WHITE)
				)
			}
		}
	}

	data class NetherAnalysis(
		val fortressLocation: Location?,
		val platformRatio: Float
	) : Analysis() {
		override fun validate(): String? {
			if (fortressLocation == null) return "Does not have fortress within bounds"

			if (platformRatio < 0.9) return  "${"%.2f".format((1.0f - platformRatio) * 100.0f)}% of the map is a lava ocean, more than 10%"
			return null
		}

		override fun generateReport(): List<Component> {
			return listOf(
				Component.text("Fortress location: ", NamedTextColor.GOLD).append(Component.text(fortressLocation.toString(), NamedTextColor.WHITE)),
				Component.text("Percentage Walkable: ", NamedTextColor.GOLD).append(Component.text("${"%.2f".format(platformRatio * 100.0f)}%", NamedTextColor.WHITE))
			)
		}
	}

	fun isOcean(biome: Biome) = biome.name.endsWith("OCEAN")

	val oceans = Biome.entries.filter(::isOcean)

	fun analyzeBiomes(world: World, outerRadius: Int, innerRadius: Int): BiomeAnalysis {
		val biomes = HashMap<Biome, Count>()
		val centerBiomes = HashMap<Biome, Count>()

		val outerRange = -outerRadius..outerRadius step 16
		for (x in outerRange) {
			for (z in outerRange) {
				biomes.getOrPut(world.getBiome(x, world.seaLevel, z)) { Count(0) }.count += 1
			}
		}

		val innerRange = -innerRadius..outerRadius step 4
		for (x in innerRange) {
			for (z in outerRange) {
				centerBiomes.getOrPut(world.getBiome(x, world.seaLevel, z)) { Count(0) }.count += 1
			}
		}

		return BiomeAnalysis(
			biomes.map { (biome, count) -> biome to count.count }.sortedByDescending { (_, count) -> count },
			centerBiomes.map { (biome, count) -> biome to count.count }.sortedByDescending { (_, count) -> count },
		)
	}

	fun analyzeNether(world: World, outerRadius: Int): CompletableFuture<NetherAnalysis> {
		val fortressLocation = world.locateNearestStructure(Location(world, 0.0, 60.0, 0.0), Structure.FORTRESS, outerRadius / 2, false)?.location

		var goodSamples = 0

		val chunkFutures = ArrayList<CompletableFuture<Chunk>>()

		fun hasPlatform(chunk: Chunk): Boolean {
			var lastBlock = chunk.getBlock(7, 100, 7)

			for (y in 99 downTo  31) {
				val currentBlock = chunk.getBlock(7, y, 7)
				if (lastBlock.isPassable && !currentBlock.isPassable) return true

				lastBlock = currentBlock
			}

			return false
		}

		val outerRange = -outerRadius..outerRadius step 16
		for (x in outerRange) {
			for (z in outerRange) {
				chunkFutures.add(world.getChunkAtAsync(world.getBlockAt(x, 0, z)))
			}
		}

		return CompletableFuture.allOf(*chunkFutures.toTypedArray()).thenApply {
			chunkFutures.forEach { chunkFuture ->
				if (hasPlatform(chunkFuture.get())) ++goodSamples
			}

			return@thenApply NetherAnalysis(fortressLocation, goodSamples.toFloat() / chunkFutures.size.toFloat())
		}
	}
}