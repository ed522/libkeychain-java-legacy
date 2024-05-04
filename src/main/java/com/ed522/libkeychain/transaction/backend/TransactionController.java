package com.ed522.libkeychain.transaction.backend;

import com.ed522.libkeychain.io.IOController;
import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.nametable.transactions.TransactionReference;

public class TransactionController {

    private final short transactionNumber;
    private final TransactionReference reference;
    private final IOController controller;
    
    public TransactionController(IOController controller, TransactionReference reference, short transactionNumber) {
        this.transactionNumber = transactionNumber;
        this.controller = controller;
        this.reference = reference;
    }

    public void endTransaction() {
        
    }

    public void sendMessage(Message msg) {

    }

}
