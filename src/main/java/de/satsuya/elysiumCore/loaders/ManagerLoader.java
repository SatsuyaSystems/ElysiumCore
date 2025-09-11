package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ManagerLoader {

    public static void loadManagers() {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.manager");
        Set<Class<?>> candidates = reflections.getTypesAnnotatedWith(ManagerInterface.class);

        // Sortiere nach Gewicht absteigend (höhere Gewichte zuerst); bei Gleichstand nach Klassenname
        List<Class<?>> ordered = new ArrayList<>(candidates);
        ordered.sort((a, b) -> {
            ManagerInterface ma = a.getAnnotation(ManagerInterface.class);
            ManagerInterface mb = b.getAnnotation(ManagerInterface.class);
            int wa = (ma != null) ? ma.weight() : 0;
            int wb = (mb != null) ? mb.weight() : 0;
            int cmp = Integer.compare(wb, wa);
            if (cmp != 0) return cmp;
            return a.getName().compareTo(b.getName());
        });

        for (Class<?> clazz : ordered) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            ManagerInterface meta = clazz.getAnnotation(ManagerInterface.class);
            String name = meta.name();
            String[] rawParams = meta.params();

            try {
                Object instance = null;

                // Resolve annotation params (supports config placeholders)
                String[] params = resolveParams(rawParams);

                if (params != null && params.length > 0) {
                    Constructor<?> ctorWithArray = null;
                    try {
                        ctorWithArray = clazz.getDeclaredConstructor(String[].class);
                    } catch (NoSuchMethodException ignored) {
                        // no (String[])-constructor available
                    }

                    if (ctorWithArray != null) {
                        ctorWithArray.setAccessible(true);
                        instance = ctorWithArray.newInstance((Object) params);
                    } else {
                        // Try (String, String, ...) constructor with same arity
                        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
                        Constructor<?> matchingStringCtor = null;
                        for (Constructor<?> c : ctors) {
                            Class<?>[] pTypes = c.getParameterTypes();
                            if (pTypes.length == params.length) {
                                boolean allString = true;
                                for (Class<?> pt : pTypes) {
                                    if (!pt.equals(String.class)) {
                                        allString = false;
                                        break;
                                    }
                                }
                                if (allString) {
                                    matchingStringCtor = c;
                                    break;
                                }
                            }
                        }
                        if (matchingStringCtor != null) {
                            matchingStringCtor.setAccessible(true);
                            instance = matchingStringCtor.newInstance((Object[]) params);
                        }
                    }

                    if (instance == null) {
                        ElysiumLogger.error("Manager " + clazz.getName() + " has no matching constructor for the provided annotation parameters. Falling back to DI/no-args.");
                    }
                }

                // Second try: type-based DI (e.g., Plugin, LuckPerms)
                if (instance == null) {
                    Constructor<?> injectableCtor = selectBestInjectableConstructor(clazz);
                    if (injectableCtor != null) {
                        Object[] args = buildInjectableArgs(injectableCtor);
                        if (args != null) {
                            injectableCtor.setAccessible(true);
                            instance = injectableCtor.newInstance(args);
                        } else {
                            ElysiumLogger.error("Failed to resolve injectable arguments for constructor of " + clazz.getName() + ". Falling back to no-args constructor.");
                        }
                    }
                }

                // Final fallback: no-args constructor
                if (instance == null) {
                    Constructor<?> ctor = null;
                    try {
                        ctor = clazz.getDeclaredConstructor();
                    } catch (NoSuchMethodException ignored) { }

                    if (ctor == null) {
                        ElysiumLogger.error("Manager " + clazz.getName() + " has no usable constructor (no-args or injectable) and will be skipped.");
                        continue;
                    }
                    ctor.setAccessible(true);
                    instance = ctor.newInstance();
                }

                if (name != null && !name.isBlank()) {
                    ManagerRegistry.register(name, instance);
                    ElysiumLogger.log("Manager " + clazz.getSimpleName() + " (" + name + ") loaded and registered.");
                } else {
                    ManagerRegistry.register(instance);
                    ElysiumLogger.log("Manager " + clazz.getSimpleName() + " loaded and registered (by type).");
                }
            } catch (Exception e) {
                ElysiumLogger.error("Error while loading manager " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static String[] resolveParams(String[] rawParams) {
        if (rawParams == null || rawParams.length == 0) return rawParams;

        String[] resolved = new String[rawParams.length];
        for (int i = 0; i < rawParams.length; i++) {
            String p = rawParams[i];
            if (p == null) {
                resolved[i] = null;
                continue;
            }
            String key = null;
            if (p.startsWith("${config:") && p.endsWith("}")) {
                key = p.substring("${config:".length(), p.length() - 1);
            } else if (p.startsWith("config:")) {
                key = p.substring("config:".length());
            }

            if (key != null) {
                try {
                    String value = ConfigLoader.configData.getString(key);
                    if (value == null) {
                        ElysiumLogger.error("Config key '" + key + "' not found or is null for manager parameter.");
                    }
                    resolved[i] = value;
                } catch (Exception e) {
                    ElysiumLogger.error("Failed to resolve config key '" + key + "' for manager parameter: " + e.getMessage());
                    resolved[i] = null;
                }
            } else {
                resolved[i] = p;
            }
        }
        return resolved;
    }

    // Choose a constructor where all parameter types can be injected via resolveInjectable()
    private static Constructor<?> selectBestInjectableConstructor(Class<?> clazz) {
        Constructor<?> best = null;
        int bestArity = -1;
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            Class<?>[] types = c.getParameterTypes();
            boolean allInjectable = true;
            for (Class<?> t : types) {
                if (resolveInjectable(t) == null) {
                    allInjectable = false;
                    break;
                }
            }
            if (allInjectable && types.length > bestArity) {
                bestArity = types.length;
                best = c;
            }
        }
        return best;
    }

    private static Object[] buildInjectableArgs(Constructor<?> ctor) {
        Class<?>[] types = ctor.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Object dep = resolveInjectable(types[i]);
            if (dep == null) return null;
            args[i] = dep;
        }
        return args;
    }

    private static Object resolveInjectable(Class<?> type) {
        // Plugin
        if (org.bukkit.plugin.Plugin.class.isAssignableFrom(type)) {
            return de.satsuya.elysiumCore.ElysiumCore.getInstance();
        }
        // LuckPerms
        if ("net.luckperms.api.LuckPerms".equals(type.getName())) {
            try {
                return net.luckperms.api.LuckPermsProvider.get();
            } catch (Throwable t) {
                ElysiumLogger.error("LuckPerms is not available for injection: " + t.getMessage());
                return null;
            }
        }
        return null;
    }
}