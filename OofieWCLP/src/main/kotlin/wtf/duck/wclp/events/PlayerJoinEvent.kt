package wtf.duck.wclp.events

import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import wtf.duck.wclp.WCLP


class PlayerJoinEvent(val pl : WCLP) : Listener {
    @EventHandler
    fun onPostLogin(event: PostLoginEvent?) {
        if (event == null || event.player == null) {
            return
        }
        if (pl.hasOrders(event.player)) {
            pl.applyOrders(event.player)
        }
    }
}