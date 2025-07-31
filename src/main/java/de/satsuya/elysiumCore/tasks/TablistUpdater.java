package de.satsuya.elysiumCore.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TablistUpdater extends BukkitRunnable {
    @Override
    public void run() {
        // This method will be called periodically to update the tablist
        // You can implement your logic here to update the tablist for players
        // For example, you might want to update player names, ping, or other information
        // Die TPS-Werte vom Server holen

        // Die Werte formatieren (nur auf zwei Nachkommastellen)

        // Den Header und Footer erstellen
        String header = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Elysium Online";


        // Header und Footer für alle online Spieler setzen
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeader(header);
        }
    }
}
