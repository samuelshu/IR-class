package porter_stemmer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Cosine {
	public float cosine(String d1, String d2) {
		Map<String, Float> d1_map = ScanFile(d1);
		Map<String, Float> d2_map = ScanFile(d2);
		float tfidf_sum = 0;
		
		for(Map.Entry<String, Float> e : d1_map.entrySet()) {
			String index = e.getKey();
			Float unit_dfidf1 = e.getValue();
			
			if(d2_map.containsKey(index)) {
				Float unit_dfidf2 = d2_map.get(index);
				tfidf_sum = tfidf_sum + (unit_dfidf1 * unit_dfidf2);
			}
		}
		
		return tfidf_sum;
	}
	
	private TreeMap<String, Float> ScanFile(String file) {
		TreeMap<String, Float> map = new TreeMap<String, Float>();
		
		try {
			Scanner scanner = new Scanner(new FileReader(file));

			while(scanner.hasNextLine()) {
				String[] columns = scanner.nextLine().split("\t ");
				String index = columns[0];
				float unit_dfidf = Float.parseFloat(columns[1]); 
				
				map.put(index, unit_dfidf);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;
	}
}
