package com.ed522.libkeychain.transaction;

import com.ed522.libkeychain.io.ClientTransactionController;
import com.ed522.libkeychain.io.ClientTransactionController;

public abstract class Transaction {
    
    protected TransactionReference ref;

    protected abstract void clientStartTransactionImpl(ClientTransactionController controller);
    protected abstract void serverStartTransactionImpl(ClientTransactionController controller);

    protected Transaction(TransactionReference ref) {
        this.ref = ref;
    }

    public void startClient(ClientTransactionController controller) {
        Thread thread = new Thread(() -> this.clientStartTransactionImpl(controller));
        thread.start();
    }
    public void startServer(ClientTransactionController controller) {
        Thread thread = new Thread(() -> this.serverStartTransactionImpl(controller));
        thread.start();
    }

}
