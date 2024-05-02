package com.ed522.libkeychain.util;

public class StandardAlgorithms {

    public static final int PBKDF2_ITERATIONS = 650_000;
    public static final int CHACHA20_IV_LENGTH = 12;
    public static final int CHACHA20_KEY_LENGTH = 32;
    public static final int CHACHA20_TAG_LENGTH = 16;
    
    public static final String PBKDF_MODE = "PBKDF2WithHmacSHA256";
    public static final String SYMMETRIC_TRANSFORMATION = "ChaCha20-Poly1305";
	public static final String SIGNATURE_ALGORITHM = "Ed448";
	public static final String ASYMMETRIC_CURVE_NAME = "Ed448";

    public static final String SYMMETRIC_CIPHER = "ChaCha20-Poly1305";
    public static final String ASYMMETRIC_CIPHER = "EdDSA";
    public static final String SYMMETRIC_WRAP = "ChaCha20-Poly1305/NONE/NoPadding";
    public static final String ASYMMETRIC_WRAP = "ECIES/NONE/NoPadding";

    private StandardAlgorithms() {}
}
