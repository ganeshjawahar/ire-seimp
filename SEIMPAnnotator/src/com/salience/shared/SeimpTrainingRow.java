package com.salience.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeimpTrainingRow implements Serializable{
	private String text = null;
	private List<String> imageList = null, mergedNeList = null;
	private long _id = -1, userId = -1;
	private Date createdAt = null;
	private int favoriteCount = 0, retweetCount = 0;
	private List<Annotation> annotationList = null;
	private List<NERList> nerList = null;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<String> getImageList() {
		return imageList;
	}

	public void setImageList(List<String> imageList) {
		this.imageList = imageList;
	}

	public List<String> getMergedNeList() {
		return mergedNeList;
	}

	public void setMergedNeList(List<String> mergedNeList) {
		this.mergedNeList = mergedNeList;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public int getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}

	public List<Annotation> getAnnotationList() {
		return annotationList;
	}

	public void setAnnotationList(List<Annotation> annotationList) {
		this.annotationList = annotationList;
	}

	public List<NERList> getNerList() {
		return nerList;
	}

	public void setNerList(List<NERList> nerList) {
		this.nerList = nerList;
	}

	public void addImage(final String img) {
		if (imageList == null)
			imageList = new ArrayList<String>();
		imageList.add(img);
	}
	
	public void addAnnotation(final Annotation ann) {
		if (annotationList == null)
			annotationList = new ArrayList<Annotation>();
		annotationList.add(ann);
	}
	
	public void setAnnotation(final Annotation ann,final int index){
		if(annotationList!=null && 0<=index && index<=annotationList.size())
			annotationList.set(index,ann);
	}

}