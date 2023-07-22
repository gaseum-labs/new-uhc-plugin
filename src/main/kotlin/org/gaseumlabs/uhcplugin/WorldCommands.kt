package org.gaseumlabs.uhcplugin

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WorldCommands : BaseCommand() {
	@CommandAlias("refreshworld")
	@Description("refreshes a world")
	fun refreshCommand(sender: CommandSender, world: World) {
		val info = WorldManager.worldsInfo(world) ?: return sender.sendMessage("I don't know that world")
		val newWorld = WorldManager.refresh(info)
		sender.sendMessage("Refreshed world \"${info.name}\" with seed ${newWorld.seed}")
	}

	@CommandAlias("biomeanalysis")
	fun biomeAnalysisCommand(sender: CommandSender) {
		val analysis = WorldAnalyzer.analyzeBiomes(WorldManager.get(WorldManager.OVERWORLD), 50, 500)

		analysis.generateReport().forEach { sender.sendMessage(it) }
	}

	@CommandAlias("tplobby")
	fun tpLobbyCommand(sender: CommandSender) {
		if (sender !is Player) return sender.sendMessage("You need to be a player")

		Lobby.tpToLobby(sender)
	}

	@CommandAlias("findseed")
	fun findSeedCommand(sender: CommandSender) {
		val testWorld = WorldManager.refresh(Lobby.WORLD_INFO)

		val analysis = WorldAnalyzer.analyzeBiomes(testWorld, 50, 500)

		val seed = testWorld.seed
		val warnings = analysis.validate()

		sender.sendMessage(if (warnings == null)
				Component.text("Seed $seed is valid", NamedTextColor.GREEN)
			else
				Component.text("Seed $seed is invalid: $warnings", NamedTextColor.RED)
		)
	}

	@CommandAlias("gotoseed")
	fun goToSeed(sender: CommandSender, seed: Long) {
		if (sender !is Player) return sender.sendMessage("You need to be a player")

		WorldManager.refresh(Lobby.WORLD_INFO, seed)

		Lobby.tpToLobby(sender)
	}
}
