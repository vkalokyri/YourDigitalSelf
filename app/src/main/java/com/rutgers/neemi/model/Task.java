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
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Script(_id) ON DELETE CASCADE")
	Script script;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES TaskDefinition(_id) ON DELETE CASCADE")
	TaskDefinition taskDefinition;

	ArrayList<String> list_of_oids = new ArrayList<>();
	ArrayList<Object> list_of_pids = new ArrayList<>();

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

	public void addPid(Object pid){
		this.list_of_pids.add(pid);
	}

	public void addOid(String oid){
		this.list_of_oids.add(oid);
	}

	public ArrayList<String> getList_of_oids() {
		return list_of_oids;
	}

	public void setList_of_oids(ArrayList<String> list_of_oids) {
		this.list_of_oids = list_of_oids;
	}

	public ArrayList<Object> getList_of_pids() {
		return list_of_pids;
	}

	public void setList_of_pids(ArrayList<Object> list_of_pids) {
		this.list_of_pids = list_of_pids;
	}
}
