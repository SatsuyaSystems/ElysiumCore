package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.manager.EcoManager;
import de.satsuya.elysiumCore.manager.InventoryManager;
import de.satsuya.elysiumCore.manager.NametagService;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import de.satsuya.elysiumCore.utils.SetHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinQuit implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Since we are doing a database operation, we should run this asynchronously
        // to avoid freezing the main server thread.
        NametagService service = ManagerRegistry.get("nametag");
        if (service != null) {
            service.onJoin(event.getPlayer());
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (SetHolder.vanishedPlayers.contains(onlinePlayer.getUniqueId()) && !event.getPlayer().hasPermission("elysiumcore.vanish.see")) {
                event.getPlayer().hidePlayer(ElysiumCore.getInstance(), onlinePlayer);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                InventoryManager inventoryManager = ManagerRegistry.get("inventory");
                EcoManager ecoManager = ManagerRegistry.get("eco");
                // We use the static getter to get our MongoDBManager.
                inventoryManager.loadInventory(event.getPlayer());
                if (!ecoManager.isPlayerInDatabase(event.getPlayer())) {
                    ecoManager.initPlayer(event.getPlayer());
                }
            }
        }.runTaskAsynchronously(ElysiumCore.getInstance());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        NametagService service = ManagerRegistry.get("nametag");
        if (service != null) {
            service.onQuit(event.getPlayer());
        }
        if (SetHolder.vanishedPlayers.contains(event.getPlayer().getUniqueId())) SetHolder.vanishedPlayers.remove(event.getPlayer().getUniqueId());
        // Database operations should be run on a separate thread to avoid lag.
        new BukkitRunnable() {
            @Override
            public void run() {
                InventoryManager inventoryManager = ManagerRegistry.get("inventory");
                // Get the MongoDBManager using the static getter.
                inventoryManager.saveInventory(event.getPlayer());
            }
        }.runTaskAsynchronously(ElysiumCore.getInstance());
    }
}