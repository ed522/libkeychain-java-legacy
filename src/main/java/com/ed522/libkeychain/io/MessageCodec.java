package com.ed522.libkeychain.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.ed522.libkeychain.message.Field;
import com.ed522.libkeychain.message.Message;
import com.ed522.libkeychain.message.TransactionSignal;

public class MessageCodec {

    // Structure (do not remove):
    /*
     *  struct SerialMessage {                      // this is just for messages, not signals
     *      uint8_t     type;                       // specifies the type, message is 0xFF
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

        out.writeInt(message.getFieldCount());
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

    public static final Message deserializeMessage(InputStream stream) {
        // TODO add implementation
        throw new UnsupportedOperationException("Not implemented: com.ed522.libkeychain.io.MessageDeserializer#deserialize");
    }

}
