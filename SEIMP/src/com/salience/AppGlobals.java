package com.salience;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppGlobals {

	public final static boolean isDebug = true;

	/*
	 * Twitter configuration parameters.
	 */
	public final static String CONSUMER_KEY = "";
	public final static String CONSUMER_SECRET = "";
	public final static String OAUTH_ACCESS_TOKEN = "";
	public final static String OAUTH_ACCESS_TOKEN_SECRET = "";
	public final static String HTTP_PROXY_HOST = "";
	public final static String HTTP_PROXY_PORT = "";
	public final static int GET_TWEETS_FROM_USER_TIME_LINE_COUNT = 20;
	public final static int MAX_PAGE_PER_USER_CHECK = 10;

	/*
	 * Mongo DB Parameters.
	 */
	public final static String MONGO_DB_SERVER_IP = "";
	public final static int MONGO_DB_PORT = 27017;
	public final static String MONGO_DB_NAME = "seimp";
	public final static String MEIJ_TRAINING_SET_COLLECTION_NAME = "meijtrainingset";
	public final static String SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME = "smallseimptrainingset";
	public final static String LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME = "largeseimptrainingset";

	/*
	 * NER parameters
	 */
	public final static String STOP_WORD_LIST_FILE = "ner/stopwordslist.txt";
	public final static String ARK_TWEET_TAGGER_TRAINING_MODEL = "ner/arkTweetModel.20120919";
	public final static String RITTER_NER_RECOGNITION_ENDPOINT = "http://10.2.4.249:5050/extract?tweet=";
	public static List<String> STOP_WORD_LIST = null;

	public static enum NER {
		ALAN_RITTER, ARK_TWEET
	}

	/*
	 * Meij parameters
	 */
	public final static String MEIJ_WSDM_2012_ANNOTATIONS = "meij_wsdm_2012/wsdm2012_annotations.txt";

	static {
		STOP_WORD_LIST = new ArrayList<String>();

		// load the stop words.
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
