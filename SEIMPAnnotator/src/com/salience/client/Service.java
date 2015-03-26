package com.salience.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.salience.shared.SeimpTrainingRow;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface Service extends RemoteService {
	public Integer getTweetCount(final String dbName,final String collection);
	public SeimpTrainingRow getTweet(final String dbName,final String collection,final int row);
	public void saveSNE(final String dbName,final String collection,final SeimpTrainingRow newRow);
	public List<String> getUnannotatedList(final String dbName,final String collection,final String user);
}
