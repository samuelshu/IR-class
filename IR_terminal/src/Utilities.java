import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

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
	
	// write string into a file
	public void writeMapToFile(String filename, Map<String, Integer> map) {
		StringBuilder result = new StringBuilder();
		BufferedWriter writer = null;
		
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			String term = e.getKey();
			double score = e.getValue();
			result.append(term+" "+score+"\n");
		}
		
		try {
		    writer = new BufferedWriter( new FileWriter(filename));
		    writer.write(result.toString());
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
