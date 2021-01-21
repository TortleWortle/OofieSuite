package wtf.duck.oofiewater

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

var WorldGuardEnabled = false


class OofieWater : JavaPlugin(), Listener, CommandExecutor {
    val oofiedList = ArrayList<UUID>()
    var shouldBroadcast = true

    override fun onLoad() {
        super.onLoad()
        WorldGuardEnabled = server.pluginManager.getPlugin("WorldGuard") != null
        if (WorldGuardEnabled) {
            registerWorldGuardFlag(this)
        }
    }

    override fun onEnable() {
        saveDefaultConfig()
        shouldBroadcast = config.getBoolean("broadcastDeaths")
        server.pluginManager.registerEvents(this, this)
        getCommand("oofiewater").executor = this
        if (config.getBoolean("limitBroadcasts")) {
            server.scheduler.runTaskTimerAsynchronously(
                this,
                Runnable {
                    oofiedList.clear()
                },
                20 * config.getInt("broadcastTimeout").toLong(),
                20 * 60 * config.getInt("broadcastTimeout").toLong()
            )
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.to.blockX == event.from.blockX
            && event.to.blockY == event.from.blockY
            && event.to.blockZ == event.from.blockZ
        ) return
        if (event.player.gameMode == GameMode.CREATIVE) return
        if (event.player.gameMode == GameMode.SPECTATOR) return
        if (event.player.isInsideVehicle) return

        val hasperm = event.player.hasPermission("oofiewater.scuba")
        if (event.player.location.block.type == Material.WATER || event.player.location.block.type == Material.STATIONARY_WATER) {
            if (WorldGuardEnabled) {
                val set = WorldGuardPlugin.inst().getRegionManager(event.player.world)
                    .getApplicableRegions(event.player.location)
                val localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.player)
                if (!set.testState(localPlayer, OofieWaterFlag)) {
                    return // return if oofiewater flag is deny
                }
            }
            if (hasperm) {
                val nightVision = PotionEffect(PotionEffectType.NIGHT_VISION, 400, 1, false, false)
                val waterBreathing = PotionEffect(PotionEffectType.WATER_BREATHING, 400, 1, false, false)
                event.player.addPotionEffect(nightVision, true)
                event.player.addPotionEffect(waterBreathing, true)
                return
            }
            event.player.health = 0.0
            sendDeathMessage(event)
        } else {
            if (hasperm) {
                event.player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                event.player.removePotionEffect(PotionEffectType.WATER_BREATHING)
            }
        }
    }

    private fun sendDeathMessage(event: PlayerMoveEvent) {
//        val format = when (count) {
//            0 -> "%s realised water is oofie."
//            1 -> "%s, I'm serious, it's oofie."
//            2 -> "You have to believe me at some point, %s. It's oofie"
//            3 -> "I don't think %s is listening."
//            4 -> "Very oofie indeed, %s."
//            5 -> "%s has not gotten the hint yet.."
//            6 -> "For the last time %s, THE..WATER..IS..OOFIE!"
//            7 -> "I give up, %s."
//            8 -> "%s still hasn't realised water is oofie."
//            9 -> "The water kills you, %s."
//            else -> null
//        }

        var message = PlaceholderAPI.setPlaceholders(event.player, config.getString("messages.died"))
        if (event.player.killer != null && event.player.killer is Player) {
            message = PlaceholderAPI.setPlaceholders(event.player, config.getString("messages.killed"))
            message = PlaceholderAPI.setPlaceholders(event.player.killer, message.replace("%player_killer", "%player"))
        }

        if (shouldBroadcast) {
            if (config.getBoolean("limitBroadcasts") && oofiedList.contains(event.player.uniqueId)) {
                event.player.sendMessage(message)
                oofiedList.add(event.player.uniqueId)
            } else {
                Bukkit.broadcastMessage(message)
            }
        } else {
            event.player.sendMessage(message)
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        if (event.entity.hasPermission("oofiewater.scuba") && event.cause == EntityDamageEvent.DamageCause.DROWNING) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (event.entity.location.block.type == Material.WATER || event.entity.location.block.type == Material.STATIONARY_WATER) {
            event.deathMessage = ""
        }
    }

    override fun onCommand(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): Boolean {
        if (sender !is Player) return true
        if (!sender.hasPermission("oofiewater.manage")) return true
        if (args == null || args.isEmpty()) {
            sender.sendMessage(
                when (shouldBroadcast) {
                    true -> "OofieWater is broadcasting death messages."
                    false -> "OofieWater is not broadcasting death messages."
                }
            )
            return true
        }
        if (args.first() == "info") {
            sender.sendMessage(
                when (shouldBroadcast) {
                    true -> "OofieWater is broadcasting death messages."
                    false -> "OofieWater is not broadcasting death messages."
                }
            )
            return true
        }
        if (args.first() == "toggle") {
            shouldBroadcast = !shouldBroadcast
            sender.sendMessage(
                when (shouldBroadcast) {
                    true -> "OofieWater is now broadcasting death messages."
                    false -> "OofieWater is no longer broadcasting death messages."
                }
            )
            return true
        }
        sender.sendMessage("/oofiewater <toggle/info>")
        return true
    }
}