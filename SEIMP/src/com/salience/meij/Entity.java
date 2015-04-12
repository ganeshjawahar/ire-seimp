package com.salience.meij;

public class Entity {
	private String name=null,_id=null;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public Entity(){}
	public Entity(final String name,final String wikiId){
		this.name=name;
		this._id=wikiId;
	}
}