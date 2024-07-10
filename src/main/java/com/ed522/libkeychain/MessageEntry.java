package com.ed522.libkeychain.nametable;

import java.util.Arrays;
import java.util.Objects;

public class MessageEntry {

	private String name;
	private FieldEntry[] fields;

	public MessageEntry(String name, FieldEntry[] fields) {
		this.name = name;
		this.fields = fields;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MessageEntry))
			return false;
		MessageEntry cast = (MessageEntry) other;
		return Arrays.equals(cast.fields, fields) && cast.name.equals(name);
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();

		buf.append("MessageEntry [");
		buf.append(" name:" + name);
		buf.append(", fields:" + Arrays.deepToString(fields));
		buf.append(" ]");

		return buf.toString();

	}

	@Override
	public int hashCode() {

		int[] values = new int[2];
		values[0] = Objects.hashCode(name);
		values[1] = Arrays.hashCode(fields);

		return Arrays.hashCode(values);

	}

}