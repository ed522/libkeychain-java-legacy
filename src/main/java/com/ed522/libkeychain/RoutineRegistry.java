package com.ed522.libkeychain.nametable.routines;

import java.util.HashMap;

public class RoutineRegistry {

    private RoutineRegistry() {}
    
    private static HashMap<Class<?>, Object> instances;
    static {
        instances = new HashMap<>();
    }

    public static void registerInstance(Class<?> type, Object instance) {
        instances.put(type, instance);
    }
    public static void deregisterInstance(Class<?> type) {
        instances.remove(type);
    }
    public static Object getInstance(Class<?> type) {
        return instances.get(type);
    }
    public static boolean hasInstance(Class<?> type) {
        return instances.containsKey(type);
    }

}
