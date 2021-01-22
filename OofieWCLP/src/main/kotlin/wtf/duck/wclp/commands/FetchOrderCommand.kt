package wtf.duck.wclp.commands

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.Node
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import org.geysermc.floodgate.FloodgateAPI
import wtf.duck.wclp.WCLP
import java.util.*

class FetchOrderCommand(private val pl: WCLP) : Command("fetchorder") {
    override fun execute(p: CommandSender?, args: Array<out String>?) {
        if (p !is ProxiedPlayer) {
            p?.sendMessage("Why tf would console need to buy perms?")
            return
        }
        if(!pl.hasOrders(p)) {
            p.sendMessage("No pending orders, please allow up to a minute.")
            return
        }
        pl.applyOrders(p)
    }
}