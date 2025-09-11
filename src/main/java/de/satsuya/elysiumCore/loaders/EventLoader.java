package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

public class EventLoader {

    public static void loadEvents(Plugin plugin) {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.events");
        Set<Class<? extends Listener>> listenerClasses = reflections.getSubTypesOf(Listener.class);

        for (Class<? extends Listener> clazz : listenerClasses) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    Listener listener = clazz.getDeclaredConstructor().newInstance();
                    Bukkit.getPluginManager().registerEvents(listener, plugin);
                    ElysiumLogger.log("Event " + clazz.getSimpleName() + " loaded successfully.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}