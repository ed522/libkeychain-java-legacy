package com.ed522.libkeychain.err;

import java.security.GeneralSecurityException;

public class InvalidProofException extends GeneralSecurityException {
    public InvalidProofException() {
        super("Illegal proof!");
    }
}
