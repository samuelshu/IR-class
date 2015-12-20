package porter_stemmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class GetFileTerms {
	
	int COLLECTION_NUM = 1095;
	String stop_words;
	List<String> StopWordList;
	String tmp_str;
	Stemmer stemmer = new Stemmer();
	
	/* make all files stemmed, and count the term frequency */
	public void getFileTerms() {		
		// get stop words
		try {
			stop_words = readFile("stop_words.txt");
			StopWordList = Arrays.asList(splitString(stop_words));
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		// get 1095 files' string and remove stop words
		for(int i=1; i<=COLLECTION_NUM; i++) {
			// TreeMap will sort by String automatically
			Map<String, Integer> map = new TreeMap<String, Integer>(); 
			StringBuilder result = new StringBuilder("");
			
			try {
				tmp_str = readFile("IRTM/"+i+".txt").toLowerCase();
				for (String s : splitString(tmp_str)) {
						
					// if words is not in stop words list, stem it
				    if (!StopWordList.contains(s)) { 
				    	stemmer.add(s.toCharArray(), s.length());
				    	stemmer.stem();
				    	String stem_str = stemmer.toString();
				    	
				    	// save stemmed words into hashmap, and count appear num
				    	if(!map.containsKey(stem_str)) {
				    		map.put(stem_str, 1);
				    	} else {
				    		int index = map.replace(stem_str, map.get(stem_str)+1);
				    	}
				    }
				}
				
				// get terms and num from treemap, and append into a string
				for (Map.Entry<String, Integer> e : map.entrySet()) {
				    String key = e.getKey();
				    int value = e.getValue();
				    //String str = String.format("%-15s\t %d\n", key, value);
				    //result.append(str);
				    result.append(key+" "+value+"\n");
				}
				
				writeStringToFile("document_terms/"+i+"_terms.txt", result.toString());
				//file.add(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	// make a dictionary to record terms and df amoung 1,095 files
	public Map<String, Float[]> getTermDictionary() {
		
		String file_terms;
		Map<String, Integer> dic_map = new TreeMap<String, Integer>(); 
	    Map<String, Float[]> dic_map_idf = new TreeMap<String, Float[]>();
		StringBuilder dic_result = new StringBuilder("");
		
		// read terms from 1,095 stemmed & counted txt
		for(int i=1; i<=COLLECTION_NUM; i++) {
			try {
				file_terms = readFile("document_terms/"+i+"_terms.txt");
				
				// if this term has shown up, df+1, otherwise, put it into treemap 
				for(String s : splitString(file_terms)) {
					if(!dic_map.containsKey(s)) {
						dic_map.put(s, 1);
					} else {
						dic_map.replace(s, dic_map.get(s)+1);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// get terms and num from treemap, and append into a string
		int index = 1;
		for (Map.Entry<String, Integer> e : dic_map.entrySet()) {
			String term = e.getKey();
			int df = e.getValue();
			float idf = (float) Math.log10(COLLECTION_NUM/df);
		    String str = String.format("%d\t %-15s\t %d\t %f\n", index, term, df, idf);
		    dic_result.append(str);
		    index++;
		    
		    // record index, df, idf to another treemap
		    Float[] index_idf = {(float) index, (float) df, idf};
		    dic_map_idf.put(term, index_idf);
		}
		
		writeStringToFile("dictionary.txt", dic_result.toString());
		return dic_map_idf;
	}
	
	// get tf-idf unit vector
	public void getUnitTFIDFVector() {
		Map<String, Float[]> dic_map_idf = getTermDictionary(); // call getTermDictionary() to get dictionary map
		Map<Integer, Float> uv_map; // saved tf-idf unit vector
		Scanner scanner; // scan file by column
		
		// scan 1,095 terms files to compare with dictionary for getting index and idf
		for(int i=1; i<=COLLECTION_NUM; i++) {
			try {
				scanner = new Scanner(new FileReader("document_terms/"+i+"_terms.txt"));
				float tfidf_sum = 0;
				StringBuilder result = new StringBuilder("");
				uv_map = new TreeMap<Integer, Float>();

				while(scanner.hasNextLine()) {
					String[] columns = scanner.nextLine().split(" ");
					String term = columns[0];
					int tf = Integer.parseInt(columns[1]);
					
					if(dic_map_idf.containsKey(term)) {
						Float[] arr = dic_map_idf.get(term);
						int index = Math.round(arr[0]);
						int df = Math.round(arr[1]);
						float idf = arr[2];
						float tf_idf = tf*idf;
						
						// add all term's tf-idf square 
						tfidf_sum = (float) (tfidf_sum + Math.pow(tf_idf,2));
						
						// record term's index and tf-idf
						uv_map.put(index, tf_idf);
					}
				}
				
				// fetch tf-idf from map
				for (Map.Entry<Integer, Float> e : uv_map.entrySet()) {
					int index = e.getKey();
					Float tf_idf = e.getValue();
					
					// count unit tf-idf value
					Float unit_tfidf = (float) (tf_idf/Math.sqrt(tfidf_sum));
					result.append(index+"\t "+unit_tfidf+"\n");
				}
				
				// save result as DocID.txt
				writeStringToFile("unit_tfidf/Doc"+i+".txt", result.toString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
