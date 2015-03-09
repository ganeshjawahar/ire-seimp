package com.salience.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.salience.shared.SeimpTrainingRow;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface ServiceAsync {

	void getTweetCount(String dbName, String collection,
			AsyncCallback<Integer> callback);

	void getTweet(String dbName, String collection, int row,
			AsyncCallback<SeimpTrainingRow> callback);

	void saveSNE(String dbName, String collection, SeimpTrainingRow newRow,
			AsyncCallback<Void> callback);

	void getUnannotatedList(String dbName, String collection,
			AsyncCallback<List<String>> callback);
}
