package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "TaskDefHasLocalProperties")
public class TaskDefHasLocalProperties implements Serializable {


	@DatabaseField(foreign = true, foreignAutoRefresh = true,columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES TaskDefinition(_id) ON DELETE CASCADE" )
	TaskDefinition taskDefinition;

	// This is a foreign object which just stores the id from the Post object in this table.
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES LocalProperties(_id) ON DELETE CASCADE")
	LocalProperties localProperties;


    public TaskDefHasLocalProperties() {
    }

    public TaskDefHasLocalProperties(TaskDefinition taskDefinition, LocalProperties localProperties) {
		this.taskDefinition = taskDefinition;
		this.localProperties = localProperties;
	}


	public TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	public void setTaskDefinition(TaskDefinition scriptDefinition) {
		this.taskDefinition = scriptDefinition;
	}

	public LocalProperties getLocalProperties() {
		return localProperties;
	}

	public void setLocalProperties(LocalProperties localProperties) {
		this.localProperties = localProperties;
	}
}
