package de.satsuya.elysiumCore;

import de.satsuya.elysiumCore.utils.ConfigLoader;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ElysiumCore extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ElysiumLogger.log("ElysiumCore is starting up...");
        try {
            // Load configuration
            de.satsuya.elysiumCore.utils.ConfigLoader.setupConfig();
            de.satsuya.elysiumCore.utils.ConfigLoader.loadConfig();
            ElysiumLogger.log("Configuration loaded successfully.");
            if (ConfigLoader.configData.getBoolean("debug")) {
                ElysiumLogger.debug("Debug mode is enabled.");
            }
            // Load events
            de.satsuya.elysiumCore.utils.EventLoader.loadEvents(this);
            ElysiumLogger.log("Events loaded successfully.");
            // Load commands
            de.satsuya.elysiumCore.utils.CommandLoader.loadCommands(this);
            ElysiumLogger.log("Commands loaded successfully.");
            de.satsuya.elysiumCore.utils.TaskLoader.loadAndStartRunnables(this);
            ElysiumLogger.log("Tasks loaded and started successfully.");
        } catch (Exception e) {
            ElysiumLogger.error("Failed to load events: " + e.getMessage());
        }
        startGlobalSneakCheckTask();
    }

    // Speichert die BukkitTask-Instanzen für jeden Spieler, der ein Pentagramm anzeigt
    private final Map<UUID, BukkitTask> activePentagrams = new HashMap<>();

    private void startGlobalSneakCheckTask() {
        // Überprüft alle 5 Ticks (0.25 Sekunden) den Status aller Spieler
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Prüfe, ob der Spieler OP ist und schleicht
                if (player.isOp() && player.isSneaking()) {
                    // Wenn der Spieler ein Pentagramm haben sollte, aber noch keinen Task hat
                    if (!activePentagrams.containsKey(player.getUniqueId())) {
                        startPentagramTask(player);
                    }
                } else {
                    // Wenn der Spieler kein OP ist ODER nicht mehr schleicht
                    // Und er hatte zuvor ein Pentagramm aktiv
                    if (activePentagrams.containsKey(player.getUniqueId())) {
                        stopPentagramTask(player);
                    }
                }
            }
        }, 0L, 5L); // Startet sofort (0L) und wiederholt alle 5 Ticks (5L)
    }

    private void startPentagramTask(Player player) {
        // Hole die aktuelle Position des Spielers
        Location center = player.getLocation().clone();
        // Startet einen neuen Task für diesen Spieler, der die Partikel spawnt
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            spawnPentagramParticles(center, player);
        }, 0L, 5L); // Startet sofort und wiederholt alle 5 Ticks (Partikel-Update-Frequenz)
        activePentagrams.put(player.getUniqueId(), task);
        ElysiumLogger.log(player.getName() + " hat ein Pentagramm begonnen!");
    }

    private void stopPentagramTask(Player player) {
        BukkitTask task = activePentagrams.remove(player.getUniqueId());
        if (task != null) {
            task.cancel(); // Stoppe den spezifischen Task für diesen Spieler
            ElysiumLogger.log(player.getName() + " hat das Pentagramm beendet!");
        }
    }

    private void spawnPentagramParticles(Location center, Player player) {
        double radius = 1.5; // Radius des Pentagramms
        int numberOfPoints = 5; // Anzahl der Spitzen für das Pentagramm
        double angleOffset = Math.toRadians(90); // 90 Grad, damit eine Spitze nach oben zeigt

        Location[] points = new Location[numberOfPoints];

        // Berechne alle 5 Hauptpunkte des Pentagramms und spawne Partikel
        for (int i = 0; i < numberOfPoints; i++) {
            double angle = i * 2 * Math.PI / numberOfPoints + angleOffset;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            points[i] = new Location(center.getWorld(), x, center.getY(), z);

            player.spawnParticle(Particle.FLAME, points[i], 0);
        }

        // Definiere die Reihenfolge, in der die Punkte verbunden werden müssen
        int[] starOrder = {0, 2, 4, 1, 3, 0};

        // Verbinde die Punkte in der Sternreihenfolge mit Partikellinien
        for (int i = 0; i < starOrder.length - 1; i++) {
            Location start = points[starOrder[i]];
            Location end = points[starOrder[i+1]];

            Vector direction = end.toVector().subtract(start.toVector()).normalize();
            double distance = start.distance(end);

            for (double d = 0; d < distance; d += 0.1) {
                Location particleLoc = start.clone().add(direction.clone().multiply(d));
                player.spawnParticle(Particle.FLAME, particleLoc, 0);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ElysiumLogger.log("ElysiumCore is shutting down...");
    }
}
