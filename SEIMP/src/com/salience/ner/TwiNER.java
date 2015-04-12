/**
 *
 */
package com.salience.ner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.salience.AppGlobals;
import com.salience.Utilities;

/**
 * @author Chenliang Li [lich0020@ntu.edu.sg]
 *
 */
public class TwiNER {

	private static final int N = 5; // up to 5-gram;
	private static final char SEPARATOR_CHAR = '_';
	public static Comparator<Double> DOUBLE_ASC = new Comparator<Double>() {
		@Override
		public int compare(Double d1, Double d2) {
			return d1.compareTo(d2);
		}
	};

	private static double MIN_LOG = -Math.pow(10, 20);

	public TwiNER() {

	}

	private List<String> tokenizeTweet(String tweet) {
		tweet = cleanTweet(tweet);
		tweet = tweet.replaceAll("@[\\p{Alnum}\\p{Punct}]+", "@USERNAME");

		StringBuilder buffer = new StringBuilder(tweet);
		int index = -1;
		do {
			index = buffer.indexOf("@USERNAME", index + 1);
			if (index > 0 && Character.isLetter(buffer.charAt(index - 1))) {
				buffer.replace(index, index + 9, " ");
			}
		} while (index >= 0);
		tweet = buffer.toString();

		// normalize the urls
		tweet = tweet.replaceAll("http://[\\p{Alnum}\\p{Punct}]+", "@http")
				.trim();

		// normalize RT labels
		tweet = tweet.replaceAll("[rR][tT]\\s+", " ").trim();
		tweet = tweet.replaceAll("\\s", " ");

		tweet = tweet.replaceAll("#[\\p{Alnum}p{Punct}]+", " ");
		tweet = tweet.replace("@USERNAME", " ");
		tweet = tweet.replace("@http", " ").trim().toLowerCase();

		List<String> tokenlist = new ArrayList<String>();
		String[] tokens = tweet.split("\\s");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (!AppGlobals.STOP_WORD_LIST.contains(token)) {
				tokenlist.add(token);
			}
		}

