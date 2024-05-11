package com.ed522.libkeychain.err;

import java.io.IOException;

public class CorruptDataException extends IOException {
	
    public CorruptDataException() {
        super();
    }
    public CorruptDataException(String msg) {
        super(msg);
    }
    public CorruptDataException(Throwable cause) {
        super(cause);
    }
    public CorruptDataException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
