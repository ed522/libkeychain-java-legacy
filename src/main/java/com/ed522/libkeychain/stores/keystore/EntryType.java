package com.ed522.libkeychain.stores.keystore;

public enum EntryType {
	PRIVATE((byte) 0), PUBLIC((byte) 1), SECRET((byte) 2);
	private final byte value;
	private EntryType(byte value) {
		if (value > 2 || value < 0) throw new IllegalArgumentException("bad byte ID");
		this.value = value; 
	}
	public byte value() {
		return value;
	}
	public static EntryType forID(byte value) {
		if (value == 0) return PRIVATE;
		else if (value == 1) return PUBLIC;
		else if (value == 2) return SECRET;
		else throw new IllegalArgumentException("Bad ID (range is 0-2 inclusive)");
	}
}
