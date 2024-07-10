package com.ed522.libkeychain.io;

import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.server.ServerInstance;
import com.ed522.libkeychain.server.Server.ServerParameters;
import com.ed522.libkeychain.transaction.TransactionReference;

public class ClientTransactionController {

    private final short transactionNumber;
    private final TransactionReference reference;
    private final ServerInstance instance;
    
    protected ClientTransactionController(ServerInstance instance, TransactionReference reference, short transactionNumber) {
        this.transactionNumber = transactionNumber;
        this.instance = instance;
        this.reference = reference;
    }

    public void endTransaction() {
        
    }

    public void sendMessage(Message msg) {

    }

    public ServerParameters getParameters() {
        return instance.getParent().getParams();
    }

}
