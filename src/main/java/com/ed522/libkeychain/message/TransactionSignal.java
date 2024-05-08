package com.ed522.libkeychain.message;

import static com.ed522.libkeychain.util.GeneralUtility.exactCastToShort;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.ed522.libkeychain.err.IllegalStateError;
import com.ed522.libkeychain.io.IOController;
import com.ed522.libkeychain.io.MessageType;

public final class TransactionSignal {

    public record Entry(String key, Object value) {
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }

    private final MessageType type;
    private final Map<String, Object> values = new HashMap<>();

    private TransactionSignal(MessageType type, Entry... entries) {
        this.type = type;
        for (Entry entry : entries) this.values.put(entry.getKey(), entry.getValue());
    }

    public static TransactionSignal startTransaction(short tid) {
        return new TransactionSignal(MessageType.SIGNAL_TRANSACTION_START, new Entry("tid", tid), new Entry("name", IOController.getTransactionName(tid)));
    }
    public static TransactionSignal endTransaction(short tid) {
        return new TransactionSignal(MessageType.SIGNAL_TRANSACTION_END, new Entry("tid", tid));
    }

    public static TransactionSignal parse(byte[] value) {

        MessageType type = MessageType.getInstance(value[0]);

        if (type == MessageType.SIGNAL_TRANSACTION_START) return deserializeTransactionStart(value);
        else if (type == MessageType.SIGNAL_TRANSACTION_END) return deserializeTransactionEnd(value);
        else throw new IllegalStateError("No appropriate serializer for type: " + type.toString());
    }

    
    public MessageType getType() {
        return this.type;
    }

    public byte[] serialize() {
        if (this.type == MessageType.SIGNAL_TRANSACTION_START) return serializeTransactionStart();
        else if (this.type == MessageType.SIGNAL_TRANSACTION_END) return serializeTransactionEnd();
        else throw new IllegalStateError("No appropriate serializer for type: " + type.toString());
    }

    private byte[] serializeTransactionStart() {

        // Structure: short tid, short namelen, String name
        Object uncastTID = this.values.get("tid");
        Object uncastName = this.values.get("name");

        if (uncastName instanceof String name && uncastTID instanceof Short tid) {

            byte[] strVal = name.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.allocate(4 + strVal.length);
            buf.putShort(tid);
            buf.putShort(exactCastToShort(strVal.length));
            buf.put(strVal);
            return buf.array();
        
        } else throw new IllegalStateError("Improperly initialized object; bad property value");

    }

    private static TransactionSignal deserializeTransactionStart(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        MessageType type = MessageType.SIGNAL_TRANSACTION_START;

        short id = buf.getShort();
        short namelen = buf.getShort();
        byte[] raw = new byte[namelen];
        buf.get(raw);
        String name = new String(raw, StandardCharsets.UTF_8);

        return new TransactionSignal(type, new Entry("tid", id), new Entry("name", name));

    }

    private byte[] serializeTransactionEnd() {

        // Structure: short tid
        Object uncastTID = this.values.get("tid");

        if (uncastTID instanceof Short tid) {

            ByteBuffer buf = ByteBuffer.allocate(2);
            buf.putShort(tid);
            return buf.array();
        
        } else throw new IllegalStateError("Improperly initialized object; bad property value");

    }

    private static TransactionSignal deserializeTransactionEnd(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        MessageType type = MessageType.SIGNAL_TRANSACTION_START;

        short id = buf.getShort();

        return new TransactionSignal(type, new Entry("tid", id));

    }

}
