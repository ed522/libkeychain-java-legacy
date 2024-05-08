package com.ed522.libkeychain.util;

public class StandardAlgorithms {

    public static final int PBKDF2_ITERATIONS = 650_000;
    public static final int SYMMETRIC_IV_LENGTH = 12;
    public static final int SYMMETRIC_TAG_LENGTH = 16;
    public static final int SYMMETRIC_KEY_LENGTH_BYTES = 32;
    public static final int SYMMETRIC_KEY_LENGTH_BITS = 256;
    public static final int HASH_LENGTH_BYTES = 32;
    
    public static final String PBKDF_MODE = "PBKDF2WithHmacSHA256";
    public static final String SIGNATURE_ALGORITHM = "Ed448";
    public static final String SIGNATURE_ALGORITHM_ASN1_OID = "1.3.101.113";
    public static final String ASYMMETRIC_CURVE_NAME = "Ed448";
    public static final String HASH_NAME = "SHA3-256";
    
    public static final String ASYMMETRIC_CIPHER = "EdDSA";
    public static final String ASYMMETRIC_WRAP = "ECIES/NONE/NoPadding";
    public static final String SYMMETRIC_CIPHER = "ChaCha20-Poly1305";
    public static final String SYMMETRIC_WRAP = "ChaCha20-Poly1305/NONE/NoPadding";
    public static final String SYMMETRIC_TRANSFORMATION = "ChaCha20-Poly1305";

    private StandardAlgorithms() {}
}