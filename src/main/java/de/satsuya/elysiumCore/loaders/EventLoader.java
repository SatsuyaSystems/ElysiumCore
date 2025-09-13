package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventLoader {

    public static void loadEvents(Plugin plugin) {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.events");
        // Collect all methods annotated with @EventHandler
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(org.bukkit.event.EventHandler.class);

        // Determine unique declaring classes
        Set<Class<?>> candidateClasses = new LinkedHashSet<>();
        for (Method method : annotatedMethods) {
            candidateClasses.add(method.getDeclaringClass());
        }

        int loadedCount = 0;
        for (Class<?> clazz : candidateClasses) {
            // Only register classes that implement Listener
            if (!Listener.class.isAssignableFrom(clazz)) {
                continue;
            }
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);
                Listener listener = (Listener) ctor.newInstance();
                Bukkit.getPluginManager().registerEvents(listener, plugin);
                loadedCount++;
                ElysiumLogger.log("Registered event listener: " + clazz.getSimpleName() + " (contains @EventHandler)");
            } catch (NoSuchMethodException e) {
                ElysiumLogger.log("Skipped " + clazz.getName() + " (no no-arg constructor).");
            } catch (Exception e) {
                ElysiumLogger.error("Failed to register listener " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        ElysiumLogger.log("EventLoader finished. Registered " + loadedCount + " listener class(es) with @EventHandler.");
    }
}