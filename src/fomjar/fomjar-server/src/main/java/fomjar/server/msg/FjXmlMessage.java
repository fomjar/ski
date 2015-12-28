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

public class FjXmlMessage extends FjMessage {
	
	private Document xml;
	
	public FjXmlMessage(String xml) {
		if (null == xml) throw new NullPointerException();
		
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		try {this.xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);}
		catch (SAXException | IOException | ParserConfigurationException e) {e.printStackTrace();}
		finally {try {is.close();} catch (IOException e) {}}
	}
	
	public Document xml() {
		return xml;
	}

	@Override
	public String toString() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {new XMLSerializer(os, new OutputFormat(xml(), "utf-8", true)).serialize(xml());}
		catch (IOException e) {e.printStackTrace();}
		return os.toString();
	}

}