		return tokenlist;
	}

	public List<ScoredSegment> tweetSegmentBySCP(String tweet) {
		List<String> tokenlist = tokenizeTweet(tweet);

		int length = tokenlist.size();
		if (length == 0)
			return null; // nothing to segment

		List<Map<List<Integer>, Double>> segmentList = new ArrayList<Map<List<Integer>, Double>>();

		double[][] probMatrix = MSNGramProb(tokenlist, N);

		for (int i = 0; i < length; i++) {
			Map<List<Integer>, Double> subsegmentMap = new HashMap<List<Integer>, Double>();

			String ngram = ngram(tokenlist, 0, i);
			if (i < N) {
				List<Integer> subsegment = new ArrayList<Integer>();
				subsegment.add(i);

				double score = Double.NEGATIVE_INFINITY;
				score = stickinessWithWikiAndLengthNormAndSCP(ngram,
						probMatrix, 0, i);

				subsegmentMap.put(subsegment, score);
			}

			if (i > 0) { // dynamic programming
				for (int j = 0; j < i; j++) { // iterate over all prior sgt
					if (i - j > N)
						continue;

					ngram = ngram(tokenlist, j + 1, i);
					Map<List<Integer>, Double> priorsgtMap = segmentList.get(j);
					double score = stickinessWithWikiAndLengthNormAndSCP(ngram,
							probMatrix, j + 1, i);

					for (List<Integer> priorsgt : priorsgtMap.keySet()) {
						Double value = priorsgtMap.get(priorsgt);
						value += score;
						List<Integer> sgt = mergeTwoSeg(priorsgt, i);
						subsegmentMap.put(sgt, value);
					}
				}
			}

			/* filter out lower scored segment */
			int k = 5;
			List<KeyValueObj<Double, List<Integer>>> topN = topN(subsegmentMap,
					DOUBLE_ASC, k);
			subsegmentMap.clear();
			for (KeyValueObj<Double, List<Integer>> kvo : topN) {
				subsegmentMap.put(kvo.getValue(), kvo.getKey());
			}

			segmentList.add(subsegmentMap);
		}

		Map<List<Integer>, Double> finalsgt = segmentList.get(length - 1);
		List<Integer> bestsgt = null;
		double max = Double.NEGATIVE_INFINITY;
		for (List<Integer> sgt : finalsgt.keySet()) {
			double score = finalsgt.get(sgt);
			if (Double.compare(score, max) > 0) {
				bestsgt = sgt;
				max = score;
			}
		}

		// LOG.info("best score: " + max);
		int[] marks = new int[length];
		int index = 0;
		for (int j = 0; j < marks.length; j++) {
			int pos = bestsgt.get(index);
			if (j == pos) {
				marks[j] = 1;
				index++;
			}
		}

		List<List<String>> segment = new ArrayList<List<String>>();
		List<String> subSeg = new ArrayList<String>();
		for (int i = 0; i < marks.length; i++) {
			subSeg.add(tokenlist.get(i));
			if (marks[i] == 1) {
				segment.add(subSeg);
				subSeg = new ArrayList<String>();
			}
		}

		List<ScoredSegment> segmentWithScore = new ArrayList<ScoredSegment>();

		int s = 0;
		for (int i = 0; i < segment.size(); i++) {
			List<String> subseg = segment.get(i);
			int e = s + subseg.size();
			String ngram = ngram(tokenlist, s, e - 1);
			double score = stickinessWithWikiAndLengthNormAndSCP(ngram,
					probMatrix, s, e - 1);

			segmentWithScore.add(new ScoredSegment(subseg, score));
			s = e;
		}

		return segmentWithScore;
	}

	/**
	 * Return a matrix of n-gram probabilities. The matrix has a L x L size,
	 * where L is the length of the specified tokenlist. The entry (i,j) refers
	 * to the probability of n-gram w_i...w_j based on the MS N-Gram Service.
	 * Note that the indices i, j refer to the words located at the i, j
	 * position of the specified tokenlist.
	 * 
	 * @param tokenlist
	 * @param m
	 * @return
	 */
	private double[][] MSNGramProb(List<String> tokenlist, int m) {
		int L=tokenlist.size();
		double[][] ngramProb=new double[L][L];
		for(int i=0;i<L;i++) {
			for(int j=0;j<L;j++) {
				final StringBuilder builder=new StringBuilder();
				for(int k=i;k<=j;k++) {
					builder.append(tokenlist.get(i));
					if(k!=j)
						builder.append(SEPARATOR_CHAR);
				}
				ngramProb[i][j]=Utilities.getNgramProbability(builder.toString());
			}
		}
		return ngramProb;
	}

	private List<Integer> mergeTwoSeg(List<Integer> prior, int index) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < prior.size(); i++) {
			list.add(prior.get(i));
		}

		list.add(index);
		return list;
	}

	private double stickinessWithWikiAndLengthNormAndSCP(String ngram,
			double[][] probMatrix, int s, int e) {
		double score = stickinessWithLengthNormBySCP(probMatrix, s, e);
		double keyphraseness = 0.0;
		int length = e - s + 1;
		try {
			keyphraseness = wikiKeyphraseness(ngram);
		} catch (Exception ex) {
			ex.printStackTrace();
			keyphraseness = 0.0;
		}

		return score * Math.exp(keyphraseness);
	}

	private double stickinessWithLengthNormBySCP(double[][] probMatrix, int s,
			int e) {
		double score = stickinessBySCP(probMatrix, s, e);
		// double score = stickinessByGFSCP(
		// probMatrix, s, e);
		score = 2 / (1 + Math.exp(-score));
		int n = e - s + 1;
		if (n > 1) {
			score = score * (n - 1) / n;
		}

		return score;
	}

	private double stickinessBySCP(double[][] probMatrix, int s, int e) {
		final double instead = Double.NaN;
		int n = e - s + 1;
		double score = 0;

		double nprob = probMatrix[s][e];

		if (n == 1) { // unigram
			score = log10(nprob) * 2;
		} else { // n-gram
			double avg = 0.0;
			for (int i = s; i < e; i++) {
				double inprob = probMatrix[i + 1][e];
				double iprob = probMatrix[s][i];
				avg += (inprob * iprob);
			}
			avg = avg / (n - 1);
			score = nprob * nprob / avg;
			score = log10(score);
		}

		return score;
	}

	private double log10(double value) {
		if (Double.compare(value, 0.0) <= 0) {
			return MIN_LOG;
		}

		return Math.log10(value);
	}

	/**
	 * Return the keyphraseness of the specified phrase, which is the prior
	 * probability of the specified phrase to be an anchor text in Wikipedia.
	 * 
	 * @param ngram
	 * @return
	 */
	private double wikiKeyphraseness(String ngram) {
		return getKeyPhraseness(ngram);
	}

	/**
	 * Clear tweet by removing the specific characters specified by '\'
	 * 
	 * @param text
	 * @return
	 */
	private static String cleanTweet(String text) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char value = text.charAt(i);
			if (value != '\\' || i == text.length() - 1) {
				buffer.append(value);
			} else {
				char suffix = text.charAt(i + 1);
				if (suffix == 't' || suffix == 's' || suffix == 'n'
						|| suffix == '\"' || suffix == 'r') {
					buffer.append(' ');
					i += 1; // jump off
				}
			}
		}

		return buffer.toString();
	}

	/**
	 * Gets the ngram from the specified token list.
	 * 
	 * @param tokenlist
	 * @return
	 */
	public static String ngram(List<String> tokenlist) {
		return ngram(tokenlist, 0, tokenlist.size() - 1);
	}

	/**
	 * Gets the ngram from the position specified by 's', to the position
	 * (included) specified by 'e'
	 * 
	 * @param tokenlist
	 * @param s
	 * @param e
	 * @return
	 */
	public static String ngram(List<String> tokenlist, int s, int e) {
		StringBuilder buffer = new StringBuilder();
		for (int i = s; i <= e; i++) {
			buffer.append(tokenlist.get(i));
			if (i != e)
				buffer.append(SEPARATOR_CHAR);
		}

		return buffer.toString();
	}

	public static <K, V> List<KeyValueObj<K, V>> topN(Map<V, K> map,
			final Comparator<K> cmp, int n) {
		List<KeyValueObj<K, V>> sortedList = sort(map, cmp);
		while (sortedList.size() > n) {
			sortedList.remove(0);
		}

		return sortedList;
	}

	public static <K, V> List<KeyValueObj<K, V>> sort(Map<V, K> map,
			final Comparator<K> cmp) {
		Comparator<KeyValueObj<K, V>> kvo_cmp = new Comparator<KeyValueObj<K, V>>() {

			@Override
			public int compare(KeyValueObj<K, V> o1, KeyValueObj<K, V> o2) {
				// TODO Auto-generated method stub
				return cmp.compare(o1.getKey(), o2.getKey());
			}
		};

		List<KeyValueObj<K, V>> list = new ArrayList<KeyValueObj<K, V>>();
		for (V v : map.keySet()) {
			K k = map.get(v);
			list.add(new KeyValueObj<K, V>(k, v));
		}
		Collections.sort(list, kvo_cmp);

		return list;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String tweet = "BBC News: United Kingdom and international news headlines.";
		//String tweet="Great meeting with President BarackObama. Wales NATOSummitUK preparations well underway http://t.co/ziBtbBpMMf";
		long start=System.currentTimeMillis();
		System.out.println(recognizeNE(tweet));
		System.out.println((System.currentTimeMillis()-start)/1000);
		/*
		TwiNER tweetSegmenter = new TwiNER();

		List<ScoredSegment> sslist = tweetSegmenter.tweetSegmentBySCP(tweet);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < sslist.size(); i++) {
			ScoredSegment ss = sslist.get(i);
			List<String> seglist = ss.getSeg();
			String seg = ngram(seglist);
			buffer.append(seg).append(' ');
			System.out.println(seg+"\t"+ss.score());
		}

		System.out.println(buffer.toString().trim()); */
	}
	
	public static List<String> recognizeNE(String tweet) throws IOException{
		//Finds the named entities using TwiNER/ 
		final TwiNER tweetSegmenter = new TwiNER();

		final List<ScoredSegment> sslist = tweetSegmenter.tweetSegmentBySCP(tweet);
		final List<Segment> sortedSegment=new ArrayList<Segment>();
		for (int i = 0; i < sslist.size(); i++) {
			ScoredSegment ss = sslist.get(i);
			List<String> seglist = ss.getSeg();
			String seg = ngram(seglist).replaceAll("_", " ");
			if(seg.trim().length()>0)
				sortedSegment.add(new Segment(seg,ss.score()));
		}
		//Sort the list by rank in decreasing order.
		Collections.sort(sortedSegment);
		
		final List<String> neList=new ArrayList<String>();
		for(int i=0;i<AppGlobals.TWINER_MAX_RES_SIZE;i++){
			if(i<sortedSegment.size() && i<AppGlobals.TWINER_MAX_RES_SIZE)
				neList.add(sortedSegment.get(i).getText());
		}	
		
		return neList;
	}	

	private static HashMap<String,Double> keyphraseness_map=null;
	private static double getKeyPhraseness(final String ngram) {
		//Returns the keyphraseness value of the ngram in wiki corpus.
		if(keyphraseness_map==null) {
			keyphraseness_map=new HashMap<String,Double>();
			//load the map
			BufferedReader reader=null;
			try {
				reader = new BufferedReader(new FileReader(AppGlobals.TWINER_WIKI_KEYPHRASENESS_FILE));
				String line=null;
				while((line=reader.readLine())!=null){
					keyphraseness_map.put(line.substring(0,line.lastIndexOf(",")),Double.parseDouble(line.substring(line.lastIndexOf(",")+1)));
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return keyphraseness_map.get(ngram)==null?0.0:keyphraseness_map.get(ngram);
	}

}

class Segment implements Comparable<Segment>{
	String text;
	Double score;
	
	public Segment(){}
	public Segment(String text,double score){this.text=text;this.score=score;}
	
	@Override
	public int compareTo(Segment doc2) {
		int val=this.score.compareTo(doc2.score);
		if(val>0) return -1;
		if(val<0) return 1;
		return 0;
	}	
	
	public String getText(){
		return text;
	}
	
}

class ScoredSegment {
	private List<String> seg;
	private double score;

	private Object data;

	public ScoredSegment(String[] ngrams, double score) {
		seg = new ArrayList<String>();
		for (int i = 0; i < ngrams.length; i++) {
			seg.add(ngrams[i]);
		}
		this.score = score;
	}

	public ScoredSegment(List<String> seg, double score) {
		this.seg = seg;
		this.score = score;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public List<String> getSeg() {
		return seg;
	}

	void setScore(double score) {
		this.score = score;
	}

	public double score() {
		return score;
	}
}

class KeyValueObj<K, V> {

	private K key;

	private V value;

	public KeyValueObj(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o instanceof KeyValueObj) {
			KeyValueObj kvo = (KeyValueObj) o;
			if (key.equals(kvo.key) && value.equals(kvo.value))
				return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}
}