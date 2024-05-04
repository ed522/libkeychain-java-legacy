package com.ed522.libkeychain.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Signal {

    private static final byte TYPE_TRANSACTION_START = 0;
    private static final byte TYPE_TRANSACTION_END = 1;

    byte[] value;

    private Signal(byte type, String name) {
        ByteBuffer buf = ByteBuffer.wrap(value);
        buf.put(type);
        byte[] nameVal = name.getBytes(StandardCharsets.UTF_8);
        buf.putInt(nameVal.length);
        buf.put(nameVal);
    }

    public static Signal startTransaction(String name) {
        return new Signal(TYPE_TRANSACTION_START, name);
    }
    public static Signal endTransaction(String name) {
        return new Signal(TYPE_TRANSACTION_END, name);
    }

    public String getName() {

        ByteBuffer val = ByteBuffer.wrap(value);
        val.position(1);
        int len = val.getInt();
        byte[] raw = new byte[len];
        val.get(raw);
        return new String(raw, StandardCharsets.UTF_8);

    }

}
