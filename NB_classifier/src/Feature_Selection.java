import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Feature_Selection {
	
	static int class_num = 13;
	static int class_doc_num = 15;
	static int N = class_num * class_doc_num;
	Utilities u = new Utilities();
	Classifier classifier = new Classifier();
	Map<String, Double> new_training_vocabulary;
	Map<String, Integer> training_doc_id; //training document id
	
	/* Import training data and combine them */
	public void ImportData() {
		// <term, {tf, df}>
		Map<String, Integer[]> training_index = new TreeMap<String, Integer[]>(); //training vocabulary
		training_doc_id = new TreeMap<String, Integer>(); //training document id
		
		try {
			// load training data index
			Scanner scanner = new Scanner(new FileReader("training.txt"));

			while(scanner.hasNextLine()) {
				String[] columns = scanner.nextLine().split(" ");
				String class_i = columns[0];
				StringBuilder result = new StringBuilder();
				
				// <term, {tf, df}>
				Map<String, Integer[]> class_term = new TreeMap<String, Integer[]>();
				
				for(int i=1; i<columns.length; i++) {
					try {
						// record the training doc id
						training_doc_id.put(columns[i], 0);
						
						// read training doc for each class, and combine them into a big doc
						Scanner doc_scanner = new Scanner(new FileReader("document_terms/"+columns[i]+"_terms.txt"));
						while(doc_scanner.hasNextLine()) {
							String[] doc_columns = doc_scanner.nextLine().split(" ");
							String term = doc_columns[0];
							int tf = Integer.parseInt(doc_columns[1]);
							
							// integrate all documents' term in a class to a document
							// calculate tf and df at the same time
							if(!class_term.containsKey(term)) {
								Integer[] tf_df = {tf, 1};
								class_term.put(term, tf_df);
							} else {
								Integer[] num = class_term.get(term);
								Integer[] tf_df = {num[0]+tf, num[1]+1};
								class_term.replace(term, tf_df);
							}
							
							// integrate all training data into a index(vocabulary)
							if(!training_index.containsKey(term)) {
								Integer[] tf_df = {tf, 1};
								training_index.put(term, tf_df);
							} else {
								Integer[] num = training_index.get(term);
								Integer[] tf_df = {num[0]+tf, num[1]+1};
								training_index.replace(term, tf_df);
							}
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// make a document for every class's terms 
				for(Map.Entry<String, Integer[]> e : class_term.entrySet()) {
					String term = e.getKey();
					Integer[] tf_df = e.getValue();
					result.append(term+" "+tf_df[0]+" "+tf_df[1]+"\n");
				}
				u.writeStringToFile("class_terms/class_"+class_i+".txt", result.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		makeLLRMatrix(training_index);
	}
	
	/* Calculate LLR Matrix */
	public void makeLLRMatrix( Map<String, Integer[]> training_index ) {
		new_training_vocabulary = new TreeMap<String, Double>(); // new vocabulary after feature selection
		StringBuilder training_result = new StringBuilder();
		
		// load combined document for every class
		for(int i=1; i<=13; i++) {
			Scanner scanner;
			// <term, {LLR, opposite, tf/train_tf}>
			Map<String, Double> class_LLR = new TreeMap<String, Double>();
			
			try {
				scanner = new Scanner(new FileReader("class_terms/class_"+i+".txt"));
				while(scanner.hasNextLine()) {
					String[] columns = scanner.nextLine().split(" ");
					String term = columns[0];
					int tf = Integer.parseInt(columns[1]);
					int df = Integer.parseInt(columns[2]);
					
					if(!class_LLR.containsKey(term)) {
						// get tf and df from training_index before LLR
						Integer[] train_tf_df = training_index.get(term);
						int train_tf = train_tf_df[0];
						int train_df = train_tf_df[1];
						
						double n11 = df; // n11: on_topic, present
						double n10 = class_doc_num-n11; // n10: on_topic, absent
						double n01 = train_df-n11; // n01: off_topic, present
						double n00 = N-(n11+n10+n01); // n00: off_topic, absent
						
						double pt = (n11+n01)/N;
						double p1 = n11/(n11+n10);
						double p2 = n01/(n01+n00);
						
						//calaulate LLR, avoid p1, 1-p1, p2, 1-p2 to be 0
						double numerator = (n11+n01)*Math.log(pt) + (n10+n00)*Math.log(1-pt);
						double a = (p1>0) ? n11*Math.log(p1) : 0;
						double b = (1-p1>0) ? n10*Math.log(1-p1) : 0;
						double c = (p2>0) ? n01*Math.log(p2) : 0;
						double d = (1-p2>0) ? n00*Math.log(1-p2) : 0;
						double denominator = a+b+c+d;
						double LLR = (-2) * (numerator-denominator);
						
						// set a threshold for p1 to check if the relationship is positive
						if(p1 > 0.4) {
							class_LLR.put(term, LLR);
							
							// if p1 > 0.4<, record the term and LLR 
							// add all LLR from 13 class into the new_training_vocabulary
							if(!new_training_vocabulary.containsKey(term)) {
								new_training_vocabulary.put(term, LLR);
							} else {
								double old_LLR = new_training_vocabulary.get(term);
								new_training_vocabulary.replace(term, old_LLR+LLR);
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int count = 0; // count vocabulary size
		for(Map.Entry<String, Double> e : new_training_vocabulary.entrySet()) {
			String term = e.getKey();
			double LLR = e.getValue();
			if(LLR>10.83) {
				training_result.append(term+" "+LLR+"\n");
				count++;
			}
		}
		
		u.log("size:"+count);
		u.writeStringToFile("new_training_index.txt", training_result.toString());
		
		// call TrainMultinomialNB to test the algorithm
		classifier.TrainMultinomialNB(new_training_vocabulary, training_doc_id, count);
	}
}
