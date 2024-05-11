package com.ed522.libkeychain.message;

public class FieldEntry {


	public static final byte TYPE_BINARY = 0x00;
	public static final byte TYPE_STRING = 0x01;
	public static final byte TYPE_I8 = 0x02;
	public static final byte TYPE_I16 = 0x03;
	public static final byte TYPE_I32 = 0x04;
	public static final byte TYPE_I64 = 0x05;
	public static final byte TYPE_U8 = 0x06;
	public static final byte TYPE_U16 = 0x07;
	public static final byte TYPE_U32 = 0x08;
	public static final byte TYPE_U64 = 0x09;
	public static final byte TYPE_F32 = 0x0A;
	public static final byte TYPE_F64 = 0x0B;

	// identical but with the highest bit set
	// TYPE_ARRAY_BINARY & 0b01111111 == TYPE_BINARY
	public static final byte TYPE_ARRAY_BINARY	 = (byte) 0x80;		 	// 2d array
	public static final byte TYPE_ARRAY_STRING	 = (byte) 0x81;
	public static final byte TYPE_ARRAY_I8		 = (byte) 0x82;
	public static final byte TYPE_ARRAY_I16		 = (byte) 0x83;
	public static final byte TYPE_ARRAY_I32		 = (byte) 0x84;
	public static final byte TYPE_ARRAY_I64		 = (byte) 0x85;
	public static final byte TYPE_ARRAY_U8		 = (byte) 0x86; 		// kind of useless but here for consistency
	public static final byte TYPE_ARRAY_U16		 = (byte) 0x87;
	public static final byte TYPE_ARRAY_U32		 = (byte) 0x88;
	public static final byte TYPE_ARRAY_U64		 = (byte) 0x89;
	public static final byte TYPE_ARRAY_F32		 = (byte) 0x8A;
	public static final byte TYPE_ARRAY_F64		 = (byte) 0x8B;
	
	// all bits set
	public static final byte TYPE_INVALID = (byte) 0xFF;


	public static byte parseType(String type) { //NOSONAR: Cognitive Complexity (this isn't really that complex, it's a less ugly switch block)


		if 		(type.equals("str"))	return TYPE_STRING;
		else if (type.equals("bin")) 	return TYPE_BINARY;
		
		else if (type.equals("i8")) 	return TYPE_I8;
		else if (type.equals("i16")) 	return TYPE_I16;
		else if (type.equals("i32")) 	return TYPE_I32;
		else if (type.equals("i64")) 	return TYPE_I64;
		
		else if (type.equals("u8")) 	return TYPE_U8;
		else if (type.equals("u16")) 	return TYPE_U16;
		else if (type.equals("u32")) 	return TYPE_U32;
		else if (type.equals("u64")) 	return TYPE_U64;
		
		else if (type.equals("f32")) 	return TYPE_F32;
		else if (type.equals("f64")) 	return TYPE_F64;


		else if (type.equals("str[")) 	return TYPE_ARRAY_STRING;
		else if (type.equals("bin[")) 	return TYPE_ARRAY_BINARY;
		
		else if (type.equals("i8[")) 	return TYPE_ARRAY_I8;
		else if (type.equals("i16[")) 	return TYPE_ARRAY_I16;
		else if (type.equals("i32[")) 	return TYPE_ARRAY_I32;
		else if (type.equals("i64[")) 	return TYPE_ARRAY_I64;

		else if (type.equals("u8[")) 	return TYPE_ARRAY_U8;
		else if (type.equals("u16[")) 	return TYPE_ARRAY_U16;
		else if (type.equals("u32[")) 	return TYPE_ARRAY_U32;
		else if (type.equals("u64[")) 	return TYPE_ARRAY_U64;
		
		else if (type.equals("f32[")) 	return TYPE_ARRAY_F32;
		else if (type.equals("f64[")) 	return TYPE_ARRAY_F64;

		else return TYPE_INVALID;


	}


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

}
