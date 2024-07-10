package com.ed522.libkeychain.basemod;

import com.ed522.libkeychain.io.ClientTransactionController;
import com.ed522.libkeychain.server.ServerInstance;

public class ServerBase {

    private ServerBase() {}

    public static final void greet(ServerInstance instance) {

        

    }
    
    static final void run(ClientTransactionController controller, BaseTransaction transaction) {

        // INIT
        /* Enrolment */
        if (!controller.getParameters().trust()) {
            
        }

    }

}
