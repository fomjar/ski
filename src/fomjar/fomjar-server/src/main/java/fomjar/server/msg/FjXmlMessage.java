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

import fomjar.server.FjMessage;

/**
 * XML格式消息。数据访问参见{@link #xml()}方法
 * 
 * @author fomjar
 */
public class FjXmlMessage implements FjMessage {
    
    private Document xml;
    
    /**
     * 初始化一个空的XML消息
     */
    public FjXmlMessage() {this(null);}
    
    /**
     * 根据给定XML字符串解析并创建一个消息。如果字符串为空，则创建一个空的XMl消息
     * 
     * @param xml 给定的XML字符串
     */
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
    
    /**
     * 以{@link Document}数据类型来访问XML消息数据，内容读写实时生效
     * 
     * @return
     */
    public Document xml() {return xml;}

    /**
     * 实时生成XML消息字符串，以utf-8格式编码，字符串缓冲区最大为1024字节
     */
    @Override
    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        try {new XMLSerializer(os, new OutputFormat(xml(), "utf-8", true)).serialize(xml());}
        catch (IOException e) {e.printStackTrace();}
        return os.toString();
    }

}
