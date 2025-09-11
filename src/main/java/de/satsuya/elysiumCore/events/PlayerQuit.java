package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.manager.InventoryManager;
import de.satsuya.elysiumCore.manager.NametagService;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import de.satsuya.elysiumCore.utils.SetHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerQuit implements Listener {

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
