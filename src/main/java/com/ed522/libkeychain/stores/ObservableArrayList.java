package com.ed522.libkeychain.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObservableArrayList<T> extends ArrayList<T> {
	
	private final transient List<Consumer<T>> onAdd;
	private final transient List<Consumer<T>> onRemove;
	private final Class<?> type;

	public ObservableArrayList(Class<T> type) {
		super();
		this.type = type;
		this.onAdd = new ArrayList<>();
		this.onRemove = new ArrayList<>();
	}
	
	@Override
	public boolean add(T t) {
		for (Consumer<T> consumer : onAdd) consumer.accept(t);
		return super.add(t);
	}

	@SuppressWarnings("unchecked") // suppress cast to T, it is checked
	@Override
	public boolean remove(Object o) {
		if (!o.getClass().equals(type)) return false;
		for (Consumer<T> consumer : onRemove) consumer.accept((T) o);
		return super.remove(o);
	}

	public void addOnAdd(Consumer<T> routine) {
		onAdd.add(routine);
	}
	public void removeOnAdd(Consumer<T> routine) {
		onAdd.remove(routine);
	}
	public void addOnRemove(Consumer<T> routine) {
		onRemove.add(routine);
	}
	public void removeOnRemove(Consumer<T> routine) {
		onRemove.remove(routine);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ObservableArrayList)) return false;
		return super.equals(other) && ((ObservableArrayList<?>) other).onAdd.equals(this.onAdd);
	}
	@Override
	public int hashCode() {
		return super.hashCode() * onAdd.hashCode() * 31;
	}

}
