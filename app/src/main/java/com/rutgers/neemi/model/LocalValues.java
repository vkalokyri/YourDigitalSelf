package com.rutgers.neemi.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "LocalValues")
public class LocalValues implements Serializable {

	@DatabaseField(foreign=true, columnName = "s_id")
	Script script;

	@DatabaseField(foreign=true, columnName = "local_id")
	LocalProperties local;

	@DatabaseField(foreign=true, columnName = "t_id")
	Task task;

	@DatabaseField
	String local_value;


	public LocalValues(){

	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public LocalProperties getLocalProperties() {
		return local;
	}

	public void setLocalProperties(LocalProperties local) {
		this.local = local;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getValue() {
		return local_value;
	}

	public void setValue(String value) {
		this.local_value = value;
	}
}
