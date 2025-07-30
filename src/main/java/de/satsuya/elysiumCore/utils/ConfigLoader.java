package de.satsuya.elysiumCore.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import de.satsuya.elysiumCore.utils.ElysiumLogger;

import java.io.File;
import java.util.Arrays;

public class ConfigLoader {
    // This class is responsible for loading and managing the plugin's configuration.
    // It can be extended to include methods for loading specific configurations,
    // handling defaults, and saving changes back to the config file.

    private static File configFile;
    public static FileConfiguration configData;

    public static void loadConfig() {
        // Load the configuration file
        ElysiumLogger.log("Loading configuration...");
        configData = YamlConfiguration.loadConfiguration(configFile);
        // Here you would typically load the config from a file or resource
        // For example, using Bukkit's getConfig() method if this were a Bukkit plugin
    }

    public static void reloadConfig() {
        // Reload the configuration file
        ElysiumLogger.log("Reloading configuration...");
        if (configFile.exists()) {
            configData = YamlConfiguration.loadConfiguration(configFile);
            ElysiumLogger.log("Configuration reloaded successfully.");
        } else {
            ElysiumLogger.error("Configuration file does not exist: " + configFile.getPath());
        }
    }

    public static void setupConfig() {
        // Set up default configuration values if they do not exist
        ElysiumLogger.log("Setting up default configuration...");
        // This could involve checking if certain keys exist and setting defaults if not
        configFile = new File("plugins/ElysiumCore/config.yml");
        if (!configFile.exists()) {
            try {
                // Create the config file if it does not exist
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                configData = new YamlConfiguration();

                configData.set("test", "This is a test configuration");
                configData.set("debug", true);

                configData.save(configFile);
                ElysiumLogger.log("Configuration file created: " + configFile.getPath());
            } catch (Exception e) {
                ElysiumLogger.error("Could not create configuration file: " + e.getMessage());
            }
        } else {
            ElysiumLogger.log("Configuration file already exists: " + configFile.getPath());
        }
    }
}
