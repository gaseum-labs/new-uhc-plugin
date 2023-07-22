package org.gaseumlabs.uhcplugin

import org.bukkit.World
import org.bukkit.entity.Player

object Lobby {
	val WORLD_INFO = WorldManager.WorldInfo("world_lobby", World.Environment.NORMAL, true)

	fun init() {
		WorldManager.get(WORLD_INFO)
	}

	fun tpToLobby(player: Player) {
		val lobbyWorld = WorldManager.get(WORLD_INFO)
		player.teleport(lobbyWorld.spawnLocation)
	}
}