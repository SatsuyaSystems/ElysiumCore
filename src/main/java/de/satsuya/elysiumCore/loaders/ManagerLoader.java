package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ManagerLoader {

    public static void loadManagers() {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.manager");
        Set<Class<?>> candidates = reflections.getTypesAnnotatedWith(ManagerInterface.class);

        for (Class<?> clazz : candidates) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            ManagerInterface meta = clazz.getAnnotation(ManagerInterface.class);
            String name = meta.name();

            try {
                Constructor<?> ctor = null;
                try {
                    ctor = clazz.getDeclaredConstructor();
                } catch (NoSuchMethodException ignored) { }

                if (ctor == null) {
                    ElysiumLogger.error("Manager " + clazz.getName() + " has no No-Args-Construktor and is skipped.");
                    continue;
                }

                Object instance = ctor.newInstance();
                if (name != null && !name.isBlank()) {
                    ManagerRegistry.register(name, instance);
                    ElysiumLogger.log("Manager " + clazz.getSimpleName() + " (" + name + ") loaded and registert.");
                } else {
                    ManagerRegistry.register(instance);
                    ElysiumLogger.log("Manager " + clazz.getSimpleName() + " loaded and (per Type) registert.");
                }
            } catch (Exception e) {
                ElysiumLogger.error("Error while loading Manager " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}