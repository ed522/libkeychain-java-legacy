package com.ed522.libkeychain.io;

import java.util.HexFormat;

public enum MessageType {
	
	SIGNAL_TRANSACTION_START((byte) 0x00),
	SIGNAL_TRANSACTION_END((byte) 0x01),
	MESSAGE((byte) 0xFF);

	private final byte typeByte;
	public byte getTypeByte() {
		return typeByte;
	}
	private MessageType(byte val) {
		this.typeByte = val;
	}

	public static MessageType getInstance(byte type) {
		for (MessageType mt : MessageType.values()) {
			if (mt.typeByte == type) return mt;
		}
		throw new IllegalArgumentException("No such type: 0x" + HexFormat.of().withUpperCase().toHexDigits(type));
	}

}
