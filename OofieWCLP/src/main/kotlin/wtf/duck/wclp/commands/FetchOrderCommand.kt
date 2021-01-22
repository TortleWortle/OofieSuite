package wtf.duck.wclp.commands

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import wtf.duck.wclp.WCLP

class FetchOrderCommand(private val pl: WCLP) : Command("fetchorder") {
    override fun execute(p: CommandSender?, args: Array<out String>?) {
        if (p !is ProxiedPlayer) {
            p?.sendMessage(TextComponent("Why tf would console need to buy perms?"))
            return
        }
        if (!pl.hasOrders(p)) {
            p.sendMessage(TextComponent("No pending orders, please allow up to a minute."))
            return
        }
        pl.applyOrders(p)
    }
}