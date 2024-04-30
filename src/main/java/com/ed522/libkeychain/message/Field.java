package com.ed522.libkeychain.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import com.ed522.libkeychain.nametable.FieldEntry;

public class Field {

	private static final String BAD_DATATYPE_ERRNO = "This data type is not appropriate for this field";

	private final FieldEntry entry;
	private byte[] byteVal;


	/*
	 * The following create proper parseable arrays with headers.
	 */
	private static byte[] makeArray8(byte[] values) {
		ByteBuffer out = ByteBuffer.allocate(values.length + Integer.BYTES);
		out.putInt(values.length);
		out.put(values);
		return out.array();
	}
	private static byte[] makeArray16(short[] values) {
		ByteBuffer out = ByteBuffer.allocate(values.length * Short.BYTES + Integer.BYTES);
		out.putInt(values.length);
		for (short i : values) out.putShort(i);
		return out.array();
	}
	private static byte[] makeArray32(int[] values) {
		ByteBuffer out = ByteBuffer.allocate(values.length * Integer.BYTES + Integer.BYTES);
		out.putInt(values.length);
		for (int i : values) out.putInt(i);
		return out.array();
	}
	private static byte[] makeArray64(long[] values) {
		ByteBuffer out = ByteBuffer.allocate(values.length * Long.BYTES + Integer.BYTES);
		out.putInt(values.length);
		for (long i : values) out.putLong(i);
		return out.array();
	}
	private static byte[] makeArrayF32(float[] values) {
		ByteBuffer out = ByteBuffer.allocate(values.length * Integer.BYTES + Integer.BYTES);
		out.putInt(values.length);
		for (float i : values) out.putDouble(i);
		return out.array();
	}
	private static byte[] makeArrayF64(double[] values) {
		ByteBuffer out = ByteBuffer.allocate(values.length * Long.BYTES + Integer.BYTES);
		out.putInt(values.length);
		for (double i : values) out.putDouble(i);
		return out.array();
	}
	private static byte[] makeArrayByte2D(byte[][] values) {

		int length = Integer.BYTES /* initial header (how many arrays there are) */ + values.length * Integer.BYTES /* headers for each array (how long each array is) */;
		for (byte[] val : values) length += val.length;

		ByteBuffer out = ByteBuffer.allocate(length);
		out.putInt(values.length);
		for (byte[] val : values) out.put(makeArray8(val));

		return out.array();

	}
	private static byte[] makeArrayString(String[] values) {

		int length = Integer.BYTES /* initial header (how many strings there are) */ + values.length * Integer.BYTES /* headers for each string (how long each string is) */;
		for (String val : values) length += val.getBytes(StandardCharsets.UTF_8).length;

		ByteBuffer out = ByteBuffer.allocate(length);
		out.putInt(values.length);
		for (String val : values) out.put(makeArray8(val.getBytes(StandardCharsets.UTF_8)));

		return out.array();

	}
	/*
	 * The following parse arrays with headers.
	 */
	private static byte[] parseArray8(byte[] in) {
		ByteBuffer read = ByteBuffer.wrap(in);
		int len = read.getInt();
		byte[] vals = new byte[len];
		read.get(vals, 0, len);
		return vals;
	}
	private static short[] parseArray16(byte[] in) {
		ByteBuffer read = ByteBuffer.wrap(in);
		int len = read.getInt();
		short[] vals = new short[len];

		for (int i = 0; i < len; i++) {
			vals[i] = read.getShort();
		}
		return vals;
	}
	private static int[] parseArray32(byte[] in) {
		ByteBuffer read = ByteBuffer.wrap(in);
		int len = read.getInt();
		int[] vals = new int[len];

		for (int i = 0; i < len; i++) {
			vals[i] = read.getInt();
		}
		return vals;
	}
	private static long[] parseArray64(byte[] in) {
		ByteBuffer read = ByteBuffer.wrap(in);
		int len = read.getInt();
		long[] vals = new long[len];

		for (int i = 0; i < len; i++) {
			vals[i] = read.getLong();
		}
		return vals;
	}
	private static float[] parseArrayF32(byte[] in) {
		ByteBuffer read = ByteBuffer.wrap(in);
		int len = read.getInt();
		float[] vals = new float[len];

		for (int i = 0; i < len; i++) {
			vals[i] = read.getFloat();
		}
		return vals;
	}
	private static double[] parseArrayF64(byte[] in) {
		ByteBuffer read = ByteBuffer.wrap(in);
		int len = read.getInt();
		double[] vals = new double[len];

		for (int i = 0; i < len; i++) {
			vals[i] = read.getDouble();
		}
		return vals;
	}
	private static byte[][] parseArrayByte2D(byte[] in) {

		ByteBuffer read = ByteBuffer.wrap(in);
		int amount = read.getInt();

		byte[][] bytes = new byte[amount][];
		for (int i = 0; i < amount; i++) {
			int len = read.getInt();
			read.get(bytes[i], 0, len);
		}

		return bytes;

	}
	private static String[] parseArrayString(byte[] in) {

		ByteBuffer read = ByteBuffer.wrap(in);
		int amount = read.getInt();

		String[] strs = new String[amount];
		for (int i = 0; i < amount; i++) {
			int len = read.getInt();
			byte[] val = new byte[len];
			read.get(val, 0, len);
			strs[i] = new String(val, StandardCharsets.UTF_8);
		}

		return strs;

	}
	
	
	/**
	 * Creates a field object.
	 * @param entry The nametable entry for the field.
	 * @param byteVal A byte array for the value. May be null.
	 */
	protected Field(FieldEntry entry, byte[] byteVal) {
		this.entry = entry;
		this.byteVal = byteVal;
	}
	
