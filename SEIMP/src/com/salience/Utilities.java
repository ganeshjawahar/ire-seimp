package com.salience;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.salience.ner.AlanRitter;
import com.salience.ner.ArkTweet;
import com.salience.ner.StanfordCrf;

public class Utilities {

	public static String makeGetCall(final String urlStr) throws IOException {
		// Make a Get call with the supplied url, and return the response.
		final URL url = new URL(urlStr);
		final HttpURLConnection uc = (HttpURLConnection) url.openConnection();
		uc.setRequestMethod("GET");
		final int rspCode = uc.getResponseCode();
		if (rspCode != 200) {
			if (AppGlobals.IS_DEBUG)
				System.out.println(rspCode + "-Failed to reach " + urlStr);
			return null;
		}
		final BufferedReader br = new BufferedReader(new InputStreamReader(
				uc.getInputStream()));
		final String response = readFromStream(br);
		br.close();
		return response;
	}

	public static String makePostCall(final String urlStr, final String text, final Proxy proxy)
			throws IOException {
		// Make a Post call with the supplied url and text as payload, and return the response.
		final URL url=new URL(urlStr);
		final URLConnection urlConnection=(proxy==null?url.openConnection():url.openConnection(proxy));
		// Set the payload
		final HttpURLConnection uc = (HttpURLConnection) urlConnection;
		uc.setDoOutput(true);
		uc.setRequestMethod("POST");
		uc.setRequestProperty("Content-type", "text/plain");
		final OutputStream os = uc.getOutputStream();
		os.write(text.getBytes());
		os.flush();

		final int rspCode = uc.getResponseCode();
		if (rspCode != 200) {
			// Not a good response.
			if (AppGlobals.IS_DEBUG)
				System.out.println(rspCode + "-Failed to reach " + urlStr+" with payload "+text);
			return null;
		}

		// Get the result
		final BufferedReader br = new BufferedReader(new InputStreamReader(
				uc.getInputStream()));
		final String response = readFromStream(br);
		br.close();

		uc.disconnect();
		return response;
	}

	public static String readFromStream(final BufferedReader br)
			throws IOException {
		// Read entire stream to a string and return it.
		final StringBuffer buff = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null)
			buff.append(line + "\n");
		return buff.toString();
	}

	public static List<String> mergeNER(final String text,
			final AppGlobals.NER... nerModules) throws IOException {
		// Merge (n) NE list and return the unique ones.
		if (nerModules.length == 0)
			return null; // empty list

		// Merge all the list into one.
		List<String> resNEList = new ArrayList<String>();
		resNEList.addAll(doNER(nerModules[0], text));
		for (int listIndex = 1; listIndex < nerModules.length; listIndex++) {
			resNEList = merge2NEList(resNEList,
					doNER(nerModules[listIndex], text));
		}

		return resNEList;
	}

	public static List<String> doNER(final AppGlobals.NER nerModule,
			final String text) throws IOException {
		// Call the NER module for the text.

		if (nerModule == AppGlobals.NER.ALAN_RITTER)
			return AlanRitter.recognizeNE(text);
		else if (nerModule == AppGlobals.NER.ARK_TWEET)
			return ArkTweet.recognizeNE(text);
		else if (nerModule == AppGlobals.NER.STANFORD_CRF)
			return StanfordCrf.recognizeNE(text);

		return null; // incorrect ner module supplied.
	}

	public static List<String> selectElements(final List<String> list,
			int start, int end) {
		// Generated a new list out of elements that lies within [start,end]
		// limit.
		if (list.size() == 0 || !(0 <= start && end < list.size()))
			return null;
		final List<String> newList = new ArrayList<String>();
		for (int index = start; index <= end; index++)
			newList.add(String.valueOf(list.get(index)));
		return newList;
	}

	private static List<String> merge2NEList(List<String> neList1,
			List<String> neList2) {
		// Merges two NE list, and returns the unique ones.
		final HashMap<String, Integer> neMap = new HashMap<String, Integer>();
		if (neList1.size() == 0) {
			if (neList2.size() <= 1)
				return neList2;
			neList1 = selectElements(neList2, 0, 0);
			neList2 = selectElements(neList2, 1, neList2.size() - 1);
		}
		if (neList2.size() == 0) {
			if (neList1.size() == 1)
				return neList1;
			neList2 = selectElements(neList1, 1, neList1.size() - 1);
			neList1 = selectElements(neList1, 0, 0);
		}
		for (final String ne1 : neList1) {
			for (final String ne2 : neList2) {
				if (ne1.equals(ne2)) {
					if (neMap.get(ne1) == null || (neMap.get(ne1) != null && neMap.get(ne1) != -1))
						neMap.put(ne1, 0);
				} else if (ne1.contains(ne2)) {
					neMap.put(ne1, 0);
					neMap.put(ne2, -1);
				} else if (ne2.contains(ne1)) {
					neMap.put(ne2, 0);
					neMap.put(ne1, -1);
				} else {
					if (!AppGlobals.STOP_WORD_LIST.contains(ne1)) {
						if (neMap.get(ne1) == null
								|| (neMap.get(ne1) != null && neMap.get(ne1) != -1))
							neMap.put(ne1, 0);
					}
					if (!AppGlobals.STOP_WORD_LIST.contains(ne2)) {
						if (neMap.get(ne2) == null
								|| (neMap.get(ne2) != null && neMap.get(ne2) != -1))
							neMap.put(ne2, 0);
					}
				}
			}
		}

		// Get the ne's where value is 0.
		final List<String> resNEList = new ArrayList<String>();
		for (final Entry<String, Integer> entry : neMap.entrySet())
			if (entry.getValue() == 0)
				resNEList.add(entry.getKey());
		return new ArrayList<String>(resNEList);
	}

	private static final Gson gson = new Gson();

	public static String convertToJson(final Object content) {
		// Converts the pojo rep. to json rep.
		return gson.toJson(content);
	}

	public static Object convertToPOJO(final String content,
			final String pojoClassName) throws ClassNotFoundException {
		// Converts the content in JSON format to POJO.
		Class clazz = Class.forName(pojoClassName);		
		return gson.fromJson(content, clazz);
	}
	
	private static void createSecondaryIndex(final String orig_file,final String sec_file) throws IOException{
		//Creates the secondary index for a file
		final PrintWriter secWriter=new PrintWriter(sec_file);
		final BufferedReader reader=new BufferedReader(new FileReader(orig_file));
		String line=null;
		int bytes=0;
		while((line=reader.readLine())!=null){
			
			bytes+=line.length();
		}
		reader.close();
		secWriter.close();		
	}

	public static double getNgramProbability(String ngram) {
		String response=null;
		try{
			response=makePostCall(AppGlobals.POST_MICROSOFT_WEB_NGRAM_ACCESS_ENDPOINT,ngram,AppGlobals.HTTP_PROXY);
			if(response!=null) //Got a valid response.
				return Double.parseDouble(response.substring(0, response.indexOf("\n")));
		} catch(IOException e) {
			e.printStackTrace();
		}		
		return 0;
	}
	
	public static boolean contains(final List<String> list,final String str){
		//Utility to check if given 'str' is present as substring in atleast one entry in the list.
		for(final String entry:list)
			if(entry.toLowerCase().contains(str.toLowerCase()))
				return true;
		return false;
	}

}
