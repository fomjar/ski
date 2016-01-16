package fomjar.server.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FjXmlMessage implements FjMessage {
    
    private Document xml;
    
    public FjXmlMessage() {this(null);}
    
    public FjXmlMessage(String xml) {
        if (null == xml || 0 == xml.length()) {
            try {this.xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();}
            catch (ParserConfigurationException e) {e.printStackTrace();}
        } else {
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            try {this.xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);}
            catch (SAXException | IOException | ParserConfigurationException e) {e.printStackTrace();}
        }
    }
    
    public Document xml() {return xml;}

    @Override
    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        try {new XMLSerializer(os, new OutputFormat(xml(), "utf-8", true)).serialize(xml());}
        catch (IOException e) {e.printStackTrace();}
        return os.toString();
    }

}
