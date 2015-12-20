import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Classifier {

	static int class_num = 13;
	static int class_doc_num = 15;
	static int N = class_num * class_doc_num;
	double[] prior = new double[class_num];
	Utilities u = new Utilities();
	
	/* Calculate condprob for every term in every class */
	public void TrainMultinomialNB(Map<String, Double> vocabulary, Map<String, Integer> training_doc_id, int V) {
		Map<String, double[]> condprob = new TreeMap<String, double[]>();
		
		for(int i=0; i<class_num; i++) {
			prior[i] = (double)class_doc_num/N; // prior probability for every class
			Map<String, Integer> term_tf = new TreeMap<String, Integer>();
			int class_tf_total = 0; //count number of words for a class
			
			try {
				Scanner scanner = new Scanner(new FileReader("class_terms/class_"+(i+1)+".txt"));
				while(scanner.hasNextLine()) {
					String[] columns = scanner.nextLine().split(" ");
					String term = columns[0];
					int tf = Integer.parseInt(columns[1]);
					
					// check if term is in V
					// save term if it is in V
					if(vocabulary.containsKey(term)) { 
						term_tf.put(term, tf);
						class_tf_total = class_tf_total+tf;
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// scan vocabulary for class loop(i)
			for(Map.Entry<String, Double> e : vocabulary.entrySet()) {
				String term = e.getKey();
				double[] p = new double[class_num];
				
				// remember smoothing(+1)
				if(term_tf.containsKey(term)) {
					int tf = term_tf.get(term);
					
					if(!condprob.containsKey(term)) {
						p[i] = (double)(tf+1)/(class_tf_total+V);
						condprob.put(term, p);
					} else {
						p = condprob.get(term);
						p[i] = (double)(tf+1)/(class_tf_total+V);
						condprob.replace(term, p);
					}
				} else { // calculate p[i] even if the term never show up in the class_i
					if(!condprob.containsKey(term)) {
						p[i] = (double)(1)/(class_tf_total+V);
						condprob.put(term, p);
					} else {
						p = condprob.get(term);
						p[i] = (double)(1)/(class_tf_total+V);
						condprob.replace(term, p);
					}
				}
			}
		}
		ApplyMultinomialNB(condprob, training_doc_id);
	}
	
	
	/* Test the Multinominal NB */
	private void ApplyMultinomialNB(Map<String, double[]> condprob, Map<String, Integer> training_doc_id) {
		Map<Integer, Integer> testing_result = new TreeMap<Integer, Integer>();
		StringBuilder result = new StringBuilder();
		
		for(int i=1; i<=1095; i++) {
			// identify if a document belongs to training data
			if(!training_doc_id.containsKey(Integer.toString(i))) {
				
				double[] score = new double[class_num];
				for(int j=0; j<class_num; j++) {
					score[j] = score[j]+Math.log(prior[j]); // prior score
				}
				
				try {
					Scanner scanner = new Scanner(new FileReader("document_terms/"+i+"_terms.txt"));
					while(scanner.hasNextLine()) {
						String[] columns = scanner.nextLine().split(" ");
						String term = columns[0];
						int tf = Integer.parseInt(columns[1]);
						
						// calculate document's score for every class
						if(condprob.containsKey(term)) {
							for(int j=0; j<class_num; j++) {
								double[] p = condprob.get(term);
								score[j] = score[j]+tf*Math.log(p[j]);
							}
						}
					}
					
					// find the max score to identify the probably class for the document
					Double[] max = new Double[2]; // {max_score, doc_id}
					for(int j=0; j<class_num; j++) {
						if(j == 0) {
							max[0] = score[j];
							max[1] = (double)j;
						}
						
						if(score[j]>max[0]) {
							max[0] = score[j];
							max[1] = (double)j;
						}
					}
					
					testing_result.put(i, max[1].intValue()+1); // {doc_id, class}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// output
		result.append("doc_id \t class_id \n");
		for(Map.Entry<Integer, Integer> e : testing_result.entrySet()) {
			int doc_id = e.getKey();
			int class_label = e.getValue();
			result.append(doc_id+" \t "+class_label+"\n");
		}
		u.writeStringToFile("output.txt", result.toString());
	}
}
