package com.ed522.libkeychain.message;

import static com.ed522.libkeychain.util.GeneralUtility.exactCastToShort;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.ed522.libkeychain.err.IllegalStateError;
import com.ed522.libkeychain.io.IOController;
import com.ed522.libkeychain.io.MessageCodec;
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

    public static TransactionSignal startTransaction(short id) {
        return new TransactionSignal(MessageType.SIGNAL_TRANSACTION_START, new Entry("tid", id), new Entry("name", IOController.));
    }
    public static TransactionSignal endTransaction(short id) {
        return new TransactionSignal(MessageType.SIGNAL_TRANSACTION_END, new Entry("tid", id));
    }

    public static TransactionSignal parse(byte[] value) {

        MessageType type = MessageType.getInstance(value[0]);

        if (type == MessageType.SIGNAL_TRANSACTION_START) return deserializeTransactionStart();
        else if (type == MessageType.SIGNAL_TRANSACTION_END) return deserializeTransactionEnd();
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
        Object uncast = this.values.get("name");

        if (uncast instanceof String name) {

            byte[] strVal = name.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.allocate(4 + strVal.length);
            buf.putShort(exactCastToShort(strVal.length));
            buf.put(strVal);
        
        } else throw new IllegalStateError("Improperly initialized object; no such ")

    }

}
