package com.ed522.libkeychain.util;

public class GeneralUtility {
	
	private GeneralUtility() {}

    public static short exactCastToShort(int val) {
        if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) throw new ArithmeticException("Out of range");
        else return (short) val;
    }

}
