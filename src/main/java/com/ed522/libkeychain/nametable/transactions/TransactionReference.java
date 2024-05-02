package com.ed522.libkeychain.nametable.transactions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TransactionReference {

	private enum AccessMode {
		STATIC,
		INSTANCE_REGISTRY,
		INSTANCE_INITIALIZER,
		INSTANCE_CONSTRUCTOR
	}

	private final AccessMode mode;
	private final Method creator;
	private final Constructor<?> constructor;

	private final Class<?> type;
	private final Method method;

	public TransactionReference(Class<?> type, Method method, boolean initialize) throws ReflectiveOperationException {
		
		this.type = type;
		this.method = method;

		// The following conditions must be satisfied:
		// 1. The type must exist and be accessible (enforced by Class object)
		// 2. The method must exist and be accessible (part 1 enforced by object)
		// 3. Method takes no arguments
		// 4. One of the following must be satisfied:
		//	a. The method is static
		//	b. The type is registered
		//	c. The class has a no-args static method annotated with @Initializer named "newInstance" that returns an instance
		//	d. The class has a no-args constructor annotated with @Initializer
		if (Modifier.isStatic(method.getModifiers()) && method.canAccess(null)) {
			// Static mode
			// No creator/constructor
			mode = AccessMode.STATIC;
			creator = null;
			constructor = null;
		} else if (InstanceRegistry.has(type)) {
			// Registry mode, already registered
			// No creator/constructor needed
			mode = AccessMode.INSTANCE_REGISTRY;
			creator = null;
			constructor = null;
		} else if (initialize) { // "initialize": make a new instance for registry?
		
			mode = AccessMode.INSTANCE_REGISTRY;
			creator = null;
			constructor = null;

			Method m = tryGetInitializerMethod(type, "newInstance");

			if (m != null) {
				InstanceRegistry.add(m.invoke(null));
			} else {

				Constructor<?> c = getSuitableConstructor(type);

				if (!c.isAnnotationPresent(Initializer.class) || !c.canAccess(null)) {
					throw new NoSuchMethodException("Out of available options to initialize reference for registry (does it have the @Initializer annotation?)"); // new exception
				}
			}

		} else {

			Method m = tryGetInitializerMethod(type, "newInstance");

			if (m != null) {

				// Creator mode
				// Creator is static intializer, constructor is not used
				creator = m;
				constructor = null;
				mode = AccessMode.INSTANCE_INITIALIZER;
				
			} else {

				creator = null;
				constructor = getSuitableConstructor(type);
				mode = AccessMode.INSTANCE_CONSTRUCTOR;

			}

		}

	}

	public <T> Constructor<T> getSuitableConstructor(Class<T> type) throws NoSuchMethodException {
		
		Constructor<T> c;
		
		try {
			c = type.getConstructor();
		} catch (NoSuchMethodException e1) {
			// rethrow for clarity
			throw new NoSuchMethodException("Out of options, no available constructor to initialize reference (does it have a no-args constructor or static initializer?)");
		}

		if (!c.isAnnotationPresent(Initializer.class) || !c.canAccess(null)) {
			throw new NoSuchMethodException("Out of options, no suitable constructor to initialize reference (does it have the @Initializer annotation?)"); // new exception
		}

		return c;

	}

	public Method tryGetInitializerMethod(Class<?> type, String name) {

		try {
			Method m = type.getMethod(name);
			if (Modifier.isStatic(m.getModifiers()) && m.canAccess(null) && m.isAnnotationPresent(Initializer.class)) return m;
			else return null;
		} catch (NoSuchMethodException e) {
			return null;
		}

	}

	public void invoke() throws IllegalAccessException, InvocationTargetException, InstantiationException {

		if (mode.equals(AccessMode.STATIC)) {
			method.invoke(null);
		} else if (mode.equals(AccessMode.INSTANCE_REGISTRY)) {
			method.invoke(InstanceRegistry.get(type));
		} else if (mode.equals(AccessMode.INSTANCE_INITIALIZER)) {
			method.invoke(creator.invoke(null));
		} else if (mode.equals(AccessMode.INSTANCE_CONSTRUCTOR)) {
			method.invoke(constructor.newInstance());
		}

	}

}
