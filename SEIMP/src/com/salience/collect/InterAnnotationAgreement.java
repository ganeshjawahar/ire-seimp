package com.salience.collect;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.salience.AppGlobals;
import com.salience.MongoDbManager;
import com.salience.Utilities;

public class InterAnnotationAgreement {
	
	public static void percentageAgreement(final String dbName,final String collectionName) throws ClassNotFoundException{
		//Computes the percentage of cases on which annotators agree.
		final DBCollection dbc=MongoDbManager.getCollection(dbName, collectionName);
		final DBCursor cursor=dbc.find();
		double score=0.0;
		int count=0;
		while(cursor.hasNext()){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities.convertToPOJO(cursor.next().toString(),"com.salience.collect.SeimpTrainingRow");
			if(computeScore(row.getAnnotationList())==true)
				score++;
			count++;			
		}
		score/=count;
		System.out.println("Percentage agreement - "+score);
	}
	
	private static boolean computeScore(List<Annotation> annotationList) {
		//Returns true if all the annotators are in agreement.
		if(annotationList==null || annotationList.size()==1) return true;
		final List<String> annotations=annotationList.iterator().next().getSneList();
		for(int index=1;index<annotationList.size();index++)
			if(annotationList.get(index).getSneList()!=null)
				for(final String sne:annotationList.get(index).getSneList())
					if(annotations!=null && annotations.indexOf(sne)==-1)
						return false;					
		return true;
	}
	
	public static void fleissKappa(final String dbName,final String collectionName,final int annotatorsCount) throws ClassNotFoundException{
		//Compute the fleiss score based on https://www.youtube.com/watch?v=KLoeZstQz0E
		final DBCollection dbc=MongoDbManager.getCollection(dbName, collectionName);
		DBCursor cursor=dbc.find();
		
		//Get the categories.
		final List<String> categories=new ArrayList<String>();
		categories.add("null");
		int rowSize=0;
		while(cursor.hasNext()){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities.convertToPOJO(cursor.next().toString(),"com.salience.collect.SeimpTrainingRow");
			if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			for(final String sne:row.getMergedNeList())
				if(categories.indexOf(sne)==-1)
					categories.add(sne);
			++rowSize;
		}
		System.out.println(rowSize);
		int colSize=categories.size();
		
		//Compute the rowsum, colsum.
		cursor=dbc.find();
		int globalRowVal[]=new int[rowSize];
		int globalColVal[]=new int[colSize];
		int curRowIndex=0;
		while(cursor.hasNext()){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities.convertToPOJO(cursor.next().toString(),"com.salience.collect.SeimpTrainingRow");
			if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			if(row.getAnnotationList()==null){
				globalRowVal[curRowIndex]=1;
				globalColVal[categories.indexOf("null")]+=annotatorsCount;
			} else {
				int localColVal[]=new int[colSize];
				//Fill this tweet's category frequency table.
				for(int index=0;index<row.getAnnotationList().size();index++)
					if(row.getAnnotationList().get(index).getSneList()!=null)
						for(final String sne:row.getAnnotationList().get(index).getSneList()) {
							localColVal[categories.indexOf(sne)]+=1;
							globalColVal[categories.indexOf(sne)]+=1;
						}
				int rowVal=0;
				for(int index=0;index<colSize;index++)
					rowVal+=localColVal[index]*localColVal[index];
				rowVal=(rowVal-annotatorsCount)/(annotatorsCount*(annotatorsCount-1));
				globalRowVal[curRowIndex]=rowVal;							
			}			
			++curRowIndex;			
		}
		
		//Calculate p_bar
		double p_bar=0;
		for(int index=0;index<rowSize;index++)
			p_bar+=globalRowVal[index];
		p_bar=p_bar/rowSize;
		
		//Calculate p_e
		double p_e=0;
		for(int index=0;index<colSize;index++)
			p_e+=((globalColVal[index]/(rowSize*annotatorsCount))*(globalColVal[index]/(rowSize*annotatorsCount)));
		
		double fleissKappa=(p_bar-p_e)/(1-p_e);
		System.out.println("Kappa score - "+fleissKappa);	
	}

	public static void main(final String[] argv) throws Exception{
		percentageAgreement(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
		fleissKappa(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,3);
	}

}
