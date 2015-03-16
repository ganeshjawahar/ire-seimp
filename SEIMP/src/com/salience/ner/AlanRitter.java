package com.salience.ner;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.salience.AppGlobals;
import com.salience.Utilities;

public class AlanRitter {
	
	public static List<String> recognizeNE(String tweet) throws IOException{
		//Find NE in the tweet, calling the ritter system.
		final List<String> neList=new ArrayList<String>();
		tweet=tweet.replaceAll("#", "");
		
		//Get the tagged text from the rest endpoint.
		final String taggedText=Utilities.makeGetCall(AppGlobals.GET_RITTER_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet));
		
		if(taggedText!=null) {		
			//Parse the tags to get NE
			final StringTokenizer st = new StringTokenizer(taggedText," ",false);
			boolean isNEOn=false;
			final StringBuffer neText=new StringBuffer();
			while(st.hasMoreTokens()){
				final String token[]=st.nextToken().split("\\/");
				
				if(token.length==3){ 
					//Make sure pos is set to true in the server side.
					final String word=token[0];
					final String neTag=token[1];
					final String posTag=token[2];
								
					if((neTag.startsWith("B-") || posTag.startsWith("NNP")) && !AppGlobals.STOP_WORD_LIST.contains(word.toLowerCase().trim())){
						isNEOn=true;
						neText.append(word+" ");
					} else if(neTag.startsWith("I-") && !AppGlobals.STOP_WORD_LIST.contains(word.toLowerCase().trim())){
						neText.append(word+" ");
					} else if(neTag.startsWith("B-ENTITY")){
						if(isNEOn) {
							isNEOn=false;
							neList.add(neText.toString());
							neText.setLength(0);						
						}
						isNEOn=true;
						neText.append(word+" ");
					} else if(neTag.startsWith("I-ENTITY")){
						isNEOn=true;
						neText.append(word+" ");
					} else {
						if(isNEOn) {
							isNEOn=false;
							neList.add(neText.toString().trim());
							neText.setLength(0);						
						}
					}
					
				}
			}	
			if(isNEOn) {
				isNEOn=false;
				neList.add(neText.toString().trim());
				neText.setLength(0);						
			}
		}
		
		return neList;
	}

}
