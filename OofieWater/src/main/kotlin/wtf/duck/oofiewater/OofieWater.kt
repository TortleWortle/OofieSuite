package wtf.duck.oofiewater

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry
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
import kotlin.collections.HashMap

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
        server.pluginManager.registerEvents(this, this)
        getCommand("oofiewater").executor = this
        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            oofiedList.clear()
        }, 20 * 60 * 1, 20 * 60 * 1) // clear every 10 minutes.
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

        // TODO: only unique message to player
        // broadcast generic message
        // only broadcast once per player per minute (reset list every minute)
        val hasperm = event.player.hasPermission("oofiewater.scuba")
        if (event.player.location.block.type == Material.WATER || event.player.location.block.type == Material.STATIONARY_WATER) {
            if (WorldGuardEnabled) {
                val set = WorldGuardPlugin.inst().getRegionManager(event.player.world).getApplicableRegions(event.player.location)
                val localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.player)
                if(!set.testState(localPlayer, OofieWaterFlag)) {
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
//        val count = c[event.player.uniqueId] ?: 0
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
//        c[event.player.uniqueId] = count + 1
//        if (format != null) {
//            if (shouldBroadcast)
//                Bukkit.broadcastMessage(String.format(format, event.player.name))
//            else
//                event.player.sendMessage(String.format(format, event.player.name))
//        } else {
//            event.player.sendMessage(String.format("The water kills you, %s.", event.player.name))
//        }
        var message = String.format("%s realised the water is oofie.", event.player.displayName)
        if (event.player.killer != null && event.player.killer is Player) {
            message = String.format("%s got oofied by %s", event.player.displayName, event.player.killer.displayName)
        }

        if (shouldBroadcast) {
            if (oofiedList.contains(event.player.uniqueId)) {
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