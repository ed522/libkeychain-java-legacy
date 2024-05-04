package com.ed522.libkeychain.transaction;

import com.ed522.libkeychain.nametable.transactions.TransactionReference;
import com.ed522.libkeychain.transaction.backend.TransactionController;

public abstract class Transaction {
    
    protected TransactionReference ref;

    protected abstract void startTransactionImpl(TransactionController controller);

    protected Transaction(TransactionReference ref) {
        this.ref = ref;
    }

    public void start(TransactionController controller) {

        Thread thread = new Thread(() -> this.startTransactionImpl(controller));
        thread.start();

    }

}
