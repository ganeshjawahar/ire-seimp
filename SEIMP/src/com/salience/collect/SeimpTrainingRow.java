package com.salience.collect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.salience.meij.Entity;

public class SeimpTrainingRow {
	private String text = null;
	private List<String> imageList = null, neList = null, sneList = null;
	private long _id = -1, userId = -1;

	private Date createdAt = null;
	private int favoriteCount = 0, retweetCount = 0;

	private String annotator = null, comments = null;

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	private Date lastUpdated = null;

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

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

	public List<String> getNeList() {
		return neList;
	}

	public void setNeList(List<String> neList) {
		this.neList = neList;
	}

	public List<String> getSneList() {
		return sneList;
	}

	public void setSneList(List<String> sneList) {
		this.sneList = sneList;
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

	public String getAnnotator() {
		return annotator;
	}

	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void addImage(final String img) {
		if (imageList == null)
			imageList = new ArrayList<String>();
		imageList.add(img);
	}

}