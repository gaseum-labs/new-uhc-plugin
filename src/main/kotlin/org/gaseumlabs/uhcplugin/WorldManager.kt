package org.gaseumlabs.uhcplugin

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.World.Environment
import org.bukkit.WorldCreator
import java.io.File
import kotlin.random.Random

object WorldManager {
	data class WorldInfo(val name: String, val environment: Environment, val structures: Boolean) {
		fun matches(world: World) = world.name == name
	}

	val OVERWORLD = WorldInfo("world", Environment.NORMAL, false)
	val NETHER = WorldInfo("world_nether", Environment.NETHER, true)

	private var worldCache = HashMap<String, World>()

	fun init() {
		get(OVERWORLD)
		get(NETHER)
	}

	fun worldsInfo(world: World): WorldInfo? {
		if (world.name == "world") return OVERWORLD
		if (world.name == "world_nether") return NETHER
		return null
	}

	fun get(info: WorldInfo, seed: Long? = null): World {
		return worldCache.getOrPut(info.name) {
			Bukkit.getServer().createWorld(
				WorldCreator(info.name)
					.seed(seed ?: Random.nextLong())
					.environment(info.environment)
					.generateStructures(info.structures)
			) ?: throw Exception("World \"${info.name}\" could not be created")
		}
	}

	fun getOrNull(info: WorldInfo): World? {
		val cached = worldCache[info.name]?.let { return it }

		val loaded = Bukkit.getServer().getWorld(info.name) ?: return null

		worldCache[info.name] = loaded
		return loaded
	}

	fun destroy(info: WorldInfo) {
		val existing = getOrNull(info)
		if (existing != null) Bukkit.getServer().unloadWorld(existing, false)
		deleteWorldFolder(info.name)
		worldCache.remove(info.name)
	}

	fun refresh(info: WorldInfo, seed: Long? = null): World {
		destroy(info)
		return get(info, seed)
	}

	private fun deleteWorldFolder(name: String) {
		val file = File(name)
		if (file.exists() && file.isDirectory) file.deleteRecursively()
	}

	fun g() {

	}
}
