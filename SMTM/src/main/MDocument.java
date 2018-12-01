package main;

import java.util.ArrayList;
import java.util.List;

public class MDocument {
	
	public int index;
	public String title;
	public boolean[] groundTruth;
	public boolean[] prediction;
	public boolean check;
	public int docLength;
	public boolean hasSeed = false;
	public boolean[] seedIndicator;
	
	public List<Integer> words;
	public List<Integer> cates;
	public List<Integer> xs;
	
	
	public MDocument() {
		words = new ArrayList<Integer>();
		cates = new ArrayList<Integer>();
		xs = new ArrayList<Integer>();
		docLength = 0;
	}
	
	public void addWord(int word) {
		docLength++;
		words.add(word);
		cates.add(-1);
		xs.add(-1);
	}

}
