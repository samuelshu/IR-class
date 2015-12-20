import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Utilities {
	
	// write string into a file
	public void writeStringToFile(String filename, String str) {
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter( new FileWriter(filename));
		    writer.write(str);
		} catch ( IOException e) {
		} finally {
		    try {
		        if ( writer != null)
		        writer.close( );
		    } catch ( IOException e) {
		    }
		}
	}
	
	// print result on screen
	public void log(Object aMsg) {
	    System.out.println(String.valueOf(aMsg) + "\n");
	}
}
