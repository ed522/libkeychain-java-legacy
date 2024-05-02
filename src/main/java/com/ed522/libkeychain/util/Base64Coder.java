package com.ed522.libkeychain.util;

import java.util.Base64;

public final class Base64Coder {
    private Base64Coder() {}
    
    public static final String byteToB64(final byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static final byte[] b64ToByte(final String base64) {
        return Base64.getDecoder().decode(base64);
    }

}
