package com.ed522.libkeychain.nametable.transactions;

import java.util.ArrayList;
import java.util.List;

public class InstanceRegistry {
	private InstanceRegistry() {}

	private static final List<Object> instances;

	static {
		instances = new ArrayList<>();
	}

	public static void add(Object instance) {
		instances.add(instance);
	}

	public static void remove(Class<?> type) {
		int i = 0;
		for (Object o : instances) {
			if (o.getClass().equals(type)) {
				instances.remove(i);
				return;
			}
			i++;
		}
	}

	public static Object get(Class<?> type) {
		for (Object o : instances) {
			if (o.getClass().equals(type)) return o;
		}
		return null;
	}

	public static boolean has(Class<?> type) {
		for (Object o : instances) {
			if (o.getClass().equals(type)) return true;
		}
		return false;
	}

}
