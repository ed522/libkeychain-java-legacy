package com.ed522.libkeychain.nametable;

import java.util.ArrayList;
import java.util.List;

import com.ed522.libkeychain.message.MessageEntry;
import com.ed522.libkeychain.transaction.TransactionReference;

public class Nametable {
    
    private String group;
    private String extension;
    private final List<MessageEntry> messages;
    private final List<TransactionReference> transactions;
    
    public Nametable(final String group, final String extension) {

        this.setGroup(group);
        this.setExtension(extension);

        this.messages = new ArrayList<>();
        this.transactions = new ArrayList<>();

    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }
    public void setGroup(final String group) {
        this.group = group;
    }
    public String getGroup() {
        return group;
    }
    public String getExtension() {
        return extension;
    }
    public List<MessageEntry> getMessages() {
        return messages;
    }


    public List<TransactionReference> getTransactions() {
        return transactions;
    }


    public MessageEntry getMessage(String name) {
        
        for (MessageEntry msg : messages) if (msg.getName().equals(name)) return msg;
        return null;

    }

    public TransactionReference getTransaction(String name) {
        
        for (TransactionReference ref : transactions) if (ref.getName().equals(name)) return ref;
        return null;

    }

}
