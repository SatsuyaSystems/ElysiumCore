package de.satsuya.elysiumCore;

import de.satsuya.elysiumCore.manager.EcoManager;
import de.satsuya.elysiumCore.manager.GuildManager;
import de.satsuya.elysiumCore.utils.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ElysiumCore extends JavaPlugin {

    private static ElysiumCore instance;
    private final Map<UUID, BukkitTask> activePentagrams = new HashMap<>();
    private static MongoDBManager mongoDBManager;
    public static GuildManager guildManager;
    public static EcoManager ecoManager;

    @Override
    public void onEnable() {
        instance = this;
        
        ElysiumLogger.log("ElysiumCore is starting...");
        try {
            ConfigLoader.setupConfig();
            ConfigLoader.loadConfig();
            ElysiumLogger.log("Configuration successfully loaded.");

            String mongoUri = ConfigLoader.configData.getString("mongodb.uri");
            String databaseName = ConfigLoader.configData.getString("mongodb.database");

            mongoDBManager = new MongoDBManager(mongoUri, databaseName);
            guildManager = new GuildManager();
            ecoManager = new EcoManager();

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

        ElysiumLogger.log("ElysiumCore has been successfully activated!");
    }
    
    @Override
    public void onDisable() {
        ElysiumLogger.log("ElysiumCore is shutting down...");
        ElysiumLogger.log("Additional bugs Sponsored by angryzero.");
        ElysiumLogger.log("ElysiumCore has been shut down.");
    }

    public static MongoDBManager getMongoDBManager() {
        return mongoDBManager;
    }
    public static GuildManager getGuildManager() { return guildManager; }
    public static EcoManager getEcoManager() { return ecoManager; }
    
    public static ElysiumCore getInstance() {
        return instance;
    }
}