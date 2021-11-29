import java.io.*;
import java.util.*;
/**
 * The CodeWriter class writes to data to the file
 * 
 * @author Kavyansh Gangwar
 *
 * */
class CodeWriter{

	// the default instance of code writer to be used 
	public static final CodeWriter instance = new CodeWriter();
	
	// writer to write to the file
	private BufferedWriter writer;

	/**
	 * Sole constructor for creating the object with the class only
	 * */
	private CodeWriter(){}
	
	/**
	 * initializes the writer
	 * 
	 * creates the file writer and opens the writer to wrtie to the file.
	 * in oreder to use the writer you should initialize the writer first
	 * 
	 * @param fileName the name of the file that is to be opened
	 * */
	public void initialize(String fileName){
		try{
			this.writer = new BufferedWriter(new FileWriter(fileName));
		}catch(IOException e){
			System.out.println("cannot open file -> "+fileName);
		}
	}

	/**
	 * write to the buffer of writer
	 * 
	 * writes the string to the buffer to be wrote down to the file
	 * 
	 * @param content the data that is to be written to the file
	 * */
	public void writeBuffer(String content){
		try{
			this.writer.write(content);
		}catch(IOException e){
			System.out.println("Cannot write to the file...");
		}
	}

	/**
	 * writes to the file
	 * 
	 * writes the data stored in the buffer to the file
	 * */
	public void write(){
		try{
			this.writer.close();
		}catch (Exception e) {
			System.out.println("Cannot wirte to file...");
		}
	}
}