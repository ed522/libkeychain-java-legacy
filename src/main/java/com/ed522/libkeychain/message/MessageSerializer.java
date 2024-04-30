package com.ed522.libkeychain.message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MessageSerializer {

    // NOSONAR is to prevent "commented code" warnings.
    // Structure (do not remove):
    /*
     *  struct SerialMessage {                      // NOSONAR
     *      uint32_t    gnamelen;                   // NOSONAR
     *      char        gname[gnamelen];            // Group name
     *      uint32_t    enamelen;                   // NOSONAR
     *      char        ename[enamelen];            // Extension name
     *      uint32_t    msgnamelen;                 // NOSONAR
     *      char        msgname[msgnamelen];        // NOSONAR
     *      uint64_t    fieldnum;                   // NOSONAR
     *      Field       fields[];                   // see Field below
     *  }                                           // NOSONAR
     *  struct Field {                              // Fields will be parsed in the order they appear in the XML.
     *      uint8_t     type;                       // according to FieldEntry
     *      byte[]      value;                      // length depends on type
     *  }                                           // NOSONAR
     */

    

    private MessageSerializer() {}
    
    public static void serialize(Message message, OutputStream stream) throws IOException {

        DataOutputStream out = new DataOutputStream(stream);

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

}
