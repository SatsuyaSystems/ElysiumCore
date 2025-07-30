package de.satsuya.elysiumCore.utils;

public class ElysiumLogger {
    public static void log(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    public static void debug(String message) {
        if (isDebugEnabled()) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public static void chat(String message) {
        System.out.println(message);
    }

    private static boolean isDebugEnabled() {
        // This could be replaced with a configuration check
        return ConfigLoader.configData.getBoolean("debug");
    }
}
