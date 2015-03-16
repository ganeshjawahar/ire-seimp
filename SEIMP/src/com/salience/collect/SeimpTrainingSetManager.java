package com.salience.collect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
		long start=System.currentTimeMillis();		
		//createSmallDataSet(getUniqueUsers(AppGlobals.MONGO_DB_NAME,AppGlobals.MEIJ_TRAINING_SET_COLLECTION_NAME));
		//computeNE(AppGlobals.MONGO_DB_NAME,AppGlobals.SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME,AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.ARK_TWEET,AppGlobals.NER.STANFORD_CRF);
		System.out.println("Done in "+(System.currentTimeMillis()-start)/1000+" s.");		
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
			setNERBdbo.put("$set",new BasicDBObject().append("nerList", bdbList));
			dbc.update(new BasicDBObject().append("_id", row.get_id()), setNERBdbo);
			Thread.sleep(4000);

			// Save the merged NER output too.
			final BasicDBObject setNEBdbo = new BasicDBObject();
			setNEBdbo.put("$set",new BasicDBObject().append("mergedNeList",Utilities.mergeNER(row.getText(), nerModules)));
			dbc.update(new BasicDBObject().append("_id", row.get_id()), setNEBdbo);
			Thread.sleep(4000);
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
					tweets = twitter.getUserTimeline(userId,paging);
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
						if (status.getLang().equals("en") ) {
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

}