package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class EventLoader {

    public static void loadEvents(Plugin plugin) {
        Plugin effectivePlugin = (plugin != null) ? plugin : ElysiumCore.getInstance();

        Reflections reflections = new Reflections("de.satsuya.elysiumCore.events");
        // Collect all methods annotated with @EventHandler
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(org.bukkit.event.EventHandler.class);

        // Determine unique declaring classes
        Set<Class<?>> candidateClasses = new LinkedHashSet<>();
        for (Method method : annotatedMethods) {
            candidateClasses.add(method.getDeclaringClass());
        }
        // Zusätzlich: alle Subtypen von Listener im Events-Paket mit aufnehmen
        try {
            Set<Class<? extends Listener>> listenerTypes = reflections.getSubTypesOf(Listener.class);
            candidateClasses.addAll(listenerTypes);
        } catch (Throwable t) {
            ElysiumLogger.log("Note: Subtype scanning not available for listeners: " + t.getMessage());
        }

        int loadedCount = 0;
        for (Class<?> clazz : candidateClasses) {
            // Only register classes that implement Listener
            if (!Listener.class.isAssignableFrom(clazz)) {
                continue;
            }
            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) {
                continue;
            }

            try {
                Optional<Listener> maybeListener = instantiateListener((Class<? extends Listener>) clazz, effectivePlugin);
                if (maybeListener.isEmpty()) {
                    ElysiumLogger.log("Skipped " + clazz.getName() + " (no suitable constructor found).");
                    continue;
                }
                Listener listener = maybeListener.get();
                Bukkit.getPluginManager().registerEvents(listener, effectivePlugin);
                loadedCount++;
                ElysiumLogger.log("Registered event listener: " + clazz.getSimpleName() + " (detected via @EventHandler or listener subtype)");
            } catch (Exception e) {
                ElysiumLogger.error("Failed to register listener " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        ElysiumLogger.log("EventLoader finished. Registered " + loadedCount + " listener class(es) with @EventHandler or implementing Listener.");
    }

    private static Optional<Listener> instantiateListener(Class<? extends Listener> clazz, Plugin plugin) throws Exception {
        // 1) Direkter No-Arg-Konstruktor
        try {
            Constructor<? extends Listener> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return Optional.of(ctor.newInstance());
        } catch (NoSuchMethodException ignored) {
        }

        // 2) Konstruktor mit ElysiumCore
        try {
            Constructor<? extends Listener> ctor = clazz.getDeclaredConstructor(ElysiumCore.class);
            ctor.setAccessible(true);
            return Optional.of(ctor.newInstance(ElysiumCore.getInstance()));
        } catch (NoSuchMethodException ignored) {
        }

        // 3) Konstruktor mit Plugin
        try {
            Constructor<? extends Listener> ctor = clazz.getDeclaredConstructor(Plugin.class);
            ctor.setAccessible(true);
            return Optional.of(ctor.newInstance(plugin != null ? plugin : ElysiumCore.getInstance()));
        } catch (NoSuchMethodException ignored) {
        }

        // 4) Konstruktor mit JavaPlugin
        try {
            Constructor<? extends Listener> ctor = clazz.getDeclaredConstructor(JavaPlugin.class);
            ctor.setAccessible(true);
            JavaPlugin jp = (plugin instanceof JavaPlugin) ? (JavaPlugin) plugin : ElysiumCore.getInstance();
            return Optional.of(ctor.newInstance(jp));
        } catch (NoSuchMethodException ignored) {
        }

        // 5) Nicht-statische innere Klasse: Outer erst instanziieren und dann Inner(...) aufrufen
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            Class<?> outerClass = clazz.getDeclaringClass();
            // Versuche Outer via No-Arg zu erzeugen
            try {
                Constructor<?> outerCtor = outerClass.getDeclaredConstructor();
                outerCtor.setAccessible(true);
                Object outerInstance = outerCtor.newInstance();

                // Inner(Outer)
                try {
                    Constructor<? extends Listener> innerCtor = clazz.getDeclaredConstructor(outerClass);
                    innerCtor.setAccessible(true);
                    return Optional.of(innerCtor.newInstance(outerInstance));
                } catch (NoSuchMethodException ignored) {
                }

                // Inner(Outer, ElysiumCore)
                try {
                    Constructor<? extends Listener> innerCtor = clazz.getDeclaredConstructor(outerClass, ElysiumCore.class);
                    innerCtor.setAccessible(true);
                    return Optional.of(innerCtor.newInstance(outerInstance, ElysiumCore.getInstance()));
                } catch (NoSuchMethodException ignored) {
                }

                // Inner(Outer, Plugin)
                try {
                    Constructor<? extends Listener> innerCtor = clazz.getDeclaredConstructor(outerClass, Plugin.class);
                    innerCtor.setAccessible(true);
                    return Optional.of(innerCtor.newInstance(outerInstance, plugin != null ? plugin : ElysiumCore.getInstance()));
                } catch (NoSuchMethodException ignored) {
                }

                // Inner(Outer, JavaPlugin)
                try {
                    Constructor<? extends Listener> innerCtor = clazz.getDeclaredConstructor(outerClass, JavaPlugin.class);
                    innerCtor.setAccessible(true);
                    JavaPlugin jp = (plugin instanceof JavaPlugin) ? (JavaPlugin) plugin : ElysiumCore.getInstance();
                    return Optional.of(innerCtor.newInstance(outerInstance, jp));
                } catch (NoSuchMethodException ignored) {
                }

            } catch (NoSuchMethodException e) {
                ElysiumLogger.log("Inner Listener " + clazz.getName() + " recognized, but external class " + outerClass.getName() + " has no no-arg constructor.");
            }
        }

        // Nichts Passendes gefunden
        return Optional.empty();
    }
}