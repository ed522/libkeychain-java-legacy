package com.ed522.libkeychain.io;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.nametable.Nametable;
import com.ed522.libkeychain.transaction.ClientTransactionController;
import com.ed522.libkeychain.transaction.TransactionReference;

public class IOController {
    
    private static final ConcurrentHashMap<String, Short> ongoingTransactions;
    static {
        ongoingTransactions = new ConcurrentHashMap<>();
    }

    protected final Map<String, Short> getOngoingTransactions() {
        return ongoingTransactions;
    }

    public static final short getOngoingTransaction(String name) {
        return ongoingTransactions.get(name);
    }
    public static final String getTransactionName(short id) {
        List<String> nameList = new ArrayList<>();
        ongoingTransactions.forEach((String s, Short i) -> {
            if (i.shortValue() == id) {
                if (nameList.isEmpty()) nameList.add(s);
                return;
            }
        });
        return nameList.get(0);
    }

    private final SendDaemon sender;
    private final ReceiveDaemon receiver;
    private final Nametable nametable;

    public IOController(SendDaemon sender, ReceiveDaemon receiver, Nametable nametable) {
        this.sender = sender;
        this.receiver = receiver;
        this.nametable = nametable;
    }

    public void sendRaw(Message message) {
        sender.send(message);
    }
    public Message receiveRaw(short transactionNumber) {
        return receiver.read(transactionNumber);
    }

    public void clientStartTransaction(String transactionName) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        TransactionReference ref = nametable.getTransaction(transactionName);
        short id = 0;
        for (short val : ongoingTransactions.values()) {
            if ((id < val && id <= 0) || (id > 0 && val > id && val < 0)) break;
            else id = val;
        }

        ongoingTransactions.put(transactionName, ++id);

        ClientTransactionController controller = new ClientTransactionController(this, ref, id);
        ref.invokeMethod(controller);

    }

}
