package com.ed522.libkeychain.server;

import java.net.Socket;

import com.ed522.libkeychain.basemod.ServerBase;
import com.ed522.libkeychain.server.Server.ServerAccessor;

public class ServerInstance extends Thread {
    
    private final ServerAccessor parent;

    protected ServerInstance(ServerAccessor serverAccessor, Socket sock) {
        this.parent = serverAccessor;
    }

    public void run() {

        // greeting
        ServerBase.greet(this);
        
    }
    public ServerAccessor getParent() {
        return parent;
    }
    
}
