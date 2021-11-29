import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;

class XMLReader{
	public static final XMLReader instance = new XMLReader();

	private XMLReader(){}

	public Document readFile(String fileName){
		try{
			File file = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}