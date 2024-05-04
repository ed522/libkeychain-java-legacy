package com.ed522.libkeychain.nametable.transactions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.ed522.libkeychain.transaction.Transaction;
import com.ed522.libkeychain.transaction.backend.TransactionController;

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
	private final String name;

	public TransactionReference(Class<?> type, String name, boolean initialize) throws ReflectiveOperationException {
		
		this.type = type;
		if (!type.isAssignableFrom(Transaction.class)) throw new IllegalArgumentException("The type given does not extend Transaction");
		this.method = type.getMethod("start", TransactionController.class);
		this.name = name;

		// The following conditions must be satisfied:
		// 1. The type must exist and be accessible (enforced by Class object)
		// 2. The type must extend Transaction
		// 3. One of the following must be satisfied:
		//	a. The type is registered
		//	b. The class has a no-args static factory method annotated with @Initializer named "newInstance"
		//	c. The class has a no-args constructor annotated with @Initializer
		if (InstanceRegistry.has(type)) {
			// Registry mode, already registered
			// No creator/constructor needed
			mode = AccessMode.INSTANCE_REGISTRY;
			creator = null;
			constructor = null;
		} else if (initialize) { // "initialize": make a new instance for registry
		
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
	public String getName() {
		return name;
	}

	private <T> Constructor<T> getSuitableConstructor(Class<T> type) throws NoSuchMethodException {
		
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

	private Method tryGetInitializerMethod(Class<?> type, String name) {

		try {
			Method m = type.getMethod(name);
			if (Modifier.isStatic(m.getModifiers()) && m.canAccess(null) && m.isAnnotationPresent(Initializer.class)) return m;
			else return null;
		} catch (NoSuchMethodException e) {
			return null;
		}

	}

	public void invokeMethod(TransactionController controller) throws IllegalAccessException, InvocationTargetException, InstantiationException {

		if (mode.equals(AccessMode.INSTANCE_REGISTRY)) {
			method.invoke(InstanceRegistry.get(type), controller);
		} else if (mode.equals(AccessMode.INSTANCE_INITIALIZER)) {
			method.invoke(creator.invoke(null), controller);
		} else if (mode.equals(AccessMode.INSTANCE_CONSTRUCTOR)) {
			method.invoke(constructor.newInstance(), controller);
		}

	}

}
