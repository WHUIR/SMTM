package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import Util.LuceneHandler;


public class Initalizer {

	private ParaSet paraSet;
	private LuceneHandler lh;
	private String seedwordPath;
	
	
	public Initalizer(ParaSet paraSet, String luceneIndexPath, 
			String seedwordPath) {
		this.paraSet = paraSet;
		lh = new LuceneHandler(luceneIndexPath);
		this.seedwordPath = seedwordPath; 
		
	}
	
	public void initalize() {
		
		System.out.println("initalizing...");
		initParaSet();
		System.out.println("loading docs...");
		loadDocuments();
		System.out.println("calculate co-occurrence...");
		new CoOccurrence(paraSet);
		
	}
	
	private Set<String>[] loadSeedword() {
        
		Set<String>[] seedSet = new HashSet[paraSet.catNum];
		String line;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(seedwordPath));
        } catch (Exception e) {
            System.out.println("the seed word file path is illegal");
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            while ((line = br.readLine()) != null) {
                
                String[] vecs = line.split(" ");
                if (!paraSet.cat2IdMap.containsKey(vecs[0])) {
                	paraSet.catNum--;
                	continue;
                }
                int cate = paraSet.cat2IdMap.get(vecs[0]);
                seedSet[cate] = new HashSet<>();
                
                for (int i=1; i<vecs.length; i++)
                    seedSet[cate].add(vecs[i]);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("the seed word file is null or ill-formed , please see the 'readme.txt' for more information");
            e.printStackTrace();
            System.exit(-1);
        }
        Set<String>[] newSeedSet = new HashSet[paraSet.catNum];
        for (int c=0; c<paraSet.catNum; c++) {
        	newSeedSet[c] = seedSet[c];
        }
        return newSeedSet;
    }
	
	private void initParaSet() {
		//get documents number
		paraSet.docNum = lh.getNumDocs();
				
		//init vocabulary
		paraSet.vocabulary = new Vocabulary();
		paraSet.vocabulary.setSeedTopicSet(loadSeedword());
		paraSet.vocabulary.addWords(lh.getWordSet(), lh.getTermCFMap());
				
		//init documents
		paraSet.documents = new MDocument[paraSet.docNum];  //[document] the documents
		
		//statistics
		paraSet.Nc_d = new double[paraSet.docNum][paraSet.catNum];
		paraSet.N_d = new double[paraSet.docNum];
		paraSet.Nw_c = new double[paraSet.catNum][paraSet.vocabulary.wordNum];
		paraSet.N_c = new double[paraSet.catNum];
		paraSet.Nw_0 = new double[paraSet.vocabulary.wordNum];
		paraSet.alpha = new double[paraSet.docNum][paraSet.catNum];
		paraSet.alphaSum = new double[paraSet.docNum];
		for (int d=0; d<paraSet.docNum; d++) {
			paraSet.alphaSum[d] = paraSet.catNum;
			for (int c=0; c<paraSet.catNum; c++) {
				paraSet.alpha[d][c] = 1;
			}
		}
		paraSet.sumNc_d = new double[paraSet.docNum];
		
		//eta
		paraSet.eta = new double[paraSet.vocabulary.wordNum][paraSet.catNum];
		
		//promotion rate
		paraSet.prom = new double[paraSet.docNum][paraSet.catNum];
		for (int d=0; d<paraSet.docNum; d++)
			for (int c=0; c<paraSet.catNum; c++)
				paraSet.prom[d][c] = paraSet.mu;
				//paraSet.prom[d][c] = 1;

		//seed word number for each document
		paraSet.seedNum = new int[paraSet.docNum][paraSet.catNum];
	}
	
	private void loadDocuments() {

        Document[] lucDocs = lh.getDocs();

        int len = paraSet.docNum;
        if (len == 0) {
            System.out.println("there is no document in the path");
            System.exit(-1);
        }
        MDocument doc;
        Document luceneDoc;

        for (int i = 0; i < len; i++) {
        	luceneDoc = lucDocs[i];
            paraSet.documents[i] = new MDocument();
            
            doc = paraSet.documents[i];
            doc.groundTruth = new boolean[paraSet.catNum];
            doc.prediction = new boolean[paraSet.catNum];
            doc.index = i;
            doc.title = luceneDoc.get(LuceneHandler.TITLE);
            doc.seedIndicator = new boolean[paraSet.catNum];
            for (int cate : paraSet.fileList.get(i).cates ) {
            	doc.groundTruth[cate] = true;
            }
            doc.check = paraSet.fileList.get(i).check;
            
            String content = luceneDoc.get(LuceneHandler.ABSTRACT);
            String[] tokens = content.split(" ");
            for (String s : tokens) {
            	s = s.replace(String.valueOf((char)10), "");
            	int id;
            	if ((id = paraSet.vocabulary.toId(s)) != -1) {
            		doc.addWord(id);
        		}
            	
            	if (paraSet.vocabulary.isSeedword(id)) {
            		doc.hasSeed = true;
            		for (int c=0; c<paraSet.catNum; c++) {
            			if (paraSet.vocabulary.isSeedWordOfTopic(id, c)) {
            				paraSet.prom[i][c] = 1;
            				doc.seedIndicator[c] = true;
            			}
            		}
            		paraSet.seedNum[i][paraSet.vocabulary.getSeedCate(id)]++;
            	}
            }
        }
        
        for (int d=0; d<paraSet.docNum; d++) {
        	double sum = 0;
        	for (int c=0; c<paraSet.catNum; c++) {
        		sum += paraSet.prom[d][c];
        	}
        	
        	for (int c=0; c<paraSet.catNum; c++) {
        		paraSet.prom[d][c] *= paraSet.catNum / sum;
        	}
        }
		
	}
	
}
