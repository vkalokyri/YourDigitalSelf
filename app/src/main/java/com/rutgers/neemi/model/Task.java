package com.rutgers.neemi.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "Task")
public class Task implements Serializable{

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField
	String name;
	@DatabaseField
	String oid;
	@DatabaseField(canBeNull = true)
	boolean isGateway;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "script_id")
	Script script;

	Object pid;
	ArrayList<LocalValues> localValues;


	public Task(){
		this.localValues=new ArrayList<LocalValues>();
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

	public Object getPid() {
		return pid;
	}

	public void setPid(Object pid) {
		this.pid = pid;
	}

	public ArrayList<LocalValues> getLocalValues() {
		return localValues;
	}
	public void setLocalValues(ArrayList<LocalValues> locals) {
		this.localValues = locals;
	}
	public void addLocalValue(LocalValues sublocal) {
		this.localValues.add(sublocal);
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}
}
