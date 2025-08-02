package de.satsuya.elysiumCore;

import de.satsuya.elysiumCore.utils.*;
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

    private static ElysiumCore instance;
    private final Map<UUID, BukkitTask> activePentagrams = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        
        ElysiumLogger.log("ElysiumCore is starting...");
        try {
            ConfigLoader.setupConfig();
            ConfigLoader.loadConfig();
            ElysiumLogger.log("Configuration successfully loaded.");

            if (ConfigLoader.configData.getBoolean("debug")) {
                ElysiumLogger.debug("Debug mode is enabled.");
            }

            EventLoader.loadEvents(this);
            ElysiumLogger.log("Events successfully loaded.");

            CommandLoader.loadCommands(this);
            ElysiumLogger.log("Commands successfully loaded.");

            TaskLoader.loadAndStartRunnables(this);
            ElysiumLogger.log("Tasks successfully loaded and started.");
            
        } catch (Exception e) {
            ElysiumLogger.error("Error loading plugin components: " + e.getMessage());
            e.printStackTrace();
        }
        
        startGlobalSneakCheckTask();
        ElysiumLogger.log("ElysiumCore has been successfully activated!");
    }
    
    @Override
    public void onDisable() {
        ElysiumLogger.log("ElysiumCore is shutting down...");
        ElysiumLogger.log("Additional bugs Sponsored by angryzero.");
        
        // Pentagram Tasks stoppen
        for (BukkitTask task : activePentagrams.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activePentagrams.clear();
        
        ElysiumLogger.log("ElysiumCore has been shut down.");
    }
    
    public static ElysiumCore getInstance() {
        return instance;
    }

    private void startGlobalSneakCheckTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp() && player.isSneaking()) {
                    if (!activePentagrams.containsKey(player.getUniqueId())) {
                        startPentagramTask(player);
                    }
                } else {
                    if (activePentagrams.containsKey(player.getUniqueId())) {
                        stopPentagramTask(player);
                    }
                }
            }
        }, 0L, 5L);
    }

    private void startPentagramTask(Player player) {
        Location center = player.getLocation().clone();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            spawnPentagramParticles(center, player);
        }, 0L, 5L);
        activePentagrams.put(player.getUniqueId(), task);
        ElysiumLogger.log(player.getName() + " has started a pentagram!");
    }

    private void stopPentagramTask(Player player) {
        BukkitTask task = activePentagrams.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            ElysiumLogger.log(player.getName() + " has ended the pentagram!");
        }
    }

    private void spawnPentagramParticles(Location center, Player player) {
        double radius = 1.5;
        int numberOfPoints = 5;
        double angleOffset = Math.toRadians(90);

        Location[] points = new Location[numberOfPoints];

        for (int i = 0; i < numberOfPoints; i++) {
            double angle = i * 2 * Math.PI / numberOfPoints + angleOffset;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            points[i] = new Location(center.getWorld(), x, center.getY(), z);

            player.spawnParticle(Particle.FLAME, points[i], 0);
        }

        int[] starOrder = {0, 2, 4, 1, 3, 0};

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
}