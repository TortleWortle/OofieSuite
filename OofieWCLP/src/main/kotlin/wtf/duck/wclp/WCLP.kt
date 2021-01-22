package wtf.duck.wclp

import com.google.common.io.ByteStreams
import khttp.get
import khttp.post
import khttp.structures.authorization.BasicAuthorization
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.Node
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import org.geysermc.floodgate.FloodgateAPI
import wtf.duck.wclp.commands.FetchOrderCommand
import wtf.duck.wclp.events.PlayerJoinEvent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class WCLP : Plugin() {
    private var task: ScheduledTask? = null
    private lateinit var config: Config
    private var bedrockOrders = HashMap<String, ArrayList<Order>>()
    private var javaOrders = HashMap<String, ArrayList<Order>>()

    private fun fetchOrders() {
        val r = get(
            config.shopURL + "orders?status=processing&per_page=50",
            auth = BasicAuthorization(
                config.shopUser,
                config.shopSecret
            )
        )

        val bOrders = HashMap<String, ArrayList<Order>>()
        val jOrders = HashMap<String, ArrayList<Order>>()

        if (r.statusCode != 200) {
            throw error("fetching orders: status not 200")
        }

        val orders = r.jsonArray
        for (i in 0 until orders.length()) {
            val order = orders.getJSONObject(i)
            val id = order.getInt("id")
            var isBedrock = false
            var username: String? = null
            val productIDS = ArrayList<Int>()

            val meta = order.getJSONArray("meta_data")
            for (j in 0 until meta.length()) {
                val m = meta.getJSONObject(j)
                val key = m.getString("key")
                if (key == "account_type") {
                    isBedrock = m.getString("value") == "bedrock"
                } else if (key == "username") {
                    username = m.getString("value")
                }
            }

            if (username == null) {
                logger.warning(String.format("No username in order: %d", id))
                continue
            }

            val products = order.getJSONArray("line_items")
            for (j in 0 until products.length()) {
                val product = products.getJSONObject(j)
                val productID = product.getInt("product_id")
                if (config.productIDS.contains(productID)) {
                    productIDS.add(productID)
                }
            }

            if (isBedrock) {
                if (bOrders[username.toLowerCase()] == null) {
                    bOrders[username.toLowerCase()] = ArrayList()
                }
                bOrders[username.toLowerCase()]!!.add(Order(id, isBedrock, username, productIDS))
            } else {
                if (jOrders[username.toLowerCase()] == null) {
                    jOrders[username.toLowerCase()] = ArrayList()
                }
                jOrders[username.toLowerCase()]!!.add(Order(id, isBedrock, username, productIDS))
            }
        }

        bedrockOrders = bOrders
        javaOrders = jOrders

        logger.info(
            String.format(
                "Fetched %d orders.",
                bOrders.mapNotNull { it.value.size }.sum() + jOrders.mapNotNull { it.value.size }.sum()
            )
        )
        if (config.applyAutomatically) {
            proxy.players.forEach {
                if(hasOrders(it)) {
                    applyOrders(it)
                }
            }
        }
    }


    override fun onEnable() {
        super.onEnable()
        saveDefaultConfig()
        loadConfig()

        task = proxy.scheduler.schedule(this, {
            logger.info("Fetching orders")
            fetchOrders()
        }, 1, 30, TimeUnit.SECONDS)

        if (config.enableCommand) {
        proxy.pluginManager.registerCommand(this, FetchOrderCommand(this))
        }
        if (config.applyOnJoin) {
            proxy.pluginManager.registerListener(this, PlayerJoinEvent(this))
        }
    }

    override fun onDisable() {
        super.onDisable()
        if (task != null) {
            proxy.scheduler.cancel(task)
        }
    }

    fun hasOrders(p: ProxiedPlayer): Boolean {
        val isBedrock = FloodgateAPI.isBedrockPlayer(p)

        val username =
            (if (isBedrock) FloodgateAPI.getPlayerByConnection(p.pendingConnection).username else p.name).toLowerCase()

        val orders = if (isBedrock) bedrockOrders[username] else javaOrders[username]
        return !(orders == null || orders.size == 0)
    }

    fun applyOrders(p: ProxiedPlayer) {
        val isBedrock = FloodgateAPI.isBedrockPlayer(p)

        val username =
            (if (isBedrock) FloodgateAPI.getPlayerByConnection(p.pendingConnection).username else p.name).toLowerCase()

        val orders = if (isBedrock) bedrockOrders[username] else javaOrders[username]
        if (orders == null || orders.size == 0) {
            return
        }

        p.sendMessage(TextComponent(String.format("%d Order(s) found.", orders.size)))

        val uuid: UUID = p.uniqueId

        orders.forEach { order ->
            val perms = order.productIDS.mapNotNull {
                config.productPerm[it]
            }

            givePermissions(uuid, perms)
            p.sendMessage(TextComponent(String.format("Order(%d): Processed.", order.id)))
            if (isBedrock) bedrockOrders.remove(username) else javaOrders.remove(username)
            proxy.scheduler.runAsync(this) {
                completeOrder(order.id)
                p.sendMessage(TextComponent(String.format("Order(%d) marked complete.", order.id)))
            }
        }
    }

    private fun givePermissions(id: UUID, perms: List<String>) {
        val lpapi = LuckPermsProvider.get()
        val lpuser = lpapi.userManager.getUser(id) ?: throw error("user not found")
        perms.forEach {
            lpuser.data().add(Node.builder(it).build())
        }
        lpapi.userManager.saveUser(lpuser).join()
    }

    private fun completeOrder(id: Int) {
        post(
            String.format("%sorders/%d", config.shopURL, id), auth = BasicAuthorization(
                config.shopUser,
                config.shopSecret
            ), data = mapOf("status" to "completed")
        )
    }

    private fun loadConfig() {
        val c: Configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(
            File(
                dataFolder, "config.yml"
            )
        )
        val pl = c.getStringList("products")
        val productIDS = ArrayList<Int>()
        val productPerm = HashMap<Int, String>()
        pl.forEach {
            val p = it.split(":", limit = 2)
            productIDS.add(p.first().toInt())
            productPerm[p.first().toInt()] = p.last()
        }
        config = Config(
            c.getString("SHOP_API_URL"),
            c.getString("SHOP_API_USER"),
            c.getString("SHOP_API_SECRET"),
            productIDS,
            productPerm,
            c.getBoolean("enableCommand"),
            c.getBoolean("applyOnJoin"),
            c.getBoolean("applyAutomatically"),
        )
    }

    private fun saveDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }

        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
            try {
                configFile.createNewFile()
                getResourceAsStream("config.yml").use { inputStream ->
                    FileOutputStream(configFile).use { os ->
                        @Suppress("UnstableApiUsage")
                        ByteStreams.copy(
                            inputStream,
                            os
                        )
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Unable to create configuration file", e)
            }
        }
    }

    data class Config(
        val shopURL: String,
        val shopUser: String,
        val shopSecret: String,
        val productIDS: List<Int>,
        val productPerm: Map<Int, String>,
        val enableCommand: Boolean,
        val applyOnJoin: Boolean,
        val applyAutomatically: Boolean,
    )
}