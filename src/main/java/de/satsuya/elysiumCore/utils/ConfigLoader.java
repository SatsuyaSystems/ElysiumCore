package de.satsuya.elysiumCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigLoader {
    
    private static File configFile;
    public static FileConfiguration configData;

    public static void setupConfig() {
        ElysiumLogger.log("Setting up default configuration...");
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("ElysiumCore");
        
        if (plugin != null) {
            // Set config file path
            configFile = new File(plugin.getDataFolder(), "config.yml");

            // Save default config from resources
            plugin.saveDefaultConfig();
        } else {
            ElysiumLogger.error("Plugin instance not found!");
        }
    }

    public static void loadConfig() {
        ElysiumLogger.log("Loading configuration...");
        
        // If configFile is not set yet, set it now
        if (configFile == null) {
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("ElysiumCore");
            if (plugin != null) {
                configFile = new File(plugin.getDataFolder(), "config.yml");
            }
        }
        
        if (configFile != null && configFile.exists()) {
            configData = YamlConfiguration.loadConfiguration(configFile);
            ElysiumLogger.log("Configuration successfully loaded.");
        } else {
            ElysiumLogger.error("Configuration file not found!");
        }
    }

    public static void saveConfig() {
        ElysiumLogger.log("Saving configuration...");
        if (configData != null && configFile != null) {
            try {
                configData.save(configFile);
                ElysiumLogger.log("Configuration saved successfully.");
            } catch (Exception e) {
                ElysiumLogger.error("Could not save configuration: " + e.getMessage());
            }
        } else {
            ElysiumLogger.error("Configuration data or file is null, cannot save.");
        }
    }

    public static void reloadConfig() {
        ElysiumLogger.log("Reloading configuration...");
        
        // If configFile is not set yet, set it now
        if (configFile == null) {
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("ElysiumCore");
            if (plugin != null) {
                configFile = new File(plugin.getDataFolder(), "config.yml");
            }
        }
        
        if (configFile != null && configFile.exists()) {
            configData = YamlConfiguration.loadConfiguration(configFile);
            ElysiumLogger.log("Configuration reloaded successfully.");
        } else {
            ElysiumLogger.error("Configuration file does not exist: " + (configFile != null ? configFile.getPath() : "null"));
        }
    }
}