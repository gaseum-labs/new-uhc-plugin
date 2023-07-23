package org.gaseumlabs.uhcplugin

import org.bukkit.World

object Game {
	val WORLD_GAME = WorldManager.WorldInfo("world_game", World.Environment.NORMAL, false)
	val WORLD_GAME_NETHER = WorldManager.WorldInfo("world_game_nether", World.Environment.NETHER, true)
}
