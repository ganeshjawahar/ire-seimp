package com.salience;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class MongoDbManager {
	
	private static MongoClient mongoClient=null;
	static{
		try {
			mongoClient=new MongoClient(new ServerAddress(AppGlobals.MONGO_DB_SERVER_IP,AppGlobals.MONGO_DB_PORT));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static DBCollection getCollection(final String dbName,final String collection){
		//Gets the specified collection.
		return mongoClient.getDB(dbName).getCollection(collection);
	}
	
	public static void insertJSON(final String dbName,final String collection,final Object content){
		//Convert the content to DB Object and insert to specified collection/db.
		
		//Convert to JSON, followed by DBObject
		final String jsonContent=Utilities.convertToJson(content);
		final DBObject dbo=(DBObject)JSON.parse(jsonContent);
		
		//Insert into Mongo.
		final DBCollection dbc=getCollection(dbName,collection);
		dbc.insert(dbo);
	}

}
