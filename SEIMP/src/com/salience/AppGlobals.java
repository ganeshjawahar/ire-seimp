package com.salience;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

public class AppGlobals {

	public final static boolean IS_DEBUG = true;
	public final static String HTTP_PROXY_HOST="proxy.iiit.ac.in";
	public final static String HTTP_PROXY_PORT="8080";
	public final static Proxy HTTP_PROXY=new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.iiit.ac.in", 8080));

	/*
	 * Twitter configuration parameters.
	 */
	public final static int GET_TWEETS_FROM_USER_TIME_LINE_COUNT = 20;
	public final static int MAX_PAGE_PER_USER_CHECK = 100;

	/*
	 * Mongo DB Parameters.
	 */
	public final static String MONGO_DB_SERVER_IP = "10.2.4.249";
	public final static int MONGO_DB_PORT = 27017;
	public final static String MONGO_DB_NAME = "seimp";
	public final static String MEIJ_TRAINING_SET_COLLECTION_NAME = "meijtrainingset";
	public final static String SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME = "smallseimptrainingset";
	public final static String LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME = "largeseimptrainingset";
	public final static String INTER_ANNOTATION_SET_COLLECTION_NAME="interannotationset";

	/*
	 * NER parameters
	 */
	private final static String STOP_WORD_LIST_FILE = "data/ner/stopwordslist.txt";
	public final static String ARK_TWEET_TAGGER_TRAINING_MODEL = "data/ner/arkTweetModel.20120919";
	public static List<String> STOP_WORD_LIST = null;
	public final static String GET_RITTER_NER_RECOGNITION_ENDPOINT = "http://10.2.4.249:5050/extract?tweet=";
	public final static String TWINER_WIKI_KEYPHRASENESS_FILE="data/ner/WikiQsEng.txt";
	public final static String POST_MICROSOFT_WEB_NGRAM_ACCESS_ENDPOINT="http://weblm.research.microsoft.com/rest.svc/phrase-tweets/2011/1/jp?u=";
	public final static int TWINER_MAX_RES_SIZE=2;

	public static enum NER {
		ALAN_RITTER, ARK_TWEET, STANFORD_CRF
	}

	/*
	 * Meij parameters
	 */
	public final static String MEIJ_WSDM_2012_ANNOTATIONS = "data/meij_wsdm_2012/wsdm2012_annotations.txt";

	static {
		// load the stop words.
		STOP_WORD_LIST = new ArrayList<String>();
		try {
			final BufferedReader br = new BufferedReader(new FileReader(
					AppGlobals.STOP_WORD_LIST_FILE));
			String line = "";
			while ((line = br.readLine()) != null)
				STOP_WORD_LIST.add(line.trim());
			br.close();
		} catch (final IOException ie) {
			ie.printStackTrace();
		}
	}

}
