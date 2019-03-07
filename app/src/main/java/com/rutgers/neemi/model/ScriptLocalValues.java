package com.rutgers.neemi.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "ScriptLocalValues")
public class ScriptLocalValues implements Serializable {

	@DatabaseField(foreign=true, foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Script(_id) ON DELETE CASCADE")
	Script script;

	@DatabaseField(foreign=true, foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES LocalProperties(_id) ON DELETE CASCADE")
	LocalProperties localProperties;

//	@DatabaseField(foreign=true, foreignAutoRefresh=true)
//	Task task;

	@DatabaseField
	String local_value;


	public ScriptLocalValues(){

	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
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


	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof ScriptLocalValues) {
			ScriptLocalValues locals = (ScriptLocalValues) obj;
			if ((locals.getLocalProperties().w5h_value == null && this.getLocalProperties().w5h_value == null) ||
			(locals.getLocalProperties().w5h_value .equals(this.getLocalProperties().w5h_value) && ((locals.getLocal_value() == null && this.getLocal_value() == null)
					|| locals.getLocal_value().equals(this.getLocal_value())))) {
				return true;
			}
		}
		return false;
	}

}