	public Field(FieldEntry entry) {
		this.entry = entry;
		this.byteVal = new byte[0];
	}
	
	
	public FieldEntry getEntry() {
		return entry;
	}
	public byte[] getByteValue() {
		return byteVal.clone();
	}
	public byte getType() {
		return entry.getType();
	}
	/*
	 * The following methods set the values for all of the datatypes.
	 * They provide a simple and checked way of setting the value.
	 * VERY long and repetitive.
	 */
	// primitives
	public void setU8(byte val) {
		if (this.entry.getType() != FieldEntry.TYPE_U8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = new byte[] {val};
	}
	public void setU16(short val) {
		if (this.entry.getType() != FieldEntry.TYPE_U16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		// Convert value to byte array. Direct put(<index>, <val>) is faster than regular put(<val>).
		this.byteVal = ByteBuffer.allocate(Short.BYTES).putShort(0, val).array();
	}
	public void setU32(int val) {
		if (this.entry.getType() != FieldEntry.TYPE_U32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Integer.BYTES).putInt(0, val).array();
	}
	public void setU64(long val /* unsigned */) {
		if (this.entry.getType() != FieldEntry.TYPE_U64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Long.BYTES).putLong(0, val).array();
	}
	public void setI8(byte val) {
		if (this.entry.getType() != FieldEntry.TYPE_I8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = new byte[] {val};
	}
	public void setI16(short val) {
		if (this.entry.getType() != FieldEntry.TYPE_I16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Short.BYTES).putShort(0, val).array();
	}
	public void setI32(int val) {
		if (this.entry.getType() != FieldEntry.TYPE_I32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Integer.BYTES).putInt(0, val).array();
	}
	public void setI64(long val) {
		if (this.entry.getType() != FieldEntry.TYPE_I64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Long.BYTES).putLong(0, val).array();
	}
	public void setF32(float val) {
		if (this.entry.getType() != FieldEntry.TYPE_F32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Float.BYTES).putFloat(0, val).array();
	}
	public void setF64(double val) {
		if (this.entry.getType() != FieldEntry.TYPE_F64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = ByteBuffer.allocate(Double.BYTES).putDouble(0, val).array();
	}
	public void setBytes(byte[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = val;
	}
	public void setString(String val) {
		if (this.entry.getType() != FieldEntry.TYPE_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = val.getBytes(StandardCharsets.UTF_8);
	}
	// array types
	public void setU8Array(byte[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray8(val);
	}
	public void setU16Array(short[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray16(val);
	}
	public void setU32Array(int[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray32(val);
	}
	public void setU64Array(long[] val /* unsigned */) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray64(val);
	}
	public void setI8Array(byte[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray8(val);
	}
	public void setI16Array(short[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray16(val);
	}
	public void setI32Array(int[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray32(val);
	}
	public void setI64Array(long[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArray64(val);
	}
	public void setF32Array(float[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_F32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArrayF32(val);
	}
	public void setF64Array(double[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_F64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArrayF64(val);
	}
	public void setBytesArray(byte[][] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArrayByte2D(val);
	}
	public void setStringArray(String[] val) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		this.byteVal = makeArrayString(val);
	}
	/*
	 * The following methods get the values for all of the datatypes.
	 * VERY long and repetitive as well.
	 */
	// primitives
	public byte getU8() {
		if (this.entry.getType() != FieldEntry.TYPE_U8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return byteVal[0];
	}
	public short getU16() {
		if (this.entry.getType() != FieldEntry.TYPE_U16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Short.BYTES).put(0, byteVal).getShort();
	}
	public int getU32() {
		if (this.entry.getType() != FieldEntry.TYPE_U32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Integer.BYTES).put(0, byteVal).getInt();
	}
	public long getU64() {
		if (this.entry.getType() != FieldEntry.TYPE_U64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Long.BYTES).put(0, byteVal).getLong();
	}
	public byte getI8() {
		if (this.entry.getType() != FieldEntry.TYPE_I8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return byteVal[0];
	}
	public short getI16() {
		if (this.entry.getType() != FieldEntry.TYPE_I16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Short.BYTES).put(0, byteVal).getShort();
	}
	public int getI32() {
		if (this.entry.getType() != FieldEntry.TYPE_I32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Integer.BYTES).put(0, byteVal).getInt();
	}
	public long getI64() {
		if (this.entry.getType() != FieldEntry.TYPE_I64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Long.BYTES).put(0, byteVal).getLong();
	}
	public float getF32() {
		if (this.entry.getType() != FieldEntry.TYPE_F32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Float.BYTES).put(0, byteVal).getFloat();
	}
	public double getF64() {
		if (this.entry.getType() != FieldEntry.TYPE_F64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return ByteBuffer.allocate(Double.BYTES).put(0, byteVal).getDouble();
	}
	public byte[] getBytes() {
		if (this.entry.getType() != FieldEntry.TYPE_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray8(byteVal);
	}
	public String getString() {
		if (this.entry.getType() != FieldEntry.TYPE_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return new String(parseArray8(byteVal), StandardCharsets.UTF_8);
	}
	// array types
	public byte[] getU8Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray8(byteVal);
	}
	public short[] getU16Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray16(byteVal);
	}
	public int[] getU32Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray32(byteVal);
	}
	public long[] getU64Array( /* unsigned */) {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_U64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray64(byteVal);
	}
	public byte[] getI8Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I8) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray8(byteVal);
	}
	public short[] getI16Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I16) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray16(byteVal);
	}
	public int[] getI32Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray32(byteVal);
	}
	public long[] getI64Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_I64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArray64(byteVal);
	}
	public float[] getF32Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_F32) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArrayF32(byteVal);
	}
	public double[] getF64Array() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_F64) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArrayF64(byteVal);
	}
	public byte[][] getBytesArray() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArrayByte2D(byteVal);
	}
	public String[] getStringArray() {
		if (this.entry.getType() != FieldEntry.TYPE_ARRAY_BINARY) throw new UnsupportedOperationException(BAD_DATATYPE_ERRNO);
		return parseArrayString(byteVal);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		
		if (!(other instanceof Field cast)) return false;

		return (
			entry.equals(cast.entry) &&
			Arrays.equals(byteVal, cast.byteVal)
		);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		int[] values = new int[2];
		values[0] = Objects.hashCode(entry);
		values[1] = Arrays.hashCode(byteVal);

		return Arrays.hashCode(values);

	}

	public Field duplicate() {
		
		Field f = new Field(entry); // immutable
		f.byteVal = this.byteVal.clone();
		return f;

	}

}