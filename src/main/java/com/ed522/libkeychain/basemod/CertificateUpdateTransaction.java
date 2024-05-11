package com.ed522.libkeychain.basemod;

import com.ed522.libkeychain.transaction.ClientTransactionController;
import com.ed522.libkeychain.transaction.ServerTransactionController;
import com.ed522.libkeychain.transaction.Transaction;
import com.ed522.libkeychain.transaction.TransactionReference;

public class CertificateUpdateTransaction extends Transaction {

    public CertificateUpdateTransaction(TransactionReference ref) {
        super(ref);
    }

    @Override
    protected void clientStartTransactionImpl(ClientTransactionController controller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clientStartTransactionImpl'");
    }

    @Override
    protected void serverStartTransactionImpl(ServerTransactionController controller) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }
    
}
