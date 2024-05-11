package com.ed522.libkeychain.transaction;

import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.server.ServerInstance;

public class ServerTransactionController {

    private final short transactionNumber;
    private final TransactionReference reference;
    private final ServerInstance parent;
    
    public ServerTransactionController(ServerInstance parent, TransactionReference reference, short transactionNumber) {
        this.transactionNumber = transactionNumber;
        this.parent = parent;
        this.reference = reference;
    }

    public void endTransaction() {
        
    }

    public void sendMessage(Message msg) {

    }

}
