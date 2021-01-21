package wtf.duck.oofiepvp

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.plugin.java.JavaPlugin

class OofiePVP : JavaPlugin(), Listener {
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onHit(event : EntityDamageByEntityEvent) {
        if (event.damager !is Player || event.entity !is Player) return
        if (!event.damager.hasPermission("oofiepvp.ponch")) event.isCancelled = true
    }
}