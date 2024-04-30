package com.ed522.libkeychain.nametable;

import java.util.ArrayList;
import java.util.List;

import com.ed522.libkeychain.nametable.routines.RoutineReference;

public class Nametable {
    
    private String group;
    private String extension;
    private final List<MessageEntry> messages;
    private final List<RoutineReference> routines;
    
    public Nametable(final String group, final String extension) {

        this.setGroup(group);
        this.setExtension(extension);

        this.messages = new ArrayList<>();
        this.routines = new ArrayList<>();

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


    public List<RoutineReference> getRoutines() {
        return routines;
    }


    public MessageEntry getMessage(String name) {
        
        for (MessageEntry msg : messages) if (msg.getName().equals(name)) return msg;
        return null;

    }


}
