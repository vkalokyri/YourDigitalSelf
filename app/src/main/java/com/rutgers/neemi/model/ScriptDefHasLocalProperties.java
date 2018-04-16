package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "ScriptDefHasLocalProperties")
public class ScriptDefHasLocalProperties implements Serializable {


	@DatabaseField(foreign = true, foreignAutoRefresh = true )
	ScriptDefinition scriptDefinition;

	// This is a foreign object which just stores the id from the Post object in this table.
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	LocalProperties localProperties;


    public ScriptDefHasLocalProperties() {
    }

    public ScriptDefHasLocalProperties(ScriptDefinition scriptDefinition, LocalProperties localProperties) {
		this.scriptDefinition = scriptDefinition;
		this.localProperties = localProperties;
	}


	public ScriptDefinition getScriptDefinition() {
		return scriptDefinition;
	}

	public void setScriptDefinition(ScriptDefinition scriptDefinition) {
		this.scriptDefinition = scriptDefinition;
	}

	public LocalProperties getLocalProperties() {
		return localProperties;
	}

	public void setLocalProperties(LocalProperties localProperties) {
		this.localProperties = localProperties;
	}
}
