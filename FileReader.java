import java.util.*;
import java.io.*;
/**
 * The FileReader class reads data from the file
 * 
 * @author Kavyansh Gangwar
 * */
class FileReader{
	// the default instance of FileReader to be used
	public static final FileReader instance = new FileReader();

	/**
	 * Sole constructor to be used only inside this class for creating objects
	 * */
	private FileReader(){}

	/**
	 * reads file 
	 * 
	 * reads vm code from the file and removes the comments.
	 * 
	 * @param fileName the name of the file that is to be read
	 * 
	 * @return program the is in the file.
	 * */
	public String readFile(String fileName){
		String program = "";

		try{
			File file = new File(fileName);

			Scanner reader = new Scanner(file);
			
			// read all the lines from the file and add them to program
			while(reader.hasNextLine()){

				String data = reader.nextLine();
				// if comment then dont add line to program
				if(data.trim().length() >= 2){
					if(data.trim().substring(0,2).equals("//")){
						continue;
					}
				}
				// add data and \n delimiter to the program to denote the end of line
				program += (data + "\n");
			}

		}catch(FileNotFoundException e){
			System.out.println("File Not Found! -> "+fileName);
		}

		return program;
	}
}