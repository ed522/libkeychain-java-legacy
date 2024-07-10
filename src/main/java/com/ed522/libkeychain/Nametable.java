package com.ed522.libkeychain.nametable;

import java.util.ArrayList;
import java.util.List;

import com.ed522.libkeychain.nametable.routines.JavaRoutineReference;

public class Nametable {

	private String groupName;
	private String extensionName;

	private List<MessageEntry> messages;
	private List<JavaRoutineReference> routines;


	public Nametable(String groupName, String extensionName, List<MessageEntry> messages, List<JavaRoutineReference> routines) {

		this.groupName = groupName;
		this.extensionName = extensionName;
		this.messages = messages;
		this.routines = routines;

	}

	public Nametable() {

		this.messages = new ArrayList<>();
		this.routines = new ArrayList<>();

	}

	
	public List<MessageEntry> getMessages() {
		return messages;
	}

	public List<JavaRoutineReference> getRoutines() {
		return routines;
	}

	public String getExtensionName() {
		return extensionName;
	}
	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}
