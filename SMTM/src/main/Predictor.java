package main;

import org.apache.commons.math3.special.Gamma;

import Util.MathUtil;

public class Predictor {

	private ParaSet paraSet;
	boolean flag = true;
	boolean seedImpactFlag = true;
	boolean sparseFlag = true;
	boolean directFlag = false;
	
	
	public Predictor(ParaSet paraSet) {
		this.paraSet = paraSet;
	}
	
	public void predict() {
		
		initSamples();
		//Reporter.macroReport(paraSet);
		
		itretate();
	}
	private void initSamples() {
		for(int d = 0; d < paraSet.docNum; d++) {
			MDocument document = paraSet.documents[d];
			
			
			for (int w=0; w<document.docLength; w++) {
				int wordNo = document.words.get(w);
				
				int c = (int) (Math.random() * (paraSet.catNum + 1));

				if (c <paraSet.catNum) {
					document.xs.set(w, 1);
					document.cates.set(w, c);
					if (seedImpactFlag) {
						paraSet.Nc_d[d][c] += paraSet.prom[d][c];
						paraSet.N_d[d] += paraSet.prom[d][c];
						paraSet.sumNc_d[d] += paraSet.prom[d][c];
					}
					else {
						paraSet.Nc_d[d][c]++;
						paraSet.N_d[d]++;
						paraSet.sumNc_d[d]++;
					}
					if (flag) {
						paraSet.Nw_c[c][wordNo] += paraSet.eta[wordNo][c];
						paraSet.N_c[c] += paraSet.eta[wordNo][c];
					}
					else {
						paraSet.Nw_c[c][wordNo]++;
						paraSet.N_c[c]++;
					}
					
					paraSet.N_1++;
					//paraSet.N_1 +=  paraSet.prom[d][c];
					//System.out.println(paraSet.prom[d][c]);
				}
				else {
					document.xs.set(w, 0);
					document.cates.set(w, -1);
					paraSet.Nw_0[wordNo]++;
					paraSet.N_0++;
				}
				
			}
		}
		
	}
	

	private void itretate() {
		for (int it=0; it<paraSet.iteration; it++) {
			
			System.out.print(it + ":\t");
			
			for(int d = 0; d < paraSet.docNum; d++){
				MDocument document = paraSet.documents[d];
				

				for(int w = 0; w < document.docLength; w++){
					int wordNo = document.words.get(w);
					int c = document.cates.get(w);
					int x = document.xs.get(w);
					
					if (x == 1) {
						if (seedImpactFlag) {
							paraSet.Nc_d[d][c] -= paraSet.prom[d][c];
							paraSet.N_d[d] -= paraSet.prom[d][c];
							if (paraSet.alpha[d][c] == 1)
								paraSet.sumNc_d[d] -= paraSet.prom[d][c];
						}
						else {
							paraSet.Nc_d[d][c]--;
							paraSet.N_d[d]--;
							if (paraSet.alpha[d][c] == 1)
								paraSet.sumNc_d[d]--;
						}
						if (flag) {
							paraSet.Nw_c[c][wordNo] -= paraSet.eta[wordNo][c];
							paraSet.N_c[c] -= paraSet.eta[wordNo][c];
						}
						else {
							paraSet.Nw_c[c][wordNo]--;
							paraSet.N_c[c]--;
						}
						paraSet.N_1--;
						//paraSet.N_1 -=  paraSet.prom[d][c];
					}
					else {
						paraSet.Nw_0[wordNo]--;
						paraSet.N_0--;
					}

					
					c = sampleC(d, wordNo);
					if (c == paraSet.catNum) {
						x = 0;
					}
					else {
						x = 1;
					}
					
					if (x == 1) {
						document.xs.set(w, 1);
						document.cates.set(w, c);
						if (seedImpactFlag) {
							paraSet.Nc_d[d][c] += paraSet.prom[d][c];
							paraSet.N_d[d] += paraSet.prom[d][c];
							if (paraSet.alpha[d][c] == 1)
								paraSet.sumNc_d[d] += paraSet.prom[d][c];
						}
						else {
							paraSet.Nc_d[d][c]++;
							paraSet.N_d[d]++;
							if (paraSet.alpha[d][c] == 1)
								paraSet.sumNc_d[d]++;
						}
						if (flag) {
							
							paraSet.Nw_c[c][wordNo] += paraSet.eta[wordNo][c];
							paraSet.N_c[c] += paraSet.eta[wordNo][c];
						}
						else {
							paraSet.Nw_c[c][wordNo]++;
							paraSet.N_c[c]++;
						}
						paraSet.N_1++;
						//paraSet.N_1 += paraSet.prom[d][c];
					}
					else {
						document.xs.set(w, 0);
						document.cates.set(w, -1);
						paraSet.Nw_0[wordNo]++;
						paraSet.N_0++;
					}

				}
				
				//System.out.println(paraSet.N_1);

			}
			
			if (sparseFlag/* && it % 10 == 9*/) {
				int counter = 0;
				for (int i=0; i<1; i++) {
					for (int d=0; d<paraSet.docNum; d++) {
						
						for (int c=0; c<paraSet.catNum; c++) {

							if (paraSet.alpha[d][c] == 1) {
								paraSet.alphaSum[d]--;
								paraSet.sumNc_d[d] -= paraSet.Nc_d[d][c];
									
							}
							paraSet.alpha[d][c] = sampleAlpha(d, c);

							if (paraSet.alpha[d][c] == 1) {
								paraSet.alphaSum[d]++;
								paraSet.sumNc_d[d] += paraSet.Nc_d[d][c];
							}
						}
						
						//System.out.println(paraSet.alphaSum[d]);
						/*if (paraSet.alphaSum[d] > 1) {
							counter++;
							System.out.println(paraSet.documents[d].docLength);
						}*/
						if (paraSet.documents[d].docLength < 10) {
							counter++;
						}
						
						if (paraSet.alphaSum[d] == 0) {
							int maxC = 0;
							double max = paraSet.Nc_d[d][0];
							for (int c2=0; c2<paraSet.catNum; c2++) {
								paraSet.documents[d].prediction[c2] = false;
								if (paraSet.Nc_d[d][c2] > max) {
									max = paraSet.Nc_d[d][c2];
									maxC = c2;
								}
							}
							paraSet.documents[d].prediction[maxC] = true;
						}
						else {
							MDocument doc = paraSet.documents[d];
							for (int c=0; c<paraSet.catNum; c++) {
								if (paraSet.alpha[d][c] == 1) {
									doc.prediction[c] = true;
								}
								else {
									doc.prediction[c] = false;
								}
							}
						}
						
						/*double sum = 0;
						for (int c=0; c<paraSet.catNum; c++) {
							sum += (paraSet.alpha[d][c] * (paraSet.Nc_d[d][c] + paraSet.gamma0) + paraSet.gamma1) /
									(paraSet.sumNc_d[d] + paraSet.alphaSum[d] * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1);
						}
						
						System.out.println(sum);*/
					}
					
					
				}
				
				System.out.println(counter);
				
			}
			
			//System.out.println(paraSet.N_0);
			//System.out.print(it + ":\t");
			if (directFlag && it%20 == 19) {
				for (int d=0; d<paraSet.docNum; d++) {
					double sum = 0;
					double sum2 = 0;
					double sumAlpha = 0;
					double sumN = 0;
					for (int c=0; c<paraSet.catNum; c++) {

						MDocument doc = paraSet.documents[d];
						double p =(paraSet.alpha[d][c] * (paraSet.Nc_d[d][c] + paraSet.gamma0) + paraSet.gamma1) /
						(paraSet.sumNc_d[d] + paraSet.alphaSum[d] * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1);
						sum2 += p;
						if (p > 0.2) {
							doc.prediction[c] = true;
							paraSet.alpha[d][c] = 1;
							sumAlpha++;
							sumN += paraSet.Nc_d[d][c];
							sum++;
						}
						else {
							doc.prediction[c] = false;
							paraSet.alpha[d][c] = 0;
						}
					}
					paraSet.alphaSum[d] = sumAlpha;
					paraSet.sumNc_d[d] = sumN;
					//System.out.println(sum);
					
				}
			}
			
			
			System.out.println("aaa:" + paraSet.N_0);
			
			Reporter.fMeasure(paraSet);
			//Reporter.hammingLoss(paraSet);
			//Reporter.oneError(paraSet);
			//Reporter.coverage(paraSet);
			//Reporter.rankLoss(paraSet);
			Reporter.macroAucReport(paraSet);
			//Reporter.microAucReport(paraSet);

		}
	}
	
	private int sampleC(int d, int wordNo){

		double[] prob = new double[paraSet.catNum + 1];
		double[] tmp = new double[paraSet.catNum];
		
		for (int c = 0; c<paraSet.catNum + 1; c++) {
			
			
			double term1, term2;
			

			//term1
			if (c < paraSet.catNum) {
				
				if (paraSet.vocabulary.isSeedWordOfTopic(wordNo, c)) {
					return c;
				}
				
				term1 = (paraSet.N_1 + paraSet.gamma) * (paraSet.alpha[d][c] * (paraSet.Nc_d[d][c] + paraSet.gamma0) + paraSet.gamma1) /
						(paraSet.sumNc_d[d] + paraSet.alphaSum[d] * paraSet.gamma0 + paraSet.catNum * paraSet.gamma1);
				tmp[c] = (paraSet.sumNc_d[d]);
			}
			else {
				term1 = (paraSet.N_0 + paraSet.gamma);
				//term1 = 0;
			}
			
			
			
			//term2
			if (c < paraSet.catNum) {
				term2 = (paraSet.Nw_c[c][wordNo] + paraSet.beta1)
						/ (paraSet.N_c[c] + paraSet.vocabulary.wordNum * paraSet.beta1);
				
				//term2 = paraSet.eta[wordNo][c];
			}
			else {
				term2 = (paraSet.Nw_0[wordNo] + paraSet.beta0)
						/ (paraSet.N_0 + paraSet.vocabulary.wordNum * paraSet.beta0);
			}
			
			prob[c] = term1 * term2;
			
		}		
		
		for(int c=1; c<paraSet.catNum + 1; c++){
			prob[c] += prob[c-1];
		}
		
		double thred = Math.random() * prob[paraSet.catNum];
		
		int cChoosed = -1;
		for (cChoosed=0; cChoosed<paraSet.catNum+1; cChoosed++) {
			if(thred <= prob[cChoosed]){
				break;
			}
		}
		
		/*if (cChoosed >10) {
			for (int c=0; c<paraSet.catNum; c++) {
				System.out.println(tmp[c]);
			}
		}*/
		
		return cChoosed;
	}
	
	private double sampleAlpha(int d, int c) {
		
		
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

		//System.out.println(beta[0] + "\t" + (paraSet.vocabulary.wordNum) + "\t" + paraSet.betaSum[c]);

		
		//System.out.println(paraSet.gamma0 + paraSet.gamma1);
		//System.out.println(paraSet.Nw_c[c][w] + paraSet.gamma0 + paraSet.gamma1);
		//System.out.println(paraSet.eta[w][c]);
		//System.out.println(paraSet.betaNum[c] + "\t" + paraSet.betaSum[c]);
		//System.out.println(beta[0] + "\t" + beta[1]);
		
		//return (int) (Math.random() * 2);

		return MathUtil.sample_neg(alpha); 
		//return 1;
		
	}
	
}
