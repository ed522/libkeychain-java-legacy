package com.ed522.libkeychain.io;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ed522.libkeychain.message.Message;

public class ReceiveDaemon implements Runnable {

    private final ConcurrentMap<Short, Queue<Message>> messageQueues = new ConcurrentHashMap<>();
    private final Socket socket;

    public ReceiveDaemon(Socket socket) {
        this.socket = socket;
    }

    public Thread getThread() {
        return Thread.ofVirtual().name("ReceiverDaemon." + Thread.currentThread().getName()).start(this);
    }

    @Override
    public void run() {

        while (true) {

            try {
                PushbackInputStream pushbackIn = new PushbackInputStream(socket.getInputStream());
                int typeRaw = pushbackIn.read();
                if (typeRaw == -1) continue;
                pushbackIn.unread(typeRaw);

                MessageType type = MessageType.getInstance((byte) typeRaw); // properly casts to valid type byte

                if (type == MessageType.MESSAGE) {
                    Message msg = MessageCodec.deserializeMessage(pushbackIn);
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
