package org.gaseumlabs.uhcplugin

import co.aikar.commands.PaperCommandManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
	override fun onEnable() {
		server.pluginManager.registerEvents(EventListener(), this)

		val commandManager = PaperCommandManager(this)

		commandManager.registerCommand(WorldCommands())

		Bukkit.getScheduler().scheduleSyncDelayedTask(this) {
			onFirstTick()
		}
	}

	private fun onFirstTick() {
		WorldManager.init()
		Lobby.init()
	}
}
