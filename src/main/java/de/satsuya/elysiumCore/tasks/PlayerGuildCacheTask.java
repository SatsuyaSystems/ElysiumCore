package de.satsuya.elysiumCore.tasks;

import de.satsuya.elysiumCore.interfaces.TaskSchedule;
import de.satsuya.elysiumCore.manager.GuildManager;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import de.satsuya.elysiumCore.utils.SetHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aktualisiert regelmäßig die Zuordnung Spieler-UUID -> Gildenname.
 * Liest dazu die Gilde der aktuell online befindlichen Spieler aus der Datenbank.
 * Falls du ALLE Spieler (auch offline) abbilden willst, ersetze die Quelle der UUIDs entsprechend deiner Datenhaltung.
 */
@TaskSchedule(delay = 0L, period = 20L * 60L * 5L, async = true) // alle 60 Sekunden
public class PlayerGuildCacheTask extends BukkitRunnable {

    private final GuildManager guildManager = ManagerRegistry.get("guild");

    @Override
    public void run() {
        try {
            // Einmalige DB-Query: alle Gilden lesen, Map UUID -> Gilde bauen
            Map<UUID, String> allMappings = guildManager.getAllPlayerGuildMappings();

            // Atomar aktualisieren
            SetHolder.playerGuildMap.clear();
            SetHolder.playerGuildMap.putAll(allMappings);

            ElysiumLogger.log("PlayerGuildCacheTask: " + allMappings.size() + " Spieler-Gilden-Zuordnungen aktualisiert.");
        } catch (Exception ex) {
            ElysiumLogger.log("PlayerGuildCacheTask Fehler: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
