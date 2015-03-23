package com.salience.ner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordCrf {

	public static List<String> recognizeNE(String tweet) throws IOException {
		// Find the NE in tweets using Stanford CRF tagger.
		tweet = tweet.replaceAll("@", "").replaceAll("#", "");

		// create an empty Annotation just with the given text
		final Annotation doc = new Annotation(tweet);
		pipeline.annotate(doc);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		final List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		final List<String> neList=new ArrayList<String>();

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);
				if(ne.equals("PERSON") || ne.equals("ORGANIZATION") || ne.equals("LOCATION"))
					neList.add(word);
			}
		}

		return neList;
	}
	
	public static void main(final String[] argv) throws IOException{
		//String tweet="@nealbrennan @TimmyTops23 @Travon @OwenBenjamin @moshekasher @JuddApatow my first choice to play me http://t.co/mHiFtBDYZq";
		String tweet=" am proud to unveil the #NATOSummitUK logo w/ @WilliamJHague #ForMin #Cymru #MyWales http://t.co/OPpY5kjk65";
		System.out.println(recognizeNE(tweet));
		System.out.println(AlanRitter.recognizeNE(tweet));
		System.out.println(ArkTweet.recognizeNE(tweet));
	}

	private static StanfordCoreNLP pipeline = null;
	static {
		final Properties prop = new Properties();
		prop.setProperty("annotators",
				"tokenize, ssplit, pos, lemma, ner, parse");
		pipeline = new StanfordCoreNLP(prop);
	}

}

