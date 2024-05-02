package com.ed522.libkeychain.nametable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MessageEntry {

	private final List<FieldEntry> fields;
    private final String extension;
    private final String group;
	private final String name;
	private int proofFactor;
	private Method proofFormula;
	private boolean signed;


	public boolean isSigned() {
		return signed;
	}
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	public int getProofFactor() {
		return proofFactor;
	}
	public void setProofFactor(int proofFactor) {
		this.proofFactor = proofFactor;
	}
	public Method getProofFormula() {
		return proofFormula;
	}
	public void setProofFormula(Method proofFormula) {
		this.proofFormula = proofFormula;
	}
	public String getExtension() {
		return extension;
	}
	public String getGroup() {
		return group;
	}
	public String getName() {
		return name;
	}


	public MessageEntry(String name, String group, String extension) {
		this.name = name;
		this.group = group;
		this.extension = extension;
		this.fields = new ArrayList<>();
	}

	public List<FieldEntry> getFields() {
		return fields;
	}

	public void addField(FieldEntry f) {
		fields.add(f);
	}
	public void removeField(String fieldName) {
		int i = 0;
		for (FieldEntry e : fields) {
			if (e.getName().equals(fieldName)) {
				fields.remove(i);
				return;
			}
			else i++;
		}
	}

	
}
