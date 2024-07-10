package com.ed522.libkeychain.nametable;

import java.util.Arrays;
import java.util.Objects;

public class FieldEntry {
    
	public static final byte TYPE_BINARY = 0;
    public static final byte TYPE_STRING = 1;
	public static final byte TYPE_U8 = 2;
	public static final byte TYPE_U16 = 3;
	public static final byte TYPE_U32 = 4;
	public static final byte TYPE_U64 = 5;
	public static final byte TYPE_I8 = 6;
	public static final byte TYPE_I16 = 7;
	public static final byte TYPE_I32 = 8;
	public static final byte TYPE_I64 = 9;
	public static final byte TYPE_F32 = 10;
	public static final byte TYPE_F64 = 11;

	public static final byte TYPE_ARRAY_STRING = -127; // 128, 0 & 0x80
	public static final byte TYPE_ARRAY_BINARY = -126; // 129, 1 & 0x80
	public static final byte TYPE_ARRAY_U8 = -125; // 130, 2 & 0x80
	public static final byte TYPE_ARRAY_U16 = -124; // 131, 3 & 0x80
	public static final byte TYPE_ARRAY_U32 = -123; // 132, 4 & 0x80
	public static final byte TYPE_ARRAY_U64 = -122; // 133, 5 & 0x80
	public static final byte TYPE_ARRAY_I8 = -121; // 134, 6 & 0x80
	public static final byte TYPE_ARRAY_I16 = -120; // 135, 7 & 0x80
	public static final byte TYPE_ARRAY_I32 = -119; // 136, 8 & 0x80
	public static final byte TYPE_ARRAY_I64 = -118; // 137, 9 & 0x80
	public static final byte TYPE_ARRAY_F32 = -117; // 138, 10 & 0x80
	public static final byte TYPE_ARRAY_F64 = -116; // 139, 11 & 0x80

	public static final byte TYPE_INVALID = -1; // 0xFF
	

    private final byte type;
	private final String name;

    public byte getType() {
		return type;
	}
    public String getName() {
		return name;
	}


	public FieldEntry(byte type, String name) {
        this.type = type;
        this.name = name;
    }

	public static byte parseTypeString(String input) {

		switch (input) {
			case "u8":
				return TYPE_U8;
			case "u16":
				return TYPE_U16;
			case "u32":
				return TYPE_U32;
			case "u64":
				return TYPE_U64;
			
			case "i8":
				return TYPE_I8;
			case "i16":
				return TYPE_I16;
			case "i32":
				return TYPE_I32;
			case "i64":
				return TYPE_I64;
		
			case "f32":
				return TYPE_F32;
			case "f64":
				return TYPE_F64;

			case "str":
				return TYPE_STRING;
			case "bin":
				return TYPE_BINARY;


			case "u8[":
				return TYPE_ARRAY_U8;
			case "u16[":
				return TYPE_ARRAY_U16;
			case "u32[":
				return TYPE_ARRAY_U32;
			case "u64[":
				return TYPE_ARRAY_U64;
			
			case "i8[":
				return TYPE_ARRAY_I8;
			case "i16[":
				return TYPE_ARRAY_I16;
			case "i32[":
				return TYPE_ARRAY_I32;
			case "i64[":
				return TYPE_ARRAY_I64;
		
			case "f32[":
				return TYPE_ARRAY_F32;
			case "f64[":
				return TYPE_ARRAY_F64;

			case "str[":
				return TYPE_ARRAY_STRING;
			case "bin[":
				return TYPE_ARRAY_BINARY;

			default:
				return TYPE_INVALID;
		}

	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean equals(Object other) {
		
		if (!(other instanceof FieldEntry)) return false;
		FieldEntry cast = (FieldEntry) other;

		return (
			type == cast.type &&
			name.equals(cast.name)
		);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		int[] values = new int[2];
		values[0] = Objects.hashCode(Byte.valueOf(type));
		values[1] = Objects.hashCode(name);

		return Arrays.hashCode(values);

	}
}
