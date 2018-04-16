package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "TaskLocalValues")
public class TaskLocalValues implements Serializable {


	@DatabaseField(foreign=true, foreignAutoRefresh=true)
	Task task;

	@DatabaseField(foreign=true, foreignAutoRefresh=true)
	LocalProperties localProperties;

	@DatabaseField
	String local_value;


	public TaskLocalValues(){

	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public LocalProperties getLocalProperties() {
		return localProperties;
	}

	public void setLocalProperties(LocalProperties localProperties) {
		this.localProperties = localProperties;
	}

	public String getLocal_value() {
		return local_value;
	}

	public void setLocal_value(String local_value) {
		this.local_value = local_value;
	}
}
