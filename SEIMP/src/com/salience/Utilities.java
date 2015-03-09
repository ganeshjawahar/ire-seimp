package com.salience;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.salience.ner.AlanRitter;
import com.salience.ner.ArkTweet;

public class Utilities {
	
	public static String makeGetCall(final String urlStr) throws IOException{
		//Make a Get call with the supplied url, and return the response.
		final URL url=new URL(urlStr);
		final HttpURLConnection uc = (HttpURLConnection) url.openConnection();
		uc.setRequestMethod("GET");
		final int rspCode = uc.getResponseCode();
		if(rspCode!=200) return null;
		final BufferedReader br=new BufferedReader(new InputStreamReader(uc.getInputStream()));
		final String response=readFromStream(br);		
		br.close();
		return response;
	}
	
	public static String readFromStream(final BufferedReader br) throws IOException{
		//Read entire stream to a string and return it.
		final StringBuffer buff=new StringBuffer();
		String line=null;
		while((line=br.readLine())!=null)
			buff.append(line+"\n");
		return buff.toString();
	}
	
	public static List<String> mergeNER(final String text,final AppGlobals.NER... nerModules) throws IOException{
		//Merge (n) NE list and return the unique ones.
		if(nerModules.length==0) return null; //empty list
		
		//Merge all the list into one.
		List<String> resNEList=new ArrayList<String>();
		resNEList.addAll(doNER(nerModules[0],text));
		for(int listIndex=1;listIndex<nerModules.length;listIndex++){
			resNEList=merge2NEList(resNEList,doNER(nerModules[listIndex],text));
		}	
		
		return resNEList;	
	}
	
	private static List<String> doNER(final AppGlobals.NER nerModule,final String text) throws IOException{
		//Call the NER module for the text.
		
		if(nerModule==AppGlobals.NER.ALAN_RITTER)
			return AlanRitter.recognizeNE(text);
		else if(nerModule==AppGlobals.NER.ARK_TWEET)
			return ArkTweet.recognizeNE(text);

		return null; //incorrect ner module supplied.
	}
	
	private static List<String> merge2NEList(final List<String> neList1,final List<String> neList2){
		//Merges two NE list, and returns the unique ones.
		final Set<String> resNEList=new HashSet<String>();
		for(final String ne1:neList1){
			for(final String ne2:neList2){
				if(ne1.contains(ne2)) {
					resNEList.add(ne1);
				} else if(ne2.contains(ne1)) {
					resNEList.add(ne2);
				} else {
					if(!AppGlobals.STOP_WORD_LIST.contains(ne1)) {
						resNEList.add(ne1);
					}
					if(!AppGlobals.STOP_WORD_LIST.contains(ne2)) {
						resNEList.add(ne2);
					}
				}
			}
		}
		return new ArrayList<String>(resNEList);
	}

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
