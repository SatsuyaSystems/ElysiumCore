package de.satsuya.elysiumCore;

import org.bukkit.plugin.java.JavaPlugin;
import de.satsuya.elysiumCore.utils.ElysiumLogger;

public final class ElysiumCore extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ElysiumLogger.log("ElysiumCore is starting up...");
        try {
            // Load events
            de.satsuya.elysiumCore.utils.EventLoader.loadEvents(this);
            ElysiumLogger.log("Events loaded successfully.");
        } catch (Exception e) {
            ElysiumLogger.error("Failed to load events: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ElysiumLogger.log("ElysiumCore is shutting down...");
    }
}
