package de.satsuya.elysiumCore.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ManagerRegistry {
    private static final Map<String, Object> BY_NAME = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> BY_TYPE = new ConcurrentHashMap<>();

    private ManagerRegistry() {}

    public static void register(String name, Object instance) {
        if (name == null || name.isEmpty() || instance == null) return;
        BY_NAME.put(name, instance);
        BY_TYPE.put(instance.getClass(), instance);
    }

    public static void register(Object instance) {
        if (instance == null) return;
        BY_TYPE.put(instance.getClass(), instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        Object obj = BY_TYPE.get(type);
        if (obj == null) return null;
        return (T) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        Object obj = BY_NAME.get(name);
        if (obj == null) return null;
        return (T) obj;
    }

    public static boolean contains(String name) {
        return BY_NAME.containsKey(name);
    }

    public static boolean contains(Class<?> type) {
        return BY_TYPE.containsKey(type);
    }

    public static void clear() {
        BY_NAME.clear();
        BY_TYPE.clear();
    }
}