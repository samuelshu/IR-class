package porter_stemmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Test {
	
	private static String test_str;
	private static String stop_words;
	private static String output = "";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* 1. read file and turn txt to string */
		try {
			test_str = readFile("IR_homework.txt");
			stop_words = readFile("stop_words.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log(test_str);
		
		
		/* 2. lowercase */
		test_str = test_str.toLowerCase();
		
		
		/* 3. stop words removal */
		//split stop_words and test_str string to string[], 
		//comparing and store the string if it not in stop_words  
		String[] StopWordsString = splitString(stop_words);
		StringBuilder result = new StringBuilder("");
		List<String> StopWordList = Arrays.asList(StopWordsString); 
		for (String s : splitString(test_str)) {
		    if (!StopWordList.contains(s)) result.append(s+" ");
		}		
		//log(result);		
		
		/* 4. split string to tokens again */
		String[] test_arr = splitString(result.toString());
		//log(Arrays.toString(test_arr));
		
		/* 5. porter stemmer */
		Stemmer s = new Stemmer();
		
		for (int i = 0; i < test_arr.length; i++) {
			//transfer string to char[], and call stemmer's add(char[] w, int wLen)
			s.add(test_arr[i].toCharArray(), test_arr[i].length());
			s.stem();
			//String u = s.toString();
			//log(test_arr[i] + " -> " + u);
			if(!output.equals(""))
				output = output + "," + s.toString();
			else
				output = s.toString();
		}
		
		/* 6. write result into a file */
		writeStringToFile("PA1_R04725054.txt", output);
	}
	
	static String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
	
	private static String[] splitString(String str) {
		String[] s_arr;
		s_arr = str.replaceAll("'s", " is").
				replaceAll("'m", " am").
				replaceAll("'re", " are").
				replaceAll("n't", " not").
				replaceAll("[^A-Za-z,\\s]+", "").
				split("[,\\s\\.\n]+");
		return s_arr;
	}
	
	private static void writeStringToFile(String filename, String str) {
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
	
	private static void log(Object aMsg) {
	    System.out.println(String.valueOf(aMsg) + "\n");
	}

}
