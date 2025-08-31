package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.manager.NametagService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Since we are doing a database operation, we should run this asynchronously
        // to avoid freezing the main server thread.
        NametagService service = ElysiumCore.getInstance().getNametagService();
        if (service != null) {
            service.onJoin(event.getPlayer());
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                // We use the static getter to get our MongoDBManager.
                ElysiumCore.getMongoDBManager().loadInventory(event.getPlayer());
                if (!ElysiumCore.getEcoManager().isPlayerInDatabase(event.getPlayer())) {
                    ElysiumCore.getEcoManager().initPlayer(event.getPlayer());
                }
            }
        }.runTaskAsynchronously(ElysiumCore.getInstance());
    }
}