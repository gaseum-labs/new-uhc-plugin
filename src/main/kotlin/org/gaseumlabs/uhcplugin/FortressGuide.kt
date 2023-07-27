package org.gaseumlabs.uhcplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.generator.structure.Structure
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Team
import java.util.UUID
import kotlin.math.floor
import kotlin.math.sqrt

class FortressGuide(var world: World, var entity: Item) {
	fun update(player: Player, fortressLocation: Location) {
		val fortressAtHeight = fortressLocation.clone()
		val distance = fastDist(fortressAtHeight, player.location)
		fortressAtHeight.y = player.location.y
		val vectorToFortress = fortressAtHeight.subtract(player.location).toVector().normalize().multiply(16.0)

		entity.ticksLived = 1
		entity.customName(Component.text("Fortress - ${floor(distance).toInt()} blocks", NamedTextColor.RED))
		entity.teleport(player.location.add(0.0, 1.0, 0.0).add(vectorToFortress))
	}

	companion object {
		val guides = HashMap<UUID, FortressGuide>()
		val fortressLocations = HashMap<World, Location?>()

		var blockTeam: Team? = null
		const val BLOCK_TEAM_NAME = "fortress_guide_color"

		fun fastDist(location0: Location, location1: Location): Double {
			return sqrt(
				(location0.x - location1.x) * (location0.x - location1.x) +
				(location0.z - location1.z) * (location0.z - location1.z)
			)
		}

		fun tick() {
			val scoreBoard = Bukkit.getServer().scoreboardManager.mainScoreboard
			val team = if (blockTeam != null) blockTeam as Team else {

				val team = scoreBoard.getTeam(BLOCK_TEAM_NAME) ?: scoreBoard.registerNewTeam(BLOCK_TEAM_NAME)

				team.color(NamedTextColor.RED)

				team
			}

			val server = Bukkit.getServer()

			server.onlinePlayers.forEach { player ->
				if (player.world.environment === World.Environment.NETHER) {
					val world = player.world

					val fortressLocation = fortressLocations.getOrPut(world) {
						world.locateNearestStructure(Location(world, 0.0, 0.0, 0.0), Structure.FORTRESS, 500, false)?.location
					} ?: return@forEach

					fun createGuide(): FortressGuide {
						val entity = world.dropItem(player.location, ItemStack(Material.BLAZE_POWDER))

						entity.setCanMobPickup(false)
						entity.setCanPlayerPickup(false)
						entity.isGlowing = true
						entity.setGravity(false)
						entity.isPersistent = true
						entity.isCustomNameVisible = true

						team.addEntity(entity)
						return FortressGuide(world, entity)
					}

					var guide = guides.getOrPut(player.uniqueId) { createGuide() }
					if (guide.world !== world) {
						guide = createGuide()
						guides[player.uniqueId] = guide
					}

					guide.update(player, fortressLocation)
				}
			}

			guides.entries.removeIf { (uuid, guide) ->
				if (Bukkit.getPlayer(uuid) == null) {
					team.removeEntities(guide.entity)
					guide.entity.remove()
					true
				} else {
					false
				}
			}
		}
	}
}