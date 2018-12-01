package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.special.Gamma;

import mloss.roc.Curve;

public class Reporter {
	
	public static double score(int d, int c, ParaSet paraSet) {
		double[] alpha = new double[2];
		
		alpha[0] = Math.log(paraSet.catNum - 1 - paraSet.alphaSum[d] + paraSet.q);
		//alpha[0] = Math.log(1);
		alpha[0] += Gamma.logGamma(paraSet.gamma0 + paraSet.gamma1);
		alpha[0] += Gamma.logGamma(paraSet.N_d[d] - paraSet.Nc_d[d][c] + paraSet.alphaSum[d] * paraSet.gamma0 + paraSet.gamma0 +
					paraSet.catNum * paraSet.gamma1);
		alpha[0] += Gamma.logGamma(paraSet.alphaSum[d] * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1);
		
		//alpha[1] = Math.log(1);
		alpha[1] = Math.log(paraSet.alphaSum[d] + paraSet.p);
		alpha[1] += Gamma.logGamma(paraSet.Nc_d[d][c] + paraSet.gamma0 + paraSet.gamma1);
		alpha[1] += Gamma.logGamma(paraSet.N_d[d] - paraSet.Nc_d[d][c] + paraSet.alphaSum[d] * paraSet.gamma0 +
				paraSet.catNum * paraSet.gamma1);
		alpha[1] += Gamma.logGamma(paraSet.alphaSum[d] * paraSet.gamma0 + paraSet.gamma0 + paraSet.catNum * paraSet.gamma1);
		
		double max = alpha[0];
		for (double v : alpha)
            if (v > max)
                max = d;
        for (int i = 0; i < alpha.length; i++)
            alpha[i] = Math.pow(Math.E, alpha[i] - max);
        
        return alpha[1] / alpha[0] + alpha[1];
	}
    
	public static void threshPDoc(ParaSet paraSet) {
		
		List<Integer> labelNums = new ArrayList<Integer>();
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			int counter = 0;
			if (doc.hasSeed && ! doc.check) {
				for (int c=0; c<paraSet.catNum; c++) {
					if (doc.seedIndicator[c]) {
						counter++;
					}
				}
				labelNums.add(counter);
				Collections.sort(labelNums);
			}
		}
		
		int topN = labelNums.get(labelNums.size()/2 - labelNums.size() / paraSet.catNum);
		
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			if (doc.check) {
				for (int c=0; c<paraSet.catNum; c++) {
					doc.prediction[c] = false;
				}
				int[] ranking = rank(paraSet.Nc_d[d], paraSet.catNum);
				for (int i=0; i<topN; i++) {
					doc.prediction[ranking[i]] = true;
				}
			}
		}
	}
	
	public static void threshPDoc2(ParaSet paraSet) {
		
		List<Integer> labelNums = new ArrayList<Integer>();
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			int counter = 0;
			if (! doc.check) {
				for (int c=0; c<paraSet.catNum; c++) {
					if (doc.groundTruth[c]) {
						counter++;
					}
				}
				labelNums.add(counter);
				Collections.sort(labelNums);
			}
		}
		
		int topN = labelNums.get(labelNums.size()/2 - labelNums.size() / paraSet.catNum);
		
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			if (doc.check) {
				for (int c=0; c<paraSet.catNum; c++) {
					doc.prediction[c] = false;
				}
				int[] ranking = rank(paraSet.Nc_d[d], paraSet.catNum);
				for (int i=0; i<topN; i++) {
					doc.prediction[ranking[i]] = true;
				}
			}
		}
	}
	
	public static void threshPLabel2(ParaSet paraSet) {
		int checkDocNum = 0;
		for (int d=0; d<paraSet.docNum; d++) {
    		if (!paraSet.documents[d].check)
    			continue;
    		checkDocNum++;
    		for (int c=0; c<paraSet.catNum; c++) {
    			paraSet.documents[d].prediction[c] = false;
    		}
    	}
		
		int pseudoDocNum = 0;
		int[] pseudoLabelNum = new int[paraSet.catNum];
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			if (!doc.check) {
				pseudoDocNum++;
				for (int c=0; c<paraSet.catNum; c++) {
					if (doc.groundTruth[c]) {
						pseudoLabelNum[c]++;
					}
				}
			}
		}
		
		for (int c=0; c<paraSet.catNum; c++) {
			int checkLabelNum = (int) ((double) pseudoLabelNum[c] / pseudoDocNum * checkDocNum);
			System.out.println(paraSet.id2CatMap.get(c) + "\t " + checkLabelNum);
			double[] scores = new double[checkDocNum];
			
			int i = 0;
			Map<Integer, Integer> i2d = new HashMap<Integer, Integer>();
			for (int d=0; d<paraSet.docNum; d++) {
	    		if (!paraSet.documents[d].check)
	    			continue;
	    		scores[i] = (paraSet.Nc_d[d][c] + paraSet.gamma0) / (paraSet.N_d[d] + paraSet.catNum * paraSet.gamma0);
	    		i2d.put(i, d);
	    		i++;
	    	}
			
			int[] ranking = rank(scores, checkDocNum);
			for (i=0; i<checkLabelNum; i++) {
				MDocument doc = paraSet.documents[i2d.get(ranking[i])];
				doc.prediction[c] = true;
			}
			
		}
		//System.out.println(pseudoDocNum);
		
	}
	
	public static void threshTopK(ParaSet paraSet) {
		
		double[][] tao = new double[paraSet.vocabulary.wordNum][paraSet.catNum];
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		double sum = 0;
    		for (int c=0; c<paraSet.catNum; c++) {
    			tao[w][c] = (paraSet.Nw_c[c][w] + paraSet.beta1)
				/ (paraSet.N_c[c] + paraSet.vocabulary.wordNum * paraSet.beta1);
    			sum += tao[w][c];
    		}
    		for (int c=0; c<paraSet.catNum;c ++) {
    			tao[w][c] /= sum;
    			//System.out.println(tao[w][c] + "\t" + sum);
    		}
    	}
    	
    	double[][] tao2 = new double[paraSet.docNum][paraSet.catNum];
    	for (int d=0; d<paraSet.docNum; d++) {
    		for (int c=0; c<paraSet.catNum; c++) {
    			for (int w=0; w<paraSet.documents[d].docLength; w++) {
        			int wordNo = paraSet.documents[d].words.get(w);
        			tao2[d][c] += tao[wordNo][c];
        		}
        		tao2[d][c] /= paraSet.documents[d].docLength;
    		}
    		
    	}
		
		
		
		int opt_num = 0;
		double maxF1 = 0;
		
		for (int j=0; j<50; j++) {
			int checkDocNum = 0;
			for (int d=0; d<paraSet.docNum; d++) {
	    		if (!paraSet.documents[d].check)
	    			continue;
	    		checkDocNum++;
	    		for (int c=0; c<paraSet.catNum; c++) {
	    			paraSet.documents[d].prediction[c] = false;
	    		}
	    	}
			
			int checkLabelNum = 50 * j + 10;
			for (int c=0; c<paraSet.catNum; c++) {
				double[] scores = new double[checkDocNum];
				
				int i = 0;
				Map<Integer, Integer> i2d = new HashMap<Integer, Integer>();
				for (int d=0; d<paraSet.docNum; d++) {
		    		if (!paraSet.documents[d].check)
		    			continue;
		    		scores[i] =tao2[d][c];
		    		i2d.put(i, d);
		    		i++;
		    	}
				
				int[] ranking = rank(scores, checkDocNum);
				for (i=0; i<checkLabelNum; i++) {
					MDocument doc = paraSet.documents[i2d.get(ranking[i])];
					doc.prediction[c] = true;
				}
				
			}
			
			double f1 = 0;
			
			for (int c=0; c<paraSet.catNum; c++) {
				int tp = 0;
	        	int fp = 0;
	        	int fn = 0;
	        	for (int d=0; d<paraSet.docNum; d++) {
	 
	        		MDocument doc = paraSet.documents[d];
	        		if (!doc.check) {
		    			continue;
		    		}
	        		
	        		if (doc.groundTruth[c] && doc.prediction[c]) {
	        			//System.out.println("in");
		        		tp++;
		        	}
		        	else if (!doc.groundTruth[c] && doc.prediction[c]) {
		        		fp++;
		        	}
		        	else if (doc.groundTruth[c] && !doc.prediction[c]) {
		        		fn++;
		        	}
	        	}
	        	
	        	f1 += (double)2 * tp / (2 * tp + fp + fn);
			}
			f1 /= paraSet.catNum;
			//System.out.println(checkLabelNum + " " + f1);
			if (f1 > maxF1) {
				maxF1 = f1;
				opt_num = checkLabelNum;
			}
			
		}
		
		
		System.out.println(opt_num);
		
		int checkDocNum = 0;
		for (int d=0; d<paraSet.docNum; d++) {
    		if (!paraSet.documents[d].check)
    			continue;
    		checkDocNum++;
    		for (int c=0; c<paraSet.catNum; c++) {
    			paraSet.documents[d].prediction[c] = false;
    		}
    	}
		
		for (int c=0; c<paraSet.catNum; c++) {
			double[] scores = new double[checkDocNum];
			
			int i = 0;
			Map<Integer, Integer> i2d = new HashMap<Integer, Integer>();
			for (int d=0; d<paraSet.docNum; d++) {
	    		if (!paraSet.documents[d].check)
	    			continue;
	    		scores[i] = tao2[d][c];
	    		i2d.put(i, d);
	    		i++;
	    	}
			
			int[] ranking = rank(scores, checkDocNum);
			for (i=0; i<opt_num; i++) {
				MDocument doc = paraSet.documents[i2d.get(ranking[i])];
				doc.prediction[c] = true;
			}
			
		}
		
	}
	
	public static void threshOne(ParaSet paraSet) {
		double[][] tao = new double[paraSet.vocabulary.wordNum][paraSet.catNum];
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		double sum = 0;
    		for (int c=0; c<paraSet.catNum; c++) {
    			tao[w][c] = (paraSet.Nw_c[c][w] + paraSet.beta1)
				/ (paraSet.N_c[c] + paraSet.vocabulary.wordNum * paraSet.beta1);
    			sum += tao[w][c];
    		}
    		for (int c=0; c<paraSet.catNum;c ++) {
    			tao[w][c] /= sum;
    			//System.out.println(tao[w][c] + "\t" + sum);
    		}
    	}
    	
    	double[][] tao2 = new double[paraSet.docNum][paraSet.catNum];
    	for (int d=0; d<paraSet.docNum; d++) {
    		for (int c=0; c<paraSet.catNum; c++) {
    			for (int w=0; w<paraSet.documents[d].docLength; w++) {
        			int wordNo = paraSet.documents[d].words.get(w);
        			tao2[d][c] += tao[wordNo][c];
        		}
        		tao2[d][c] /= paraSet.documents[d].docLength;
    		}
    		
    	}
    	
    	double maxF1 = 0;
    	double op_thr = 0;
    	
    	for (int i=0; i<200; i++) {

			double threshold = -1 + i * 0.01;
			double f1 = 0;
			for (int c=0; c<paraSet.catNum; c++) {
				int tp = 0;
	        	int fp = 0;
	        	int fn = 0;
	        	for (int d=0; d<paraSet.docNum; d++) {
	 
	        		MDocument doc = paraSet.documents[d];
	        		if (doc.check) {
		    			continue;
		    		}
	        		
	        		if (tao2[d][c] > threshold) {
	        		
	        		//if ((paraSet.Nc_d[d][c] + paraSet.gamma0 + paraSet.gamma1) /
	            	//		(paraSet.N_d[d] + paraSet.catNum * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1) > threshold) {
	        			doc.prediction[c] = true;
	        		}
	        		else {
	        			doc.prediction[c] = false;
	        		}
	        		
	        		if (doc.groundTruth[c] && doc.prediction[c]) {
		        		tp++;
		        	}
		        	else if (!doc.groundTruth[c] && doc.prediction[c]) {
		        		fp++;
		        	}
		        	else if (doc.groundTruth[c] && !doc.prediction[c]) {
		        		fn++;
		        	}
	        	}
	        	
	        	f1 += (double)2 * tp / (2 * tp + fp + fn);
			}
			f1 /= paraSet.catNum;
			
			if (f1 > maxF1) {
				maxF1 = f1;
				op_thr = threshold;
			}
    	}	
    	
		for (int c=0; c<paraSet.catNum; c++) {
			
			for (int d=0; d<paraSet.docNum; d++) {
        		MDocument doc = paraSet.documents[d];
        		if (!doc.check) {
	    			continue;
	    		}
        		
        		if (tao2[d][c] > op_thr) {
        			doc.prediction[c] = true;
        		}
        		else {
        			doc.prediction[c] = false;
        		}
			}
			
			
		}
	}
	
	public static void thresh(ParaSet paraSet) {
		
		double[][] tao = new double[paraSet.vocabulary.wordNum][paraSet.catNum];
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		double sum = 0;
    		for (int c=0; c<paraSet.catNum; c++) {
    			tao[w][c] = (paraSet.Nw_c[c][w] + paraSet.beta1)
				/ (paraSet.N_c[c] + paraSet.vocabulary.wordNum * paraSet.beta1);
    			sum += tao[w][c];
    		}
    		for (int c=0; c<paraSet.catNum;c ++) {
    			tao[w][c] /= sum;
    			//System.out.println(tao[w][c] + "\t" + sum);
    		}
    	}
    	
    	double[][] tao2 = new double[paraSet.docNum][paraSet.catNum];
    	for (int d=0; d<paraSet.docNum; d++) {
    		for (int c=0; c<paraSet.catNum; c++) {
    			for (int w=0; w<paraSet.documents[d].docLength; w++) {
        			int wordNo = paraSet.documents[d].words.get(w);
        			tao2[d][c] += tao[wordNo][c];
        		}
        		tao2[d][c] /= paraSet.documents[d].docLength;
    		}
    		
    	}
    	
    	
		for (int c=0; c<paraSet.catNum; c++) {
			double maxF1 = 0;
			double op_thr = 0;
			for (int i=0; i<200; i++) {

				double threshold = -1 + i * 0.01;
				int tp = 0;
	        	int fp = 0;
	        	int fn = 0;
	        	for (int d=0; d<paraSet.docNum; d++) {
	 
	        		MDocument doc = paraSet.documents[d];
	        		if (doc.check) {
		    			continue;
		    		}
	        		
	        		if (tao2[d][c] > threshold) {
	        		
	        		//if ((paraSet.Nc_d[d][c] + paraSet.gamma0 + paraSet.gamma1) /
	            	//		(paraSet.N_d[d] + paraSet.catNum * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1) > threshold) {
	        			doc.prediction[c] = true;
	        		}
	        		else {
	        			doc.prediction[c] = false;
	        		}
	        		
	        		if (doc.groundTruth[c] && doc.prediction[c]) {
		        		tp++;
		        	}
		        	else if (!doc.groundTruth[c] && doc.prediction[c]) {
		        		fp++;
		        	}
		        	else if (doc.groundTruth[c] && !doc.prediction[c]) {
		        		fn++;
		        	}
	        	}
				double f1 = (double)2 * tp / (2 * tp + fp + fn);
				//System.out.println("f1" + f1);
				if (f1 > maxF1) {
					maxF1 = f1;
					op_thr = threshold;
				}
			}
			//System.out.println(op_thr);
			for (int d=0; d<paraSet.docNum; d++) {
        		MDocument doc = paraSet.documents[d];
        		if (!doc.check) {
	    			continue;
	    		}
        		
        		if (tao2[d][c] > op_thr) {
        			doc.prediction[c] = true;
        		}
        		else {
        			doc.prediction[c] = false;
        		}
			}
			
			
		}
	}
	
	public static void threshPLabel(ParaSet paraSet) {
		
		double[][] tao = new double[paraSet.vocabulary.wordNum][paraSet.catNum];
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		double sum = 0;
    		for (int c=0; c<paraSet.catNum; c++) {
    			tao[w][c] = (paraSet.Nw_c[c][w] + paraSet.beta1)
				/ (paraSet.N_c[c] + paraSet.vocabulary.wordNum * paraSet.beta1);
    			sum += tao[w][c];
    		}
    		for (int c=0; c<paraSet.catNum;c ++) {
    			tao[w][c] /= sum;
    			//System.out.println(tao[w][c] + "\t" + sum);
    		}
    	}
    	
    	double[][] tao2 = new double[paraSet.docNum][paraSet.catNum];
    	for (int d=0; d<paraSet.docNum; d++) {
    		for (int c=0; c<paraSet.catNum; c++) {
    			for (int w=0; w<paraSet.documents[d].docLength; w++) {
        			int wordNo = paraSet.documents[d].words.get(w);
        			tao2[d][c] += tao[wordNo][c];
        		}
        		tao2[d][c] /= paraSet.documents[d].docLength;
    		}
    		
    	}
    	
		int checkDocNum = 0;
		for (int d=0; d<paraSet.docNum; d++) {
    		if (!paraSet.documents[d].check)
    			continue;
    		checkDocNum++;
    		for (int c=0; c<paraSet.catNum; c++) {
    			paraSet.documents[d].prediction[c] = false;
    		}
    	}
		
		
		int pseudoDocNum = 0;
		int[] pseudoLabelNum = new int[paraSet.catNum];
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			if (doc.hasSeed) {
				pseudoDocNum++;
				for (int c=0; c<paraSet.catNum; c++) {
					if (doc.seedIndicator[c]) {
					//if (doc.groundTruth[c]) {
						pseudoLabelNum[c]++;
					}
				}
			}
		}
		
		for (int c=0; c<paraSet.catNum; c++) {
			int checkLabelNum = (int) ((double) pseudoLabelNum[c] / pseudoDocNum * checkDocNum);
			double[] scores = new double[checkDocNum];
			
			int i = 0;
			Map<Integer, Integer> i2d = new HashMap<Integer, Integer>();
			for (int d=0; d<paraSet.docNum; d++) {
	    		if (!paraSet.documents[d].check)
	    			continue;
	    		scores[i] = tao2[d][c];
	    		i2d.put(i, d);
	    		i++;
	    	}
			
			int[] ranking = rank(scores, checkDocNum);
			for (i=0; i<checkLabelNum; i++) {
				MDocument doc = paraSet.documents[i2d.get(ranking[i])];
				doc.prediction[c] = true;
			}
			
		}
		//System.out.println(pseudoDocNum);
		
	}
	
	
	
	public static void hammingLoss(ParaSet paraSet) {
		threshPDoc(paraSet);
		System.out.println("HammingLoss: \t" + hammingLossCore(paraSet));
	}
	
	public static void fMeasure(ParaSet paraSet) {
		//threshPDoc2(paraSet);
		
		//threshOne(paraSet);
		//thresh(paraSet);
		//threshPLabel(paraSet);
		
		//System.out.println("Macro-F1: \t" + macroF1Report(paraSet));
		
		//thresh(paraSet);
		//threshPLabel(paraSet);
		
		System.out.println("Macro-F1: \t" + macroF1Report(paraSet));
		//System.out.println("Micro-F1: \t" + microF1Report(paraSet));
		
		
	}
	
	public static double macroF1Report(ParaSet paraSet) {

    	double f1 = 0;
		for (int c=0; c<paraSet.catNum; c++) {
			int tp = 0;
        	int fp = 0;
        	int fn = 0;
			for (int d=0; d<paraSet.docNum; d++) {
	    		MDocument doc = paraSet.documents[d];
	    		
	    		if (!doc.check /*paraSet.alphaSum[d] > 1*//* paraSet.documents[d].docLength<10*/) {
	    			continue;
	    		}
	    		
	        	if (doc.groundTruth[c] && doc.prediction[c]) {
	        		tp++;
	        	}
	        	else if (!doc.groundTruth[c] && doc.prediction[c]) {
	        		fp++;
	        	}
	        	else if (doc.groundTruth[c] && !doc.prediction[c]) {
	        		fn++;
	        	}
			}
	    	f1 += (double)2 * tp / (2 * tp + fp + fn);
			//System.out.println(paraSet.id2CatMap.get(c) + "\t" + tp + "\t" + fp + "\t" + fn);
    	}
    	f1 /= paraSet.catNum;
    	return f1;
    	
    }

    public static double microF1Report(ParaSet paraSet) {

    	int tp = 0;
    	int fp = 0;
    	int fn = 0;
    	int counter = 0;
    	for (int c=0; c<paraSet.catNum; c++) {
    		
			for (int d=0; d<paraSet.docNum; d++) {
	    		MDocument doc = paraSet.documents[d];
	    		
	    		/*if (paraSet.alphaSum[d] >5 && doc.docLength > 30) {
	    			for (int w=0; w<doc.docLength; w++) {
	    				System.out.print(doc.cates.get(w) + " ");
	    			}
	    			System.out.print("\n");
	    			//System.out.println(doc.docLength);
	    		}*/
	    		/*if (paraSet.alphaSum[d] == 0) {
	    			for (int c1=0; c1<paraSet.catNum; c1++) {
	    				if (doc.groundTruth[c1]) {
	    					System.out.print(c1 + "&");
	    				}
	    			}
	    			System.out.print(" | ");
	    			for (int w=0; w<doc.docLength; w++) {
	    				System.out.print(doc.cates.get(w) + " ");
	    			}
	    			System.out.print("\n");
	    		}*/
	    		
	    		if (!doc.check/* || paraSet.alphaSum[d] == 0*/) {
	    			continue;
	    		}
	    		
	        	if (doc.groundTruth[c] && doc.prediction[c]) {
	        		tp++;
	        	}
	        	else if (!doc.groundTruth[c] && doc.prediction[c]) {
	        		fp++;
	        	}
	        	else if (doc.groundTruth[c] && !doc.prediction[c]) {
	        		fn++;
	        	}
			}
    	}
    	//System.out.println(tp + "\t" + fp + "\t" + fn);
    	double f1 = (double)2 * tp / (2 * tp + fp + fn);
    	/*for (int d=0; d<paraSet.docNum; d++) {
    		//if (paraSet.alphaSum[d]  == 0) {
    		if (paraSet.documents[d].docLength < 15) {
    			System.out.println(paraSet.documents[d].title);
    		}
    	}*/
    	System.out.println("counter:\t" + counter);
    	System.out.println("docNum:\t" + paraSet.docNum);
    	return f1;
    	
    }
	
	public static double hammingLossCore(ParaSet paraSet) {
		int docCounter = 0;
		double loss = 0;
		for (int d=0; d<paraSet.docNum; d++) {
			MDocument doc = paraSet.documents[d];
			int counter = 0;
			if (doc.check || paraSet.alphaSum[d] == 0) {
				docCounter++;
				for (int c=0; c<paraSet.catNum; c++) {
					if (doc.prediction[c] != doc.groundTruth[c]) {
						counter++;
					}
				}
				loss += (double) counter / paraSet.catNum;
			}
		}
		return loss /= docCounter;
	}
    
    public static void macroAucReport(ParaSet paraSet) {
    	double[][] tao = new double[paraSet.vocabulary.wordNum][paraSet.catNum];
    	for (int w=0; w<paraSet.vocabulary.wordNum; w++) {
    		double sum = 0;
    		for (int c=0; c<paraSet.catNum; c++) {
    			tao[w][c] = (paraSet.Nw_c[c][w] + paraSet.beta1)
				/ (paraSet.N_c[c] + paraSet.vocabulary.wordNum * paraSet.beta1);
    			sum += tao[w][c];
    		}
    		for (int c=0; c<paraSet.catNum;c ++) {
    			tao[w][c] /= sum;
    			//System.out.println(tao[w][c] + "\t" + sum);
    		}
    	}
    	ArrayList<Double>[] scores = new ArrayList[paraSet.catNum];
    	for (int c=0; c<paraSet.catNum; c++) {
    		scores[c] = new ArrayList<Double>();
    	}
    	
    	for (int d=0; d<paraSet.docNum; d++) {
    		if (!paraSet.documents[d].check || paraSet.alphaSum[d] == 0)
    			continue;
    		for (int c=0; c<paraSet.catNum; c++) {
    			double score = 0;
    			for (int w=0; w<paraSet.documents[d].docLength; w++) {
    				int wordNo = paraSet.documents[d].words.get(w);
    				score += tao[wordNo][c];
    			}
    			score /= paraSet.documents[d].docLength;
    			//System.out.println(score);
    			scores[c].add(score);
    			/*scores[c].add((paraSet.Nc_d[d][c] + paraSet.gamma0 + paraSet.gamma1) /
    			(paraSet.N_d[d] + paraSet.catNum * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1));*/
    		}
    	}
    	
    	
    	double auc = 0;
    	for (int c=0; c<paraSet.catNum; c++) {
    		ArrayList<Integer> trueLabels = new ArrayList<Integer>();
    		for (int d=0; d<paraSet.docNum; d++) {
    			MDocument doc = paraSet.documents[d];
    			if (!doc.check ||  paraSet.alphaSum[d] == 0)
        			continue;
    			if (doc.groundTruth[c])
    				trueLabels.add(1);
    			else
    				trueLabels.add(0);
    		}
    		Curve analysis = new Curve.PrimitivesBuilder()
    			    .scores(scores[c])
    			    .labels(trueLabels)
    			    .build();
    		double area = analysis.rocArea();
    		auc += area;
    		area = analysis.prArea();
    	}
    	
    	auc /= paraSet.catNum;
    	System.out.println("macro AUC: \t" + auc);
    }
    
    public static void microAucReport(ParaSet paraSet) {
    	ArrayList<Double> scores = new ArrayList<Double>();
    	ArrayList<Integer> trueLabels = new ArrayList<Integer>();
    	
    	for (int d=0; d<paraSet.docNum; d++) {
    		MDocument doc = paraSet.documents[d];
    		if (!doc.check)
    			continue;
    		for (int c=0; c<paraSet.catNum; c++) {
    			//scores.add((paraSet.Nc_d[d][c] + paraSet.gamma0) / 
				//		(paraSet.N_d[d] + paraSet.catNum * paraSet.gamma0));
    			scores.add(score(d, c, paraSet));
    			if (doc.groundTruth[c])
    				trueLabels.add(1);
    			else
    				trueLabels.add(0);
    		}
    	}
    	
    	Curve analysis = new Curve.PrimitivesBuilder()
			    .scores(scores)
			    .labels(trueLabels)
			    .build();
		double auc = analysis.rocArea();


    	System.out.println("micro AUC:\t" + auc);
    }
    
    public static void oneError(ParaSet paraSet) {
    	double oneError = 0;
    	int counter = 0;
    	for (int d=0; d<paraSet.docNum; d++) {
    		MDocument doc = paraSet.documents[d];
    		if (!doc.check)
    			continue;
    		counter++;
    		double max = 0;
    		int maxC = -1;
    		for (int c=0; c<paraSet.catNum; c++) {
    			double score = (paraSet.Nc_d[d][c] + paraSet.gamma0);
    			if (score >= max) {
    				max = score;
    				maxC = c;
    			}
    		}
    		    		
    		if (!paraSet.documents[d].groundTruth[maxC]) {
    			oneError++;
    		}
    	}
    	oneError /= counter;
    	
    	System.out.println("OneError:\t" + oneError);
    }
    
    public static void coverage(ParaSet paraSet) {

    	double coverage = 0;
    	int counter = 0;
    	for (int d=0; d<paraSet.docNum; d++) {
    		MDocument doc = paraSet.documents[d];
    		if (!doc.check)
    			continue;
    		counter++;
    		
    		int[] ranking = rank(paraSet.Nc_d[d], paraSet.catNum);
    		Set<Integer> relSet = new HashSet<Integer>();

    		for (int c=0; c<paraSet.catNum; c++) {
    			if (doc.groundTruth[c]) {
    				relSet.add(c);
    			}
    		}
    		for (int i=0; i<paraSet.catNum; i++) {
    			coverage++;
    			if (relSet.contains(ranking[i])) {
    				relSet.remove(ranking[i]);
    			}
    			if (relSet.isEmpty()) {
    				break;
    			}
    		}
    		
    	}
    	coverage /= counter;
    	
    	System.out.println("Coverage:\t" + coverage);
    }
    
    public static void rankLoss(ParaSet paraSet) {
    	double rankLoss = 0;
    	int counter = 0;
    	for (int d=0; d<paraSet.docNum; d++) {
    		MDocument doc = paraSet.documents[d];
    		if (!doc.check)
    			continue;
    		counter++;
    		Set<Integer> relSet = new HashSet<Integer>();
    		Set<Integer> irelSet = new HashSet<Integer>();
    		double lossCounter = 0;
    		for (int c=0; c<paraSet.catNum; c++) {
    			if (doc.groundTruth[c]) {
    				relSet.add(c);
    			}
    			else {
    				irelSet.add(c);
    			}
    		}
    		
    		for (int c1 : relSet) {
    			for (int c2 : irelSet) {
    				if (paraSet.Nc_d[d][c1] < paraSet.Nc_d[d][c2]) {
    					lossCounter++;
    				}
    				else if (paraSet.Nc_d[d][c1] == paraSet.Nc_d[d][c2] && c1 > c2) {
    					lossCounter++;
    				}
    			}
    		}
    		if (relSet.size() == 0 || irelSet.size() == 0)
    			continue;
    		rankLoss += (lossCounter / relSet.size() / irelSet.size());
    		
    	}
    	rankLoss /= counter;
    	
    	System.out.println("RankLoss:\t" + rankLoss);
    }
    
    private static int[] rank(double raw[], int num) {
    	double[] ori = new double[num];
    	for (int i=0; i<num; i++) {
    		ori[i] = raw[i];
    	}
    	int[] ranking = new int[num];
    	for (int i=0; i<num; i++) {
    		ranking[i] = i;
    	}
    	
    	for (int i=0; i<num - 1; i++) {
    		for (int j=i+1; j<num; j++) {
    			if (ori[j] > ori[i]) {
    				double tmp = ori[i];
    				ori[i] = ori[j];
    				ori[j] = tmp;
    				
    				int tmp2;
    				tmp2 = ranking[i];
    				ranking[i] = ranking[j];
    				ranking[j] = tmp2;
    			}
    		}
    	}
    	
    	return ranking;
    }
    
    public static void printTopWords(int num, ParaSet paraSet) {
    	for (int c=0; c<paraSet.catNum; c++) {
    		System.out.print( paraSet.id2CatMap.get(c) + " |");
    		int[] ranking = rank(paraSet.Nw_c[c], paraSet.vocabulary.wordNum);
    		for (int i=0; i<num; i++) {
    			System.out.print( " " + paraSet.vocabulary.toWord(ranking[i]));
    		}
    		
    		System.out.print("\n");
    		//System.out.println(paraSet.N_c[c]);
    	}
    	//System.out.println(paraSet.N_0);
    }
    
}
