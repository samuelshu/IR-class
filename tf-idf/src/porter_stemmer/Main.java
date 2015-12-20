package porter_stemmer;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long time1, time2;
		time1 = System.currentTimeMillis();
		
		GetFileTerms getFileTerms = new GetFileTerms();
		getFileTerms.getFileTerms();
		getFileTerms.getUnitTFIDFVector();
		
		Cosine cos = new Cosine();
		Scanner scanner = new Scanner(System.in);	
		
		time2 = System.currentTimeMillis();
		System.out.println("執行時間：" + ((time2-time1)/1000));
		
		while(true) {
			System.out.printf("請輸入要比較的document（以空白鍵隔開）：\n");
			int a = scanner.nextInt();
			int b = scanner.nextInt();
			float sim = cos.cosine("unit_tfidf/Doc"+a+".txt", "unit_tfidf/Doc"+b+".txt");
			System.out.printf("Sim(%d, %d) = %f \n", a, b, sim);
		}
	}

}
