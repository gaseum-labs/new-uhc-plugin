package org.gaseumlabs.uhcplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.Biome

object WorldAnalyzer {
	data class Count(var count: Int)

	data class BiomeAnalysis(
		val fullBiomes: List<Pair<Biome, Int>>,
		val centerbiomes: List<Pair<Biome, Int>>,
	) {
		val fullSamples: Int = fullBiomes.sumOf { (_, count) -> count }
		val centerSamples: Int  = fullBiomes.sumOf { (_, count) -> count }

		fun validate(): String? {
			val centerOceansRatio = fullBiomes.sumOf { (biome, count) -> if (oceans.contains(biome)) count else 0 }.toFloat() / fullSamples
			if (centerOceansRatio > 0.1f) return "Oceans are ${"%.2f".format(centerOceansRatio * 100.0f)}% of the final radius, more than 10%"

			val totalOceansRatio = fullBiomes.sumOf { (biome, count) -> if (oceans.contains(biome)) count else 0 }.toFloat() / fullSamples
			if (totalOceansRatio > 0.1f) return "Oceans are ${"%.2f".format(totalOceansRatio * 100.0f)}% of the map, more than 10%"

			return null
		}

		fun generateReport(): List<Component> {
			return fullBiomes.map { (biome, count) ->
				val percentage = count.toFloat() / fullSamples.toFloat()

				Component.text("${biome.name}: ", NamedTextColor.GOLD).append(
					Component.text("%.2f".format(percentage * 100.0f) + "%", NamedTextColor.WHITE)
				)
			}
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
}