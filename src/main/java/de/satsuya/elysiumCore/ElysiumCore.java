package de.satsuya.elysiumCore;

import de.satsuya.elysiumCore.loaders.*;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElysiumCore extends JavaPlugin {

    private static ElysiumCore instance;

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

            ManagerLoader.loadManagers();
            ElysiumLogger.log("Managers successfully loaded.");

            EventLoader.loadEvents(this);
            ElysiumLogger.log("Events successfully loaded.");

            CommandLoader.loadCommands(this);

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

    public static ElysiumCore getInstance() {
        return instance;
    }
}