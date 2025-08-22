package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.manager.GuildManager;
import de.satsuya.elysiumCore.utils.ConfigLoader;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    private final GuildManager guildManager;

    public ChatEvent() {
        this.guildManager = ElysiumCore.getGuildManager();
    }

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
                    if (guildManager.isPlayerInGuild(sender.getUniqueId())) {
                        recipient.sendMessage("["+ guildManager.getPlayerGuild(sender.getUniqueId()) +"] <" + sender.getDisplayName() + "> " + message);
                        return;
                    }
                    recipient.sendMessage("<" + sender.getDisplayName() + "> " + message);
                }
            }
        }
    }
}
