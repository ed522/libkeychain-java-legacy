package com.ed522.libkeychain.nametable.routines;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaRoutineReference {
    
    private String name;
    private String trigger;
    private Class<?> routineClass;
    private Method method;


    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
    public Class<?> getRoutineClass() {
        return routineClass;
    }
    public void setRoutineClass(Class<?> routineClass) {
        this.routineClass = routineClass;
    }
    public String getTrigger() {
        return trigger;
    }
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public JavaRoutineReference(String name, String trigger, Class<?> routineClass, Method method) {

        this.name = name;
        this.trigger = trigger;
        this.routineClass = routineClass;
        this.method = method;
        
    }


    private static Constructor<?> getNoArgs(Class<?> type) {

        Constructor<?>[] constructors = type.getConstructors();
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == 0 && c.canAccess(null)) return c;
        }

        return null;

    }

    public void run() throws ReflectiveOperationException {
        
        // Must satisfy the following:
        // 1. Method is accessible *and*
        //  a. Method is static *or*
        //  b. Owner has a registered instance *or*
        //  c. Owner implements Constructable for itself *or*
        //  d. Owner has a no-args constructor

        Constructor<?> noArgs = getNoArgs(routineClass);

        if (Modifier.isStatic(this.method.getModifiers()) && this.method.canAccess(null)) {

            this.method.invoke(null);

        } else if (RoutineRegistry.hasInstance(routineClass)) {

            this.method.invoke(RoutineRegistry.getInstance(routineClass));

        } else if (Constructable.class.isAssignableFrom(routineClass)) {

            Object instance = routineClass.getMethod("newInstance").invoke(null);
            RoutineRegistry.registerInstance(routineClass, instance);
            
            method.invoke(instance);

        } else if (noArgs != null) {

            Object instance = noArgs.newInstance();
            RoutineRegistry.registerInstance(routineClass, instance);

            method.invoke(instance);

        }

    }

}
