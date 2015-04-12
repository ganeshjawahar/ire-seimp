package com.salience.collect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.salience.AppGlobals;
import com.salience.MongoDbManager;
import com.salience.TwitterManager;
import com.salience.Utilities;

public class SeimpTrainingSetManager {

	public static void main(final String[] argv) throws Exception {
		long start = System.currentTimeMillis();
		// createSmallDataSet(getUniqueUsers(AppGlobals.MONGO_DB_NAME,AppGlobals.MEIJ_TRAINING_SET_COLLECTION_NAME));
		// computeNE(AppGlobals.MONGO_DB_NAME,AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME,AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.ARK_TWEET,AppGlobals.NER.STANFORD_CRF);

		// List<String>
		// mar20List=Arrays.asList("Baby akshay","Rahasya","Main Aur Charles","Dum Laga Ke Haisha","Badlapur","Nh10 Navdeep Singh","Shamitabh","Dhanak","Liar's Dice","Tigers emraan","Hawaizaada","Monsoon Shootout","Ab Tak Chhappan 2","Tevar","roy ranbir");
		// List<String>
		// mar21List=Arrays.asList("#NZvWI","#m");//,"#PakvsAus","#AUSvPAK","#INDvsBAN","#BANvsIND","#SAvSL","#SLvSA");
		// for(final String keyword:mar21List)
		// createBigCollection(keyword);
		
		// fillMergedNeList(AppGlobals.MONGO_DB_NAME,AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME);
		
		//createInterAnnotationDataset("#AppleWatch",20); //114
		//createInterAnnotationDataset("#SAvsNZ",20);//406
		//createInterAnnotationDataset("#NationalAwards",20);//79
		
		//computeNE(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.ARK_TWEET,AppGlobals.NER.STANFORD_CRF);
		//fillMergedNeList(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
	}
	
