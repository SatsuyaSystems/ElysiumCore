package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.utils.ConfigLoader;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {

    private final LuckPerms luckPerms;

    public ChatEvent() {
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        // Cancel normal chat sending
        event.setCancelled(true);

        // Get the sender's user data from LuckPerms
        User user = luckPerms.getUserManager().getUser(sender.getUniqueId());

        String prefix = "";
        String suffix = "";

        if (user != null) {
            QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(sender);
            prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
            suffix = user.getCachedData().getMetaData(queryOptions).getSuffix();
        }

        // Build the formatted message with prefix, display name, and suffix
        StringBuilder chatMessage = new StringBuilder();

        // Apply prefix with color codes
        if (prefix != null) {
            chatMessage.append(ChatColor.translateAlternateColorCodes('&', prefix));
        }

        // Append the player's display name
        chatMessage.append(sender.getDisplayName());

        // Apply suffix with color codes
        if (suffix != null) {
            chatMessage.append(ChatColor.translateAlternateColorCodes('&', suffix));
        }

        // Append the message itself, adding a space and the message
        chatMessage.append(ChatColor.WHITE).append(": ").append(message);

        // Log the message
        ElysiumLogger.chat(chatMessage.toString());

        // Send the formatted message to recipients
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (sender.getWorld().equals(recipient.getWorld())) {
                double distance = sender.getLocation().distance(recipient.getLocation());
                if (distance <= ConfigLoader.configData.getInt("chatRadius") || recipient.equals(sender) || recipient.hasPermission("elysium.chat.spy")) {
                    recipient.sendMessage(chatMessage.toString());
                }
            }
        }
    }
}