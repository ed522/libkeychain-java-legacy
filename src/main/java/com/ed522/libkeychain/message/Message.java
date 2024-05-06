package com.ed522.libkeychain.message;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.ed522.libkeychain.io.MessageCodec;
import com.ed522.libkeychain.nametable.FieldEntry;
import com.ed522.libkeychain.nametable.MessageEntry;

public class Message {
    

    private final List<Field> fields;
    private final MessageEntry entry;
    private final String associatedTransaction;
    private short transactionNumber;
    private byte[] proof;

    public String getGroup() {
        return entry.getGroup();
    }
    public String getExtension() {
        return entry.getExtension();
    }
    public String getName() {
        return entry.getName();
    }


    protected Message(MessageEntry entry, String associatedTransaction) {
        this.entry = entry;
        this.associatedTransaction = associatedTransaction;
        fields = new ArrayList<>();

        for (FieldEntry e : entry.getFields()) {
            fields.add(new Field(e));
        }

    }


    protected void setProof(byte[] proof) {
        this.proof = proof;
    }
    protected void addNewField(Field field) {
        fields.add(field);
    }
    protected void removeField(Field field) {
        fields.remove(field);
    }
    protected void removeField(String name) {
        
        int i = 0;

        for (Field f : fields) {
            if (f.getEntry().getName().equals(name)) fields.remove(i);
            else i++;
        }

    }

    public int getFieldCount() {
        return fields.size();
    }
    public Field getField(String name) {
        
        synchronized (fields) {
            for (Field f : fields) {
                if (f.getEntry().getName().equals(name)) return f;
            }

            return null;
        }

    }
    public List<Field> getFields() {
        
        synchronized (fields) {
            List<Field> newFields = new ArrayList<>();
            for (Field f : this.fields) {
                newFields.add(f.duplicate());
            }
            return newFields;
        }

    }
    public void setField(Field field) {
        
        synchronized (fields) {
            int i = 0;
            for (Field f : fields) {
                if (f.getEntry().getName().equals(field.getEntry().getName())) {

                    if (f.getEntry().getType() != field.getEntry().getType())
                        throw new IllegalArgumentException("The given field and the contained field with a matching name are of different types (wrong field for this message?)");

                    fields.remove(i);
                    fields.add(i, field);
                    return;
                }
                i++;
            }
        }

    }

    protected void prepareToSend(short transactionNumber) throws IllegalAccessException, InvocationTargetException {
        this.transactionNumber = transactionNumber;
        calculateProof(entry.getProofFormula() != null ? (int) entry.getProofFormula().invoke(null) : entry.getProofFactor());
    }

    public void calculateProof(int factor) {
        // TODO implement proofs
    }
    public void verifyProof(int factor) {
        // TODO implement proofs
    }

    public void serializeToStream(OutputStream out) throws IOException {
        MessageCodec.serialize(this, out);
    }
    public short getTransactionNumber() {
        return this.transactionNumber;
    }
    public String getAssociatedTransaction() {
        return this.associatedTransaction;
    }


}
