package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vocabulary {
	
	private Map<String, Integer> word2IdMap;
	private Map<Integer, String> id2WordMap;
	private Map<Integer, Integer> tf;
	
	private Set<String> seedSet;
	private Set<Integer> seedIdSet;

	private Set<String>[] seedTopicSet;
	private Set<Integer>[] seedTopicIdSet;
	
	public int seedwordNum;
	public int wordNum;
	
	public Vocabulary() {
		word2IdMap = new HashMap<String, Integer>();
		id2WordMap = new HashMap<Integer, String>();
		tf = new HashMap<Integer, Integer>();
		
		seedSet = new HashSet<String>();
		seedIdSet = new HashSet<Integer>();
		
		seedwordNum = 0;
		wordNum = 0;	
	}
	
	public int getTf(int wordId) {
		if (!tf.containsKey(wordId)) {
			return 0;
		}
		return tf.get(wordId);
	}
	
	public int toId(String word) {
		if(word2IdMap.containsKey(word)){
			return word2IdMap.get(word);
		}
		else {
			return -1;
		}
	}
	
	public String toWord(int word) {
		if(id2WordMap.containsKey(word)){
			return id2WordMap.get(word);
		}
		else {
			return null;
		}
	}
	
	public void setSeedTopicSet(Set<String>[] seedTopicSet) {
		this.seedTopicSet = seedTopicSet;
		for (int i=0; i<seedTopicSet.length; i++) {
			for (String word : seedTopicSet[i]) {
				this.seedSet.add(word);
			}
		}
	}
	
	public void addWords(Set<String> sets, Map<String, Integer> termCFMap) {
		for (String word : sets) {
			word2IdMap.put(word, wordNum);
			id2WordMap.put(wordNum, word);
			tf.put(wordNum, 0);
			if (seedSet.contains(word)) {
				seedIdSet.add(wordNum);
				seedwordNum++;
			}
			wordNum++;
		}
		
		for (String word : termCFMap.keySet()) {
			int termId = word2IdMap.get(word);
			//System.out.println(termId);
			tf.put(termId, termCFMap.get(word));
			//System.out.println(termCFMap.get(termId));
		}
		
		seedTopicIdSet = new Set[seedTopicSet.length];
		for (int i=0; i<seedTopicSet.length; i++) {
			seedTopicIdSet[i] = new HashSet<Integer>();
			for (String word : seedTopicSet[i]) {
				seedTopicIdSet[i].add(toId(word));
			}
		}	
	}
	
	/////////////////////
	public boolean isSeedword(String word) {
		return seedSet.contains(word);
	}
	
	public boolean isSeedword(int wordId) {

		return seedIdSet.contains(wordId);
	}
	
	public int getSeedCate(int wordId) {
		for (int c=0; c<seedTopicIdSet.length; c++) {
			if (seedTopicIdSet[c].contains(wordId)) {
				return c;
			}
		}
		return -1;
	}
	
	public Set<Integer>[] getSeedTopicIdSet() {
		return seedTopicIdSet;
	}
	
	public Set<Integer> getSeedIdSet() {
		return seedIdSet;
	}
	
	public Set<String>[] getSeedTopicSet() {
		return seedTopicSet;
	}
	
	public boolean isSeedWordOfTopic(int wordId, int topic) {
		if (seedTopicIdSet[topic].contains(wordId)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void addSeedWord(int wordId, int c) {
		seedTopicIdSet[c].add(wordId);
	}

	
	

}
