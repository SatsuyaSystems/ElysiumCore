package de.satsuya.elysiumCore.utils;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.loaders.ConfigLoader;

import java.util.logging.Level;

public class ElysiumLogger {
    public static void log(String message) {
        ElysiumCore.getInstance().getLogger().info("[INFO] " + message);
    }

    public static void error(String message) {
        ElysiumCore.getInstance().getLogger().severe("[ERROR] " + message);
    }

    public static void debug(String message) {
        if (!isDebugEnabled()) return;
        ElysiumCore.getInstance().getLogger().log(Level.FINEST,"[DEBUG] " + message);
    }

    public static void chat(String message) {
        ElysiumCore.getInstance().getLogger().info(message);
    }

    private static boolean isDebugEnabled() {
        // This could be replaced with a configuration check
        return ConfigLoader.configData.getBoolean("debug");
    }
}

