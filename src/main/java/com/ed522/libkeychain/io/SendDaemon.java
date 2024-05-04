package com.ed522.libkeychain.io;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.message.Signal;
import com.ed522.libkeychain.nametable.Nametable;

public class SendDaemon implements Runnable {
    
    private final ConcurrentLinkedDeque<Message> messageDeque;
    private Socket socket;
    private Nametable nametable;

    public SendDaemon(Socket socket, Nametable nametable) {
        this.socket = socket;
        this.nametable = nametable;
        this.messageDeque = new ConcurrentLinkedDeque<>();
    }

    public void send(Message message) {
        messageDeque.addFirst(message);
        synchronized (messageDeque) {
            messageDeque.notifyAll();
        }
    }

    public void sendSignal(Signal signal)

    @Override
    public void run() {

        try {

            synchronized (messageDeque) {
                messageDeque.wait();
            }
            MessageSerializer.serialize(messageDeque.getLast(), socket.getOutputStream());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

}
