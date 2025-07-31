package de.satsuya.elysiumCore.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

public class TaskLoader {
    public static void loadAndStartRunnables(Plugin plugin) {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.tasks");
        Set<Class<? extends Runnable>> runnableClasses = reflections.getSubTypesOf(Runnable.class);

        for (Class<? extends Runnable> clazz : runnableClasses) {
            // Ignoriere abstrakte Klassen, da diese nicht instanziiert werden können
            if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
                try {
                    // Instanziiere die Runnable-Klasse
                    Runnable runnable = clazz.getDeclaredConstructor().newInstance();

                    // Starte die Runnable-Instanz als wiederkehrende Aufgabe (Task)
                    // Die folgenden Werte sind nur Beispiele. Du musst sie für jede Task anpassen.
                    long delay = 0L; // Start ohne Verzögerung
                    long period = 20L; // Wiederhole jede Sekunde (20 Ticks)

                    // Um eine Task nur einmal zu starten:
                    // plugin.getServer().getScheduler().runTask(plugin, runnable);

                    // Um eine wiederkehrende Task zu starten:
                    BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);

                    ElysiumLogger.log("Runnable " + clazz.getSimpleName() + " loaded and started successfully.");

                    // Optional: Speichere die BukkitTask-Instanz, um sie später zu verwalten
                    // (z.B. in einer Liste in deiner Hauptklasse, um sie bei onDisable zu stoppen)

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
