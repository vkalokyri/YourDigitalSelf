package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;

public class TaskDefinition {

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField
	String name;

	ArrayList<LocalProperties> locals;

	public TaskDefinition()
	{
		this.locals=new ArrayList<LocalProperties>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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