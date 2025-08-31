package de.satsuya.elysiumCore.tasks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TablistUpdater extends BukkitRunnable {

    private final LuckPerms luckPerms;

    public TablistUpdater() {
        // Get the LuckPerms API instance
        // This should be done once, preferably in the main plugin class's onEnable
        this.luckPerms = LuckPermsProvider.get();
    }

    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    @Override
    public void run() {
        // Create the header and footer
        String header = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Elysium Online";
        String footer = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + getCurrentTime();

        // Loop through all online players to update their tablist
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Set the header and footer for each player
            player.setPlayerListHeader(header);
            player.setPlayerListFooter(footer);

            // Get the user data from LuckPerms
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());

            if (user != null) {
                // Get the player's prefix and suffix from LuckPerms
                // The QueryOptions specify the context for the query (e.g., world)
                // For a simple tablist, a default context is fine
                QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);
                String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
                String suffix = user.getCachedData().getMetaData(queryOptions).getSuffix();

                // Build the new tablist name with the prefix, player name, and suffix
                StringBuilder tabNameBuilder = new StringBuilder();

                if (prefix != null) {
                    tabNameBuilder.append(ChatColor.translateAlternateColorCodes('&', prefix));
                }

                tabNameBuilder.append(player.getName());

                if (suffix != null) {
                    tabNameBuilder.append(ChatColor.translateAlternateColorCodes('&', suffix));
                }

                // Set the player's custom name in the tablist
                player.setPlayerListName(tabNameBuilder.toString());
            }
        }
    }
}