package main;

public class BiasedGPU {

	public static void main(String[] args) {
		
		boolean expliFlag = true;
		
		//iteration
		int iteration = 100;
		
		//basic info
		int catNum = 23;  //the number of categories
		String luceneIndexPath = "./luceneIndex";
		String  = "";  //the categories file
		String dataRootPath = "";  //root dir of dataset
		String seedwordPath = "";  //seed word file
		
		//parameters
		double gamma = 1;
		double beta0 = 0.01;
		double beta1 = 0.01;
		double gamma0 = (double) 50 / catNum;

		double gamma1 = 0.00000001;
		double p = 1;
		double q = 1;
		double mu = 0.3;
		
		ParaSet paraSet = new ParaSet(expliFlag, iteration, gamma, beta0, beta1, gamma0, gamma1, p, q, mu, catNum, luceneIndexPath);
		
		new LuceneIndexer(paraSet).index(luceneIndexPath, catsFilePath, dataRootPath, seedwordPath);
		
		new Initalizer(paraSet, luceneIndexPath, seedwordPath).initalize();	
		
		new Predictor(paraSet).predict();

		
		Reporter.printTopWords(20, paraSet);
		
	}

}
