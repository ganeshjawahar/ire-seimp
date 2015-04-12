package com.salience.collect;

import java.util.Date;
import java.util.List;

public class Annotation {

	String comments, annotator;
	List<String> sneList;

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAnnotator() {
		return annotator;
	}

	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public List<String> getSneList() {
		return sneList;
	}

	public void setSneList(List<String> sneList) {
		this.sneList = sneList;
	}

}
