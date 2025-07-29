package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        // Log the player's join event
        ElysiumLogger.log("[PlayerJoin] " + playerName);
        // Send a welcome message to the player
        event.getPlayer().sendMessage("Welcome to the server, " + playerName + "!");
    }
}
