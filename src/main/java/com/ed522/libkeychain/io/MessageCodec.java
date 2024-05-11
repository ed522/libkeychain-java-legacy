package com.ed522.libkeychain.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.ed522.libkeychain.message.Field;
import com.ed522.libkeychain.message.FieldEntry;
import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.message.MessageEntry;
import com.ed522.libkeychain.message.MessageType;
import com.ed522.libkeychain.message.TransactionSignal;
import com.ed522.libkeychain.nametable.Nametable;

public class MessageCodec {

    // Structure (do not remove):
    /*
     *  struct SerialMessage {                      // this is just for messages, not signals
     *      uint8_t     type;                       // specifies the type, message is 0x00
     *      uint32_t    gnamelen;                   // |
     *      char        gname[gnamelen];            // Group name
     *      uint32_t    enamelen;                   // |
     *      char        ename[enamelen];            // Extension name
     *      uint32_t    msgnamelen;                 // |
     *      char        msgname[msgnamelen];        // |
     *      uint64_t    fieldnum;                   // |
     *      Field       fields[];                   // see Field below
     *  }                                           // |
     *  struct Field {                              // Fields will be parsed in the order they appear in the XML.
     *      uint8_t     type;                       // according to FieldEntry
     *      byte[]      value;                      // length depends on type
     *  }                                           // |
     * 
     *  struct SerialSignal {                       // |
     *      uint8_t     type;                       // depends on what you want to do, see TYPE_SIGNAL_?? constants
     *      byte[]      payload;                    // see the ??Payload structs
     *  }                                           // |
     *  struct TransactionStartPayload {            // for transaction start
     *      uint16_t    id;                         // TID to assign to new transaction
     *      uint16_t    namelen;                    // |
     *      char        name[namelen];              // transaction name per TML file
     *  }                                           // |
     *  struct TransactionEndPayload {              // |
     *      uint16_t    id;                         // transaction to end
     *  }                                           // |
     */

    

    private MessageCodec() {}
    
    public static void serialize(Message message, OutputStream stream) throws IOException {

        DataOutputStream out = new DataOutputStream(stream);

        out.write(MessageType.MESSAGE.getTypeByte());
        out.writeShort(message.getTransactionNumber());
        // group name
        out.writeInt(message.getGroup().length());
        out.writeBytes(message.getGroup());
        // extension name
        out.writeInt(message.getExtension().length());
        out.writeBytes(message.getExtension());
        // message name
        out.writeInt(message.getName().length());
        out.writeBytes(message.getName());

        List<Field> fields = message.getFields();
        for (int i = 0; i < fields.size(); i++) {

            out.write(fields.get(i).getType());
            out.write(fields.get(i).getByteValue());

        }

    }

    public static void serialize(TransactionSignal signal, OutputStream output) throws IOException {

        DataOutputStream out = new DataOutputStream(output);
        out.write(signal.getType().getTypeByte());
        byte[] val = signal.serialize();
        out.writeInt(val.length);
        out.write(val);

    }

    public static final Message deserializeMessage(InputStream stream, Nametable[] nametables) throws IOException {
        
        DataInputStream in = new DataInputStream(stream);
        
        short tid = in.readShort();
        
        int len = in.readInt();
        String gname = new String(stream.readNBytes(len), StandardCharsets.UTF_8);
        len = in.readInt();
        String ename = new String(stream.readNBytes(len), StandardCharsets.UTF_8);
        len = in.readInt();
        String mname = new String(stream.readNBytes(len), StandardCharsets.UTF_8);

        Nametable nametable = null;
        for (Nametable nt : nametables) {
            if (nt.getGroup().equals(gname) && nt.getExtension().equals(ename)) {
                nametable = nt;
                break;
            }
        }
        Objects.requireNonNull(nametable, "No such nametable");

        MessageEntry msgEntry = nametable.getMessage(mname);

        Field[] fields = new Field[msgEntry.getFields().size()];
        for (int i = 0; i < msgEntry.getFields().size(); i++) {
            
            FieldEntry entry = msgEntry.getFields().get(i);
            fields[i] = new Field(entry);
            
            parseField(fields[i], in, entry);

        }

        return msgEntry.newInstance(tid);

    }

