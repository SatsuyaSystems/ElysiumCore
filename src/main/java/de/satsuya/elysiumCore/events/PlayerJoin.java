package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Since we are doing a database operation, we should run this asynchronously
        // to avoid freezing the main server thread.
        new BukkitRunnable() {
            @Override
            public void run() {
                // We use the static getter to get our MongoDBManager.
                ElysiumCore.getMongoDBManager().loadInventory(event.getPlayer());
            }
        }.runTaskAsynchronously(ElysiumCore.getInstance());
    }
}