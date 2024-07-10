package com.ed522.libkeychain.basemod;

import com.ed522.libkeychain.io.ClientTransactionController;
import com.ed522.libkeychain.transaction.Transaction;
import com.ed522.libkeychain.transaction.TransactionReference;

public class BaseTransaction extends Transaction {

    /*
     * Client               Server
     * 
     * 
     * ---   Greeting   ---
     *              <--------- Greeting
     * Response     ---------->
     *              <--------- Response
     * 
     * Cert date    ---------->
     *                      (check expiry/revocation)
     * CERT GOOD: [
     *              <--------- Cert good
     * ACK          --------->
     * (continue)
     * ]
     * CERT BAD: [
     *              <--------- New cert needed
     * Old hash     ---------> 
     *              <--------- Newest cert
     * ACK          --------->
     * (retry connection with new cert)
     * ]
     * 
     * ===   PREINIT HERE   ===
     * 
     * ---   Enrolment   ---
     * NO TRUST: [
     * (check enrolment from greeting)
     *  NOT ENROLLED: (
     *  Enrol pls    --------->
     *               <--------- ACK
     *  )
     * ]
     * 
     * 
     * ---   update store   ---
     * Get missing  --------->
     *              <--------- Diff
     * Get certs    --------->
     *              <--------- Diff
     * 
     * ===   POSTINIT HERE   ===
     * 
     * New cert message is signed by the key corresponding to
     * the cert with the old hash
     * 
     * If there is an illegal message or error, terminate connection
     * 
     * This scheme is entirely secure *if* the cert is not
     * compromised
     * 
     * If it is, MitM is possible but opportunity is limited
     * in time and must be maintained for all connections
     */
    
    public BaseTransaction(TransactionReference ref) {
        super(ref);
    }

    @Override
    protected void clientStartTransactionImpl(ClientTransactionController controller) {
        ClientBase.run(controller, this);
    }

    @Override
    protected void serverStartTransactionImpl(ClientTransactionController controller) {
        ServerBase.run(controller, this);
    }
    
}
