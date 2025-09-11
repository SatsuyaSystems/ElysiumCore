package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.interfaces.TaskSchedule;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
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

                    // Lese Konfiguration aus Annotation (falls vorhanden), sonst Defaults
                    TaskSchedule schedule = clazz.getAnnotation(TaskSchedule.class);
                    long delay = (schedule != null) ? schedule.delay() : 0L;
                    long period = (schedule != null) ? schedule.period() : 1200L;
                    boolean async = (schedule == null) || schedule.async();

                    // Starte die Runnable-Instanz als (a)synchrone wiederkehrende Aufgabe
                    BukkitTask task = async
                            ? plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period)
                            : plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);

                    ElysiumLogger.log("Runnable " + clazz.getSimpleName()
                            + " loaded and started successfully. (delay=" + delay
                            + ", period=" + period + ", async=" + async + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
