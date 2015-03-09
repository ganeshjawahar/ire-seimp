package com.salience.server;

import com.google.gson.Gson;

public class Utilities {

	private static final Gson gson=new Gson();
	
	public static String convertToJson(final Object content){
		//Converts the pojo rep. to json rep.
		return gson.toJson(content);
	}	
	
	public static Object convertToPOJO(final String content,final String pojoClassName) throws ClassNotFoundException{
		//Converts the content in JSON format to POJO.
		Class clazz=Class.forName(pojoClassName);
		return gson.fromJson(content, clazz);
	}
	

}
