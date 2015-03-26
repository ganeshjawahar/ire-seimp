package com.salience.shared;

import java.io.Serializable;
import java.util.List;

public class NERList implements Serializable {

	String name;
	List<String> neList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getNeList() {
		return neList;
	}

	public void setNeList(List<String> neList) {
		this.neList = neList;
	}

}
