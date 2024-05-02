package com.ed522.libkeychain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UtilityTests {
	private static final boolean incrementByteArray(byte[] value) {

		for (int i = value.length - 1; i >= 0; i--) {
			value[i]++;
			if (value[i] != 0) return false;
		}
		return true; // overflowed to all 0

	}

	@Test
	public void testIncrementByteArray_noOverflow() {
		byte[] test = {83, 127, -43, 104};
		assertFalse(incrementByteArray(test));
		assertArrayEquals(test, new byte[] {83, 127, -43, 105});
	}
	@Test
	public void testIncrementByteArray_oneOverflow() {
		byte[] test = {83, 127, 82, -1};
		assertFalse(incrementByteArray(test));
		assertArrayEquals(test, new byte[] {83, 127, 83, 0});
	}
	@Test
	public void testIncrementByteArray_fullOverflow() {
		byte[] test = {-1, -1, -1, -1};
		assertTrue(incrementByteArray(test));
		assertArrayEquals(test, new byte[] {0, 0, 0, 0});
	}

}