	public static void writeToDisk(final String dbName,final String collectionName,final String fileName) throws IOException{
		//Write the entire collection into a file.
		final PrintWriter writer=new PrintWriter(fileName);
		final DBCollection dbc = MongoDbManager.getCollection(dbName,collectionName);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			writer.println(cursor.next().toString());
		}
		writer.close();
	}

	public static void fillMergedNeList(final String dbName,
			final String collection) throws IOException, ClassNotFoundException {
		// For every tweet in the collection, collect the NE's from different
		// ner output and union them.
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collection);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
						
			//Set the annotation to null
			row.setAnnotationList(null);
			
			//Union the NE's
			final HashMap<String,List<String>> neMap=new HashMap<String,List<String>>();
			for(final NERList ner:row.getNerList()){
				for(final String ne:ner.getNeList()){
					if(neMap.get(ne)==null) neMap.put(ne, new ArrayList<String>());
					neMap.get(ne).add(ner.getName());
				}
			}			
			final List<String> resNeList=new ArrayList<String>();
			for(final Entry<String,List<String>> entry:neMap.entrySet()){
				final StringBuilder builder=new StringBuilder(entry.getKey());
				builder.append(" (");
				for(final String ner:entry.getValue())
					builder.append(ner+",");
				builder.setLength(builder.length()-1);
				builder.append(")");
				resNeList.add(builder.toString());
			}			
			row.setMergedNeList(resNeList);
			
			// Save the merged NER output.
			final BasicDBObject setNEBdbo = new BasicDBObject();
			setNEBdbo.put("$set",new BasicDBObject().append("mergedNeList",row.getMergedNeList()).append("annotationList",row.getAnnotationList()));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNEBdbo);	
		}

	}

	public static void computeNE(final String dbName, final String collection,
			final AppGlobals.NER... nerModules) throws IOException,
			ClassNotFoundException, InterruptedException {
		// For every tweet in the collection, compute and save the NE's based on
		// tweet text.

		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collection);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");

			// Save individual ner output separately.
			final BasicDBList bdbList = new BasicDBList();
			for (int index = 0; index < nerModules.length; index++) {
				final BasicDBObject bdbo = new BasicDBObject();
				bdbo.put("name", nerModules[index].name());
				bdbo.put("neList",
						Utilities.doNER(nerModules[index], row.getText()));
				bdbList.add(bdbo);
			}
			final BasicDBObject setNERBdbo = new BasicDBObject();
			setNERBdbo.put("$set",
					new BasicDBObject().append("nerList", bdbList));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNERBdbo);
			Thread.sleep(500);

			// Save the merged NER output too.
			final BasicDBObject setNEBdbo = new BasicDBObject();
			setNEBdbo.put(
					"$set",
					new BasicDBObject().append("mergedNeList",
							Utilities.mergeNER(row.getText(), nerModules)));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNEBdbo);
			Thread.sleep(500);
		}
	}

	public static List getUniqueUsers(final String dbName,
			final String collection) {
		// Find the unique users from the supplied collection.
		return MongoDbManager.getCollection(dbName, collection).distinct(
				"userId");
	}

	public static void createSmallDataSet(final List userList)
			throws NumberFormatException, TwitterException,
			InterruptedException, FileNotFoundException {
		// Creates the training dataset based on recent tweets from supplied
		// users.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		for (final Object user : userList) {
			final long userId = Long.parseLong(user.toString());

			// Get the recent tweets for the user.
			int countValidTweets = 0;
			List<Status> tweets = null;

			int iteration = 1;
			Paging paging = new Paging(iteration, 100);
			do {
				try {
					tweets = twitter.getUserTimeline(userId, paging);
				} catch (Exception e) {
					if (AppGlobals.IS_DEBUG)
						System.out.println("User " + userId
								+ " is not processed due to error "
								+ e.getMessage());
				}

				for (final Status status : tweets) {
					if (countValidTweets < AppGlobals.GET_TWEETS_FROM_USER_TIME_LINE_COUNT) {
						SeimpTrainingRow row = null;
						// Process only if the tweet is in english.
						if (status.getLang().equals("en")) {
							// get the media entities from the status
							MediaEntity[] media = status.getMediaEntities();
							for (MediaEntity m : media) {
								if (m.getMediaURL().contains("jpg")) {
									if (row == null) {
										row = new SeimpTrainingRow();

										// Set the tweet parameters.
										row.set_id(status.getId());
										row.setText(status.getText());
										row.setCreatedAt(status.getCreatedAt());
										row.setUserId(status.getUser().getId());
										row.setFavoriteCount(status
												.getFavoriteCount());
										row.setRetweetCount(status
												.getRetweetCount());
									}
									row.addImage(m.getMediaURL());
								}
							}
							if (row != null) {
								// check if the row exist already in mongo.
								final BasicDBObject whereClause = new BasicDBObject();
								whereClause.put("_id", status.getId());
								if (MongoDbManager
										.getCollection(
												AppGlobals.MONGO_DB_NAME,
												AppGlobals.SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME)
										.find(whereClause).size() == 0) {
									// save in mongo.
									MongoDbManager
											.insertJSON(
													AppGlobals.MONGO_DB_NAME,
													AppGlobals.SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME,
													row);
									++countValidTweets;
								}
							}
						}
					}
				}

				paging.setPage(++iteration);
				if (iteration > AppGlobals.MAX_PAGE_PER_USER_CHECK) {
					if (AppGlobals.IS_DEBUG)
						System.out.println("Got only " + countValidTweets
								+ " tweets for user " + userId);
					break;
				}

				// Sleep the main thread to account for GET
				// statuses/user_timeline 300 req/15min 1req/3seconds
				Thread.sleep(3000);

			} while (countValidTweets < AppGlobals.GET_TWEETS_FROM_USER_TIME_LINE_COUNT);
			if (AppGlobals.IS_DEBUG)
				System.out.println("Got full "
						+ AppGlobals.GET_TWEETS_FROM_USER_TIME_LINE_COUNT
						+ " tweets for user " + userId);
		}

	}

	public static void createBigCollection(final String keyword)
			throws TwitterException, InterruptedException {
		// Creates a tweet collection obtained from supplied keyword.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		// Compose the query.
		final Query query = new Query(keyword + " -filter:retweets");
		query.setLang("en");
		query.setResultType(ResultType.mixed);
		query.setCount(100);
		// query.since("2014-02-20");
		// query.until("2014-03-19");

		long lastID = Long.MAX_VALUE;
		while (true) {
			final QueryResult res = twitter.search(query);

			// Stopping criterion
			if (res.getTweets() == null || res.getTweets().size() == 0)
				break;

			// Parse the tweets
			int processed = 0;
			for (final Status status : res.getTweets()) {
				SeimpTrainingRow row = null;
				// Process only if the tweet is in english.
				if (status.getLang().equals("en")) {
					// get the media entities from the status
					MediaEntity[] media = status.getMediaEntities();
					for (MediaEntity m : media) {
						if (m.getMediaURL().contains("jpg")) {
							if (row == null) {
								row = new SeimpTrainingRow();

								// Set the tweet parameters.
								row.set_id(status.getId());
								row.setText(status.getText());
								row.setCreatedAt(status.getCreatedAt());
								row.setUserId(status.getUser().getId());
								row.setFavoriteCount(status.getFavoriteCount());
								row.setRetweetCount(status.getRetweetCount());
							}
							row.addImage(m.getMediaURL());
						}
					}
					if (row != null) {
						// check if the row exist already in mongo.
						final BasicDBObject whereClause = new BasicDBObject();
						whereClause.put("_id", status.getId());
						if (MongoDbManager
								.getCollection(
										AppGlobals.MONGO_DB_NAME,
										AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME)
								.find(whereClause).size() == 0) {
							// save in mongo.
							MongoDbManager
									.insertJSON(
											AppGlobals.MONGO_DB_NAME,
											AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME,
											row);
						}
						++processed;
					}
				}

				// Set the max id.
				if (status.getId() < lastID)
					lastID = status.getId();
			}

			if (AppGlobals.IS_DEBUG)
				System.out.println("Saved " + processed + " from "
						+ res.getTweets().size() + " tweets.");

			// Handle twitter rate-limit 450 request per 15 min.
			Thread.sleep(3000);

			query.setMaxId(lastID - 1);
		}
		System.out.println("Completed for -" + keyword);

	}
	
	public static void createInterAnnotationDataset(final String keyword,int size)
			throws TwitterException, InterruptedException {
		// Creates a tweet collection of size defined by 'size' and obtained from supplied keyword.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		// Compose the query.
		final Query query = new Query(keyword + " -filter:retweets");
		query.setLang("en");
		query.setResultType(ResultType.mixed);
		query.setCount(100);

		long lastID = Long.MAX_VALUE;
		final HashMap<Long,SeimpTrainingRow> rowMap=new HashMap<Long,SeimpTrainingRow>();
		while (true) {
			final QueryResult res = twitter.search(query);
			int processed = 0;

			// Stopping criterion
			if (res.getTweets() == null || res.getTweets().size() == 0)
				break;

			// Parse the tweets
			for (final Status status : res.getTweets()) {
				SeimpTrainingRow row = null;
				// Process only if the tweet is in english.
				if (status.getLang().equals("en")) {
					// get the media entities from the status
					MediaEntity[] media = status.getMediaEntities();
					for (MediaEntity m : media) {
						if (m.getMediaURL().contains("jpg")) {
							if (row == null) {
								row = new SeimpTrainingRow();

								// Set the tweet parameters.
								row.set_id(status.getId());
								row.setText(status.getText());
								row.setCreatedAt(status.getCreatedAt());
								row.setUserId(status.getUser().getId());
								row.setFavoriteCount(status.getFavoriteCount());
								row.setRetweetCount(status.getRetweetCount());
							}
							row.addImage(m.getMediaURL());
						}
					}
					if (row != null) {
						rowMap.put(row.get_id(), row);
						++processed;
					}
				}

				// Set the max id.
				if (status.getId() < lastID)
					lastID = status.getId();
			}

			if (AppGlobals.IS_DEBUG)
				System.out.println("Obtained " + processed + " from "
						+ res.getTweets().size() + " tweets.");

			// Handle twitter rate-limit 450 request per 15 min.
			Thread.sleep(3000);

			query.setMaxId(lastID - 1);
		}
		if (AppGlobals.IS_DEBUG)
			System.out.println("Obtained " + rowMap.size() + " tweets in total.");
		
		//Randomly pick 'size' tweets from the map
		final List<Long> keys = new ArrayList(rowMap.keySet());
		Collections.shuffle(keys);
		int count=0;
		for(int rowIndex=0;(rowIndex<keys.size() && rowIndex<size);rowIndex++) {
			MongoDbManager.insertJSON(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,rowMap.get(keys.get(rowIndex)));
			++count;
		}
		
		if (AppGlobals.IS_DEBUG)
			System.out.println("Saved " + count+" random tweets in total.");
		
		System.out.println("Completed for -" + keyword);
	}

}