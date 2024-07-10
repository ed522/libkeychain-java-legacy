package com.ed522.libkeychain.nametable;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ed522.libkeychain.nametable.routines.JavaRoutineReference;

public class NametableParser {
    
    private File file;

    public NametableParser(File file) {
        this.file = file;
    }

    public Nametable readNametable()
        throws ParserConfigurationException, SAXException, IOException, ReflectiveOperationException {
        
        Nametable nametable = new Nametable();

        DocumentBuilderFactory fac = 
            DocumentBuilderFactory.newDefaultInstance();
        fac.setIgnoringComments(true);
        DocumentBuilder builder = fac.newDocumentBuilder();

        Document doc = builder.parse(file);

        nametable.setGroupName(
            doc.getElementsByTagName("grpname")
               .item(0)
               .getTextContent()
        );
        nametable.setGroupName(
            doc.getElementsByTagName("extname")
               .item(0)
               .getTextContent()
        );

        NodeList msgNodes = doc.getElementsByTagName("msg");

        for (int i = 0; i < msgNodes.getLength(); i++) {

            Node msg = msgNodes.item(i);

            Node name = msg.getOwnerDocument().getElementsByTagName("name").item(0);

            NodeList fieldsList = msg.getOwnerDocument().getElementsByTagName("field");
            FieldEntry[] fields = new FieldEntry[fieldsList.getLength()];
            
            for (int j = 0; j < fieldsList.getLength(); j++) {

                Node field = fieldsList.item(j);
                String fieldName = field.getOwnerDocument()
                                        .getElementsByTagName("name")
                                        .item(0)
                                        .getTextContent();
                byte type = FieldEntry.parseTypeString(
                    field.getOwnerDocument()
                         .getElementsByTagName("type")
                         .item(0)
                         .getTextContent()
                );

                fields[j] = new FieldEntry(type, fieldName);
                
            }

            nametable.getMessages().add(new MessageEntry(name.getTextContent(), fields));

        }

        NodeList rtNodes = doc.getElementsByTagName("routines");

        for (int i = 0; i < rtNodes.getLength(); i++) {

            Node routine = rtNodes.item(i);

            Node name = routine.getOwnerDocument().getElementsByTagName("name").item(0);
            Node trigger = routine.getOwnerDocument().getElementsByTagName("trigger").item(0);

            NodeList jRoutines = routine.getOwnerDocument().getElementsByTagName("jref");
            if (jRoutines.getLength() == 0) continue;
            // parse class
            Class<?> c =
                Class.forName(
                    jRoutines.item(0)
                            .getOwnerDocument()
                            .getElementsByTagName("jclass")
                            .item(0)
                            .getTextContent()
                );
            
            JavaRoutineReference ref = new JavaRoutineReference(
                name.getTextContent(),
                trigger.getTextContent(),
                c,
                c.getMethod(
                    jRoutines.item(0)
                             .getOwnerDocument()
                             .getElementsByTagName("jmethod")
                             .item(0)
                             .getTextContent()
                )
            );

            nametable.getRoutines().add(ref);

        }

        return nametable;

    }

}
