package com.salience.meij;

import java.util.ArrayList;
import java.util.List;

public class MeijTrainingSet {
	
	private List<MeijTrainingRow> data=null;

	public List<MeijTrainingRow> getData() {
		return data;
	}

	public void setData(List<MeijTrainingRow> data) {
		this.data = data;
	}	
	
	public void addNewRow(MeijTrainingRow row){
		if(data==null) data=new ArrayList<MeijTrainingRow>();
		data.add(row);
	}
}
