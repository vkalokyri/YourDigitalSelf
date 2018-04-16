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
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	Script script;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	TaskDefinition taskDefinition;


	Object pid;


	ArrayList<TaskLocalValues> localValues;


	public Task(){
		this.localValues=new ArrayList<TaskLocalValues>();
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

	public ArrayList<TaskLocalValues> getLocalValues() {
		return localValues;
	}
	public void setLocalValues(ArrayList<TaskLocalValues> locals) {
		this.localValues = locals;
	}
	public void addLocalValue(TaskLocalValues sublocal) {
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

	public TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	public void setTaskDefinition(TaskDefinition taskDefinition) {
		this.taskDefinition = taskDefinition;
	}
}
