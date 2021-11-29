import java.util.*;
import org.w3c.dom.*;

class JackAnalyzer{
	public static void main(String[] args){
		if(args.length==0){
			System.out.println("Plese give fileName as argument.");
			return;
		}
		FileReader fileReader = FileReader.instance;
		String code = fileReader.readFile(args[0]);
		JackTokenizer tokenizer = JackTokenizer.instance;
		// System.out.println(code);
		String tokens = tokenizer.tokenize(code);
		CodeWriter writer = CodeWriter.instance;
		String fileName[] = args[0].split("/");
		String tokenFileName = fileName[fileName.length-1].substring(0,fileName[fileName.length-1].length()-5)+"T.xml";
		writer.initialize(tokenFileName);
		writer.writeBuffer(tokens);
		writer.write();
		XMLReader reader = XMLReader.instance;
		Document doc = reader.readFile(tokenFileName);
		JackCompileEngine compileEngine = JackCompileEngine.instance;
		String parseTreeXML="";
		try{
			parseTreeXML = compileEngine.compile(doc.getDocumentElement().getChildNodes());
		}catch (Exception e){
			e.printStackTrace();
		}
		String parseTreeFileName = fileName[fileName.length-1].substring(0,fileName[fileName.length-1].length()-5)+".xml";
		writer.initialize(parseTreeFileName);
		writer.writeBuffer(parseTreeXML);
		writer.write();
		String vmFileName = fileName[fileName.length-1].substring(0,fileName[fileName.length-1].length()-5)+".vm";
		writer.initialize(vmFileName);
		writer.writeBuffer(VMCodeGenerator.instance.getCode());
		writer.write();
	}
}