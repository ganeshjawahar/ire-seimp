package com.salience.ner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.salience.AppGlobals;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;

public class ArkTweet {
	
	public static List<String> recognizeNE(String tweet) throws IOException{
		//Find NE in the tweet, using CMU Ark tweet nlp.
		final List<String> neList=new ArrayList<String>();
		tweet=tweet.replaceAll("#", " ");
		tweet=tweet.replaceAll("\\s+", " ");
		tweet=tweet.replaceAll("[^\\x00-\\x7F]", " ");
		
		//Call the tokenizer
		final List<TaggedToken> tokenList=tagger.tokenizeAndTag(tweet);
		final StringBuffer neText=new StringBuffer();
		for(final TaggedToken tt:tokenList){
			final String token=tt.token;
			final String tag=tt.tag;			
			if(tag.equals("Z") || tag.equals("#") || tag.equals("^") || tag.equals("S") || tag.equals("$")){
				if(tag.compareTo("$")==0 && (token.replaceAll("[0-9a-zA-Z]", "").trim().length()>0)){

				}
				else if(!AppGlobals.STOP_WORD_LIST.contains(token.toLowerCase()))
					neText.append(token+" ");
			}
			else if(neText.length()!=0 && tag.compareTo("$")==0){
				neText.append(token+" ");
			}
			else if(neText.length()!=0){
				neList.add(neText.toString().trim());
				neText.setLength(0);
			}
		}
		
		return neList;
	}
	
	private static Tagger tagger=null;
	static{
		tagger=new Tagger();
		
		//load the model.
		try{
			tagger.loadModel(AppGlobals.ARK_TWEET_TAGGER_TRAINING_MODEL);
		} catch(IOException ie){
			ie.printStackTrace();
		}
	}

}
