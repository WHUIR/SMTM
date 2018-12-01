package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Test {

	public static void outputWordCat(ParaSet paraSet) {
		File fout;
		if (paraSet.expliFlag) 
			fout = new File("/Users/frank/Desktop/output/test.txt");
		else
			fout = new File("/Users/frank/Desktop/output/test.txt");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (int w = 0; w < paraSet.vocabulary.wordNum; w++) {
				bw.write(paraSet.vocabulary.toWord(w));
				for (int c=0; c<paraSet.catNum; c++) {
					bw.write(" " + paraSet.Nw_c[c][w]);
				}
				bw.newLine();
			}
			bw.close();
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void outputEta(ParaSet paraSet) {
		File fout;
		fout = new File("/Users/frank/Desktop/output/eta.txt");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (int w = 0; w < paraSet.vocabulary.wordNum; w++) {
				bw.write(paraSet.vocabulary.toWord(w));
				for (int c=0; c<paraSet.catNum; c++) {
					bw.write(" " + paraSet.eta[w][c]);
				}
				bw.newLine();
			}
			bw.close();
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
