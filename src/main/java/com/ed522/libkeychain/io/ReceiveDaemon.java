package com.ed522.libkeychain.io;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.message.MessageType;
import com.ed522.libkeychain.nametable.Nametable;

public class ReceiveDaemon implements Runnable {

    private final ConcurrentMap<Short, Queue<Message>> messageQueues = new ConcurrentHashMap<>();
    private final Socket socket;
    private final Nametable[] nametables;

    public ReceiveDaemon(Socket socket, Nametable[] nametables) {
        this.socket = socket;
        this.nametables = nametables;
    }

    public Thread getThread() {
        return Thread.ofVirtual().name("ReceiverDaemon." + Thread.currentThread().getName()).start(this);
    }

    @Override
    public void run() {

        while (true) {

            try {
                PushbackInputStream in = new PushbackInputStream(socket.getInputStream());
                int typeRaw = in.read();
                if (typeRaw == -1) continue;
                in.unread(typeRaw);

                MessageType type = MessageType.getInstance((byte) typeRaw); // properly casts to valid type byte

                if (type == MessageType.MESSAGE) {
                    Message msg = MessageCodec.deserializeMessage(in, nametables);
                    messageQueues.get(msg.getTransactionNumber()).add(msg);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        }

    }

    public Message read(short value) {
        return messageQueues.get(value).poll();
    }

}