    // NOSONAR use: Complexity (not applicable)
    private static void parseField(Field field, DataInputStream in, FieldEntry entry) throws IOException { //NOSONAR


        if (entry.getType() == FieldEntry.TYPE_STRING)
            field.setString(new String(deserializeBytes(in), StandardCharsets.UTF_8));
        else if (entry.getType() == FieldEntry.TYPE_BINARY)
            field.setBytes(deserializeBytes(in));
        
        else if (entry.getType() == FieldEntry.TYPE_I8) field.setI8(in.readByte());
        else if (entry.getType() == FieldEntry.TYPE_I16) field.setI16(in.readShort());
        else if (entry.getType() == FieldEntry.TYPE_I32) field.setI32(in.readInt());
        else if (entry.getType() == FieldEntry.TYPE_I64) field.setI64(in.readLong());
        
        else if (entry.getType() == FieldEntry.TYPE_U8) field.setU8(in.readByte());
        else if (entry.getType() == FieldEntry.TYPE_U16) field.setU16(in.readShort());
        else if (entry.getType() == FieldEntry.TYPE_U32) field.setU32(in.readInt());
        else if (entry.getType() == FieldEntry.TYPE_U64) field.setU64(in.readLong());
        
        else if (entry.getType() == FieldEntry.TYPE_F32) field.setF32(in.readFloat());
        else if (entry.getType() == FieldEntry.TYPE_F64) field.setF64(in.readDouble());

        // arrays
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_STRING)
            field.setStringArray(deserializeStringArray(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_BINARY)
            field.setBytesArray(deserialize2DByteArray(in));
        
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_I8) field.setI8Array(deserializeBytes(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_I16) field.setI16Array(deserializeShortArray(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_I32) field.setI32Array(deserializeIntArray(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_I64) field.setI64Array(deserializeLongArray(in));

        else if (entry.getType() == FieldEntry.TYPE_ARRAY_U8) field.setU8Array(deserializeBytes(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_U16) field.setU16Array(deserializeShortArray(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_U32) field.setU32Array(deserializeIntArray(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_U64) field.setU64Array(deserializeLongArray(in));
        
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_F32) field.setF32Array(deserializeFloatArray(in));
        else if (entry.getType() == FieldEntry.TYPE_ARRAY_F64) field.setF64Array(deserializeDoubleArray(in));


    }

    private static byte[] deserializeBytes(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        return stream.readNBytes(len);
    }

    private static short[] deserializeShortArray(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        short[] vals = new short[len];
        for (int i = 0; i < vals.length; i++) vals[i] = stream.readShort();
        return vals;
    }

    private static int[] deserializeIntArray(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        int[] vals = new int[len];
        for (int i = 0; i < vals.length; i++) vals[i] = stream.readInt();
        return vals;
    }

    private static long[] deserializeLongArray(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        long[] vals = new long[len];
        for (int i = 0; i < vals.length; i++) vals[i] = stream.readLong();
        return vals;
    }

    private static float[] deserializeFloatArray(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        float[] vals = new float[len];
        for (int i = 0; i < vals.length; i++) vals[i] = stream.readFloat();
        return vals;
    }

    private static double[] deserializeDoubleArray(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        double[] vals = new double[len];
        for (int i = 0; i < vals.length; i++) vals[i] = stream.readDouble();
        return vals;
    }

    private static String[] deserializeStringArray(DataInputStream stream) throws IOException {
        int entries = stream.readInt();
        String[] vals = new String[entries];
        for (int i = 0; i < vals.length; i++) vals[i] = new String(deserializeBytes(stream), StandardCharsets.UTF_8);
        return vals;
    }
    private static byte[][] deserialize2DByteArray(DataInputStream stream) throws IOException {
        int entries = stream.readInt();
        byte[][] vals = new byte[entries][];
        for (int i = 0; i < vals.length; i++) vals[i] = deserializeBytes(stream);
        return vals;
    }

}
