package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Database operations should be run on a separate thread to avoid lag.
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get the MongoDBManager using the static getter.
                ElysiumCore.getMongoDBManager().saveInventory(event.getPlayer());
            }
        }.runTaskAsynchronously(ElysiumCore.getInstance());
    }
}
