package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.utils.ConfigLoader;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        // Cancle normal chat sending
        event.setCancelled(true);

        ElysiumLogger.chat(sender.getName() + ": " + message);

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (sender.getWorld().equals(recipient.getWorld())) {
                double distance = sender.getLocation().distance(recipient.getLocation());
                if (distance <= ConfigLoader.configData.getInt("chatRadius") || recipient.equals(sender)) {
                    recipient.sendMessage("<" + sender.getDisplayName() + "> " + message);
                }
            }
        }
    }
}
