package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "TaskDefinition")
public class TaskDefinition implements Serializable {

	@DatabaseField(generatedId = true)
	int _id;
	@DatabaseField
	String name;

	ArrayList<LocalProperties> locals;

	public TaskDefinition(String name){
		this.name=name;
	}
	public TaskDefinition()
	{
		this.locals=new ArrayList<LocalProperties>();
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public ArrayList<LocalProperties> getLocals() {
		return locals;
	}
	public void setLocals(ArrayList<LocalProperties> locals) {
		this.locals = locals;
	}
	public void addSubLocal(LocalProperties sublocal) {
		this.locals.add(sublocal);
	}


}