package wtf.duck.oofiewater

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry
import org.bukkit.plugin.java.JavaPlugin

lateinit var OofieWaterFlag: StateFlag

fun registerWorldGuardFlag(pl : JavaPlugin) {
    val registry : FlagRegistry = WorldGuardPlugin.inst().flagRegistry
    try {
        val flag = StateFlag("oofiewater", true)
        registry.register(flag)
        OofieWaterFlag = flag
    } catch (e : FlagConflictException) {
        pl.logger.warning("some fuck already registered oofiewater, tell them to piss off")
        val existing = registry.get("oofiewater")
        if (existing is StateFlag) {
            OofieWaterFlag = existing
        } else {
            pl.logger.warning("flag is oofie, not kidding it's actually fucked.")
        }
    }
}