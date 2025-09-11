package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.manager.EcoManager;
import de.satsuya.elysiumCore.manager.MongoDBManager;
import de.satsuya.elysiumCore.manager.NametagService;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import de.satsuya.elysiumCore.utils.SetHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {

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
                MongoDBManager mongodb = ManagerRegistry.get("mongodb");
                EcoManager ecoManager = ManagerRegistry.get("eco");
                // We use the static getter to get our MongoDBManager.
                mongodb.loadInventory(event.getPlayer());
                if (!ecoManager.isPlayerInDatabase(event.getPlayer())) {
                    ecoManager.initPlayer(event.getPlayer());
                }
            }
        }.runTaskAsynchronously(ElysiumCore.getInstance());
    }
}