package org.oreo.nodesAutobalance

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import phonon.nodes.Nodes
import kotlin.random.Random

class NodesAutobalance : JavaPlugin(), Listener {

    val town1Name = config.getString("town1")
    val town2Name = config.getString("town2")

    private var nodesInstance : Nodes? = null

    override fun onEnable() {

        Bukkit.getServicesManager().register(Nodes::class.java, Nodes, this, ServicePriority.Normal)

        // Retrieve the shared instance
        nodesInstance = Bukkit.getServicesManager().load(Nodes::class.java)
        if (nodesInstance == null) {
            // Handle error: service not registered
            logger.severe("Nodes not detected!")
        }

        server.pluginManager.registerEvents(this,this)
        saveDefaultConfig()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun joinAutobalance(e:PlayerJoinEvent){
        val player = e.player
        val resident = Nodes.getResident(player)

        if (resident != null) {
            resident.town?.let {
                    Nodes.removeResidentFromTown(it, resident)
            }
        }

        val town1 = town1Name?.let { nodesInstance!!.getTownFromName(it) }
        val town2 = town2Name?.let { nodesInstance!!.getTownFromName(it) }

        if (resident == null){
            player.sendMessage("§cNo resident object detected, please leave and join again. If the issue persists contact staff or me directly -oreo <3")
            return
        }

        if (town1 == null || town2 == null){
            player.sendMessage("§cCannot add you to a town something has gone wrong -oreo <3")
            return
        }


        if (town1.playersOnline.size > town2.playersOnline.size){
            nodesInstance!!.addResidentToTown(town2,resident)
            player.sendMessage("§bYou have been autobalanced to $town2Name")
        }else if (town2.playersOnline.size > town1.playersOnline.size){
            nodesInstance!!.addResidentToTown(town1,resident)
            player.sendMessage("§bYou have been autobalanced to $town1Name")
        }else{
            if(Random.nextBoolean()){
                nodesInstance!!.addResidentToTown(town1,resident)
                player.sendMessage("§bYou have been autobalanced to $town1Name")
            }else{
                nodesInstance!!.addResidentToTown(town2,resident)
                player.sendMessage("§bYou have been autobalanced to $town2Name")
            }
        }
    }

    @EventHandler
    fun leaveAutobalance(e:PlayerQuitEvent){
        val resident = nodesInstance!!.getResident(e.player)

        if (resident != null) { //Remove the player from the town when he leaves
            resident.town?.let { nodesInstance!!.removeResidentFromTown(it, resident) }
        }
    }
}
