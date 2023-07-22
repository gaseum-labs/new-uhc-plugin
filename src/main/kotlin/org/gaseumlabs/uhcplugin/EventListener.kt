package org.gaseumlabs.uhcplugin

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class EventListener : Listener {
	@EventHandler
	fun onTeleport(event: PlayerTeleportEvent) {
		if (event.cause !== PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return

		if (
			WorldManager.NETHER.matches(event.from.world) &&
			WorldManager.OVERWORLD.matches(event.to.world)
		) {
			event.to.world = WorldManager.get(Lobby.WORLD_INFO)
		}
	}
}