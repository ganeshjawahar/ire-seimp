package com.salience.client;

import java.util.ArrayList;
import java.util.List;

public class ClientGlobals {
	public static String MONGO_DB_NAME="seimp";
	public static List<String> COLLECTION_LIST=new ArrayList<String>();
	public static List<String> ANNOTATOR_LIST=new ArrayList<String>();
	static{
		COLLECTION_LIST.add("smallseimptrainingset");
		COLLECTION_LIST.add("largeseimptrainingset");
		COLLECTION_LIST.add("interannotationset");
		
		ANNOTATOR_LIST.add("ganesh");
		ANNOTATOR_LIST.add("snehith");
		ANNOTATOR_LIST.add("sindhura");
		ANNOTATOR_LIST.add("priya");
	}
	
}