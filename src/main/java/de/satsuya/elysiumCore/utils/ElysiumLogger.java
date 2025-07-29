package de.satsuya.elysiumCore.utils;

public class ElysiumLogger {
    public static void log(String message) {
        System.out.println("[" + java.time.LocalTime.now().withSecond(0).withNano(0) + "][ElysiumCore] " + message);
    }

    public static void error(String message) {
        System.err.println("[" + java.time.LocalTime.now().withSecond(0).withNano(0) + "][ElysiumCore ERROR] " + message);
    }

    public static void debug(String message) {
        if (isDebugEnabled()) {
            System.out.println("[" + java.time.LocalTime.now().withSecond(0).withNano(0) + "][ElysiumCore DEBUG] " + message);
        }
    }

    private static boolean isDebugEnabled() {
        // This could be replaced with a configuration check
        return true; // For now, always return true for debugging
    }
}
