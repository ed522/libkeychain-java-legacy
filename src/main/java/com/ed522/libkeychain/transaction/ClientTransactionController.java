package com.ed522.libkeychain.transaction;

import com.ed522.libkeychain.io.IOController;
import com.ed522.libkeychain.message.Message;

public class ClientTransactionController {

    private final short tid;
    private final TransactionReference ref;
    private final IOController io;
    
    public ClientTransactionController(IOController io, TransactionReference ref, short tid) {
        this.tid = tid;
        this.io = io;
        this.ref = ref;
    }

    public void endTransaction() {
        
    }

    public void sendMessage(Message msg) {
        
    }

}
