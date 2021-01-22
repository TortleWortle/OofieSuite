package wtf.duck.oofiestick

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BlockIterator


class OofieStick : JavaPlugin(), Listener, CommandExecutor {
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(this, this)
        getCommand("oofiestick").executor = this
    }

    @EventHandler
    fun onUse(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (!hasOofieStickAndPerm(event.player)) return
            val block = getTargetBlock(event.player, 100)
            if (block.type != Material.AIR) {
                block.world.strikeLightningEffect(block.location)
                block.world.getNearbyEntities(block.location, 5.0, 32.0, 5.0).forEach {
                    if (it !is Player) return
                    if (it.uniqueId == event.player.uniqueId) return
                    it.health = 0.0
                }
            }
        }
    }

    fun getTargetBlock(player: Player?, range: Int): Block {
        val iter = BlockIterator(player, range)
        var lastBlock: Block = iter.next()
        while (iter.hasNext()) {
            lastBlock = iter.next()
            if (lastBlock.type === Material.AIR) {
                continue
            }
            break
        }
        return lastBlock
    }

    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player || event.entity !is LivingEntity) return
        if (hasOofieStickAndPerm(event.damager as Player)) {
            (event.entity as LivingEntity).health = 0.0
        }
    }

    override fun onCommand(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): Boolean {
        if (sender !is Player) return false
        if (!sender.hasPermission("oofiestick.use")) return false
        val item = ItemStack(Material.STICK)
        val meta = item.itemMeta
        meta.displayName = "OofieStick"
        meta.lore = listOf("WARNING: does oofie.")
        item.itemMeta = meta
        sender.inventory.addItem(item)
        return true
    }

    fun hasOofieStickAndPerm(p: Player): Boolean {
        if (!p.hasPermission("oofiestick.use")) return false
        if (p.itemInHand.type != Material.STICK) return false
        if (p.itemInHand.itemMeta?.displayName != "OofieStick") return false
        return true
    }
}