package de.satsuya.elysiumCore.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TablistUpdater extends BukkitRunnable {

    private String getCurrentTime() {
        // Ruft das aktuelle Datum und die Uhrzeit vom System ab
        LocalDateTime now = LocalDateTime.now();

        // Definiert das Format, in dem die Uhrzeit ausgegeben werden soll (HH:mm:ss)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Formatiert das Datum/die Uhrzeit in den gewünschten String
        return now.format(formatter);
    }

    @Override
    public void run() {
        // This method will be called periodically to update the tablist
        // You can implement your logic here to update the tablist for players
        // For example, you might want to update player names, ping, or other information
        // Die TPS-Werte vom Server holen

        // Die Werte formatieren (nur auf zwei Nachkommastellen)

        // Den Header und Footer erstellen
        String header = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Elysium Online";
        String footer = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + getCurrentTime();


        // Header und Footer für alle online Spieler setzen
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeader(header);
            player.setPlayerListFooter(footer);
        }
    }
}
