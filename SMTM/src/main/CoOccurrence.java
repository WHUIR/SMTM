package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoOccurrence {

    private ParaSet paraSet;
    private Map<Integer, int[]> DFws;
    private Map<Integer, Integer> DFs;
    private Map<Integer, double[]> Pws;
    private double[][] RELwc;
    private double[][] Vwc;
    
    public CoOccurrence(ParaSet paraSet) {
    	this.paraSet = paraSet;
    	this.DFws = new HashMap<Integer, int[]>();
    	this.DFs = new HashMap<Integer, Integer>();
    	this.Pws = new HashMap<Integer, double[]>();
    	this.RELwc = new double[paraSet.catNum][paraSet.vocabulary.wordNum];
    	this.Vwc = new double[paraSet.catNum][paraSet.vocabulary.wordNum];
    	
    	computeDF();
    	computePws();
    	computeRELwc();
    	computeVwc();

    	computeEta();
    	
    }
    
    
    private void computeDF() {
    	
    	for (int d=0; d<paraSet.docNum; d++) {
    		Set<Integer> handledSeedwords = new HashSet<Integer>();
    		Set<Integer> wordSet = new HashSet<Integer>();
    		
    		for (int i=0; i<paraSet.documents[d].docLength; i++) {
    			wordSet.add(paraSet.documents[d].words.get(i));
    		}

    		for (int i=0; i<paraSet.documents[d].docLength; i++) {
    			int seedNo = paraSet.documents[d].words.get(i);
    			if (paraSet.vocabulary.isSeedword(seedNo) && !handledSeedwords.contains(seedNo)) {
    				int[] wordNums;
    				if(DFws.containsKey(seedNo)) {
    					wordNums = DFws.get(seedNo);
    				}
    				else {
    					wordNums = new int[paraSet.vocabulary.wordNum];
    					DFws.put(seedNo, wordNums);
    				}
    				if (DFs.containsKey(seedNo)) {
    					DFs.put(seedNo, DFs.get(seedNo) + 1);
    				}
    				else {
    					DFs.put(seedNo, 1);
    				}
    				for (int wordNo : wordSet) {
    	    			wordNums[wordNo]++;
    				}
    				handledSeedwords.add(seedNo);
    			}
    		}
    	}
    }
    
    private void computePws() {
    	Set<Integer> seedIdSet = paraSet.vocabulary.getSeedIdSet();
    	for (int seed : seedIdSet) {
    		if(!DFs.containsKey(seed)) {
    			continue;
    		}
    		double[] Pw = new double[paraSet.vocabulary.wordNum];
    		int[] dfws = DFws.get(seed);
    		int dfs = DFs.get(seed);
    		for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    			Pw[w] = (double) dfws[w] / dfs;
    		}
    		Pws.put(seed, Pw);
    	}
    }
    
    private void computeRELwc() {
    	Set<Integer>[] seedTopicIdSet = paraSet.vocabulary.getSeedTopicIdSet();
    	for (int t=0; t<paraSet.catNum; t++) {
    		for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    			double sum=0;
    			int counter = 0;
    			for (int seed : seedTopicIdSet[t]) {
    				if(!Pws.containsKey(seed)) {
    	    			continue;
    	    		}
    				counter++;
    				sum += Pws.get(seed)[w];
    			}
    			RELwc[t][w] = sum;
    			if (counter > 0) {
    				RELwc[t][w] = sum / counter;
    			}
    			else {
    				RELwc[t][w] = 0;
    			}
    			
    		}
    	}
    }
    
    private void computeVwc() {
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {   		
    		double max = 0;
    		double sum = 0;
    		for (int t=0; t<paraSet.catNum; t++) {
    			Vwc[t][w] = RELwc[t][w];
    
    			if (Vwc[t][w] > max)
    				max = Vwc[t][w];
    			
    			sum += Vwc[t][w];
    		}
    		
    		for (int t=0; t<paraSet.catNum; t++) {
    			/*if (max == 0) {
    				Vwc[t][w] = 0.5;
    			}
    			else {
    				Vwc[t][w] /= max;
    			}*/
    			
    			if (sum ==0) {
    				Vwc[t][w] = (double) 1 / paraSet.catNum;
    			}
    			else {
    				Vwc[t][w] /= sum;
    			}
    			if(Vwc[t][w] <= 0.01) {
    				Vwc[t][w] = 0.01;
    			}

    		}
    		
    	}
    }
    
    
    void computeEta() {
    	double[] sum = new double[paraSet.catNum];
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		for (int c=0; c<paraSet.catNum; c++) {
    			paraSet.eta[w][c] = Vwc[c][w];
    			sum[c] += paraSet.eta[w][c];
    		}
    	}
    	
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		for (int c=0; c<paraSet.catNum; c++) {
    			paraSet.eta[w][c] *= paraSet.vocabulary.wordNum / sum[c];
    			//paraSet.eta[w][c] = 1;
    		}
    	}
    	
    }

}
