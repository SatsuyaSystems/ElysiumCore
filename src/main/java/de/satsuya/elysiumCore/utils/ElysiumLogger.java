package de.satsuya.elysiumCore.utils;

import de.satsuya.elysiumCore.ElysiumCore;

public class ElysiumLogger {
    public static void log(String message) {
        ElysiumCore.getInstance().getLogger().info("[INFO] " + message);
    }

    public static void error(String message) {
        ElysiumCore.getInstance().getLogger().severe("[ERROR] " + message);
    }

    public static void debug(String message) {
        ElysiumCore.getInstance().getLogger().info("[DEBUG] " + message);
    }

    public static void chat(String message) {
        ElysiumCore.getInstance().getLogger().info(message);
    }

    private static boolean isDebugEnabled() {
        // This could be replaced with a configuration check
        return ConfigLoader.configData.getBoolean("debug");
    }
}

