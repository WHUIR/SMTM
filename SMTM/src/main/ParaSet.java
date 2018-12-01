package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParaSet {
	
	public boolean expliFlag;
	
	Set<Integer> dicardedWords = new HashSet<Integer>();
	
	//iteration
	int iteration;
		
	//parameters
	public double gamma;
	//public double alpha;
	public double beta0;
	public double beta1;
	public double gamma0;
	public double gamma1;
	public double p;
	public double q;
	public double mu;

	public int catNum;  //number of topics
	
	//statistics
	public double[][] Nc_d;
	public double[] N_d;
	public double[][] Nw_c;
	public double[] N_c;
	public double N_1;
	public double[] Nw_0;
	public double N_0;
	public double[][] alpha;
	public double[] alphaSum;
	public double[] sumNc_d;
	
	//eta
	public double[][] eta;
	
	//prom
	public double[][] prom;

	
	public int docNum;  //number of documents
	
	//documents
	public MDocument[] documents;  //[document] the documents
	
	//vocabulary
	public Vocabulary vocabulary;
	
	//seed word number for each document
	public int[][] seedNum;  //[document][category];
	
	
	//////////////////
	public String luceneIndexPath;
	
	//category to id map
	Map<String, Integer> cat2IdMap;
	Map<Integer, String> id2CatMap;
	
	//file list
	List<FileInfo> fileList;
	
	public ParaSet(boolean expliFlag, int iteration, double gamma, double beta0, double beta1, 
			double gamma0, double gamma1, double p, double q, double miu, int catNum, String luceneIndexPath) {
		this.expliFlag = expliFlag;
		this.iteration = iteration;
		this.gamma = gamma;
		//this.alpha = alpha;
		this.beta0 = beta0;
		this.beta1 = beta1;
		this.gamma0 = gamma0;
		this.gamma1 = gamma1;
		this.p = p;
		this.q = q;
		this.mu = miu;
		this.catNum = catNum;
		this.luceneIndexPath = luceneIndexPath;
		
		cat2IdMap = new HashMap<String, Integer>();
		id2CatMap = new HashMap<Integer, String>();
	}
	

}
