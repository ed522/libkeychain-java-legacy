package com.ed522.libkeychain.io;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.message.TransactionSignal;
import com.ed522.libkeychain.nametable.Nametable;

public class SendDaemon implements Runnable {
    
    private final Object lock = new Object();
    private final ConcurrentLinkedDeque<Message> messageDeque;
    private final ConcurrentLinkedDeque<TransactionSignal> signalDeque;
    private Socket socket;
    private Nametable nametable;

    public SendDaemon(Socket socket, Nametable nametable) {
        this.socket = socket;
        this.nametable = nametable;
        this.messageDeque = new ConcurrentLinkedDeque<>();
        this.signalDeque = new ConcurrentLinkedDeque<>();
    }

    public void send(Message message) {
        messageDeque.addFirst(message);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void sendSignal(TransactionSignal signal) {
        signalDeque.addFirst(signal);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void run() {

        try {

            synchronized (lock) {
                while (messageDeque.isEmpty() && signalDeque.isEmpty())
                    lock.wait();
            }
            if (messageDeque.peekLast() != null) MessageCodec.serialize(messageDeque.getLast(), socket.getOutputStream());
            if (signalDeque.peekLast() != null) MessageCodec.serialize(signalDeque.getLast(), socket.getOutputStream());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

}
