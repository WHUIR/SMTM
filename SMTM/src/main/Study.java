package main;

public class Study {

	/*ParaSet paraSet;
	
	public Study(ParaSet paraSet) {
		this.paraSet = paraSet;
	}
	
	public static void printDoc(ParaSet paraSet, WDocument doc) {
		double tao0Sum = 0;
		for (int i=0; i<doc.length; i++) {
			int wordNo = doc.words.get(i);
			int wordFre = doc.wordFres.get(i);
			tao0Sum += paraSet.tao[wordNo][0] * wordFre;
		}
		double tao1Sum = 0;
		for (int i=0; i<doc.length; i++) {
			int wordNo = doc.words.get(i);
			int wordFre = doc.wordFres.get(i);
			tao1Sum += paraSet.tao[wordNo][1] * wordFre;
		}
		double t0RationT1 = tao0Sum / tao1Sum;
		double t1RationT0 = tao1Sum / tao0Sum;
		System.out.println(doc.title + "\t" + doc.groundTruth + "\t" + doc.topic
				+ "\t" + tao0Sum + "\t" + tao1Sum + "\t" + t0RationT1 + "\t" + t1RationT0);
	}*/
	
	/*public void printResults() {
		System.out.println("--------------------------------------------------------");
		double counter1 = 0;
		double counter2 = 0;
		double counter3 = 0;
		for (WDocument doc : paraSet.documents) {
			double tao0Sum = 0;
			for (int word : doc.words) {
				tao0Sum += paraSet.tao[word][0];
			}
			double tao1Sum = 0;
			for (int word : doc.words) {
				tao1Sum += paraSet.tao[word][1];
			}
			double t0RationT1 = tao0Sum / tao1Sum;
			double t1RationT0 = tao1Sum / tao0Sum;
			
			/*int r = 5;
			
			if (doc.groundTruth!=doc.topic && (t0RationT1>r || t1RationT0 > r)) {
				if ((doc.groundTruth==0 && t0RationT1>r) || (doc.groundTruth==1 && t1RationT0>r)) {
					counter1++;
				}
				counter2++;
				System.out.println(doc.title + "\t" + doc.groundTruth + "\t" + doc.topic
						+ "\t" + tao0Sum + "\t" + tao1Sum + "\t" + t0RationT1 + "\t" + t1RationT0);
			}*/
			
			/*int r = 4;
			if(doc.groundTruth!=doc.topic) {
				counter3++;
			}
			
			if (t1RationT0>r) {
				counter2++;
				if(doc.groundTruth==0 && doc.topic == 1) {
					System.out.println(doc.title + "\t" + doc.groundTruth + "\t" + doc.topic
							+ "\t" + tao0Sum + "\t" + tao1Sum + "\t" + t0RationT1 + "\t" + t1RationT0);
					counter1++;
				}
			}
			
			
		}
		System.out.print((counter1 / counter2) + "\t" + counter1+"/"+counter2 + "\t" + counter3);
	}*/
}
