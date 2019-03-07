package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "Subscript")
public class Subscript implements Serializable {

	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES ScriptDefinition(_id) ON DELETE CASCADE")
	ScriptDefinition superscript;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES ScriptDefinition(_id) ON DELETE CASCADE")
	ScriptDefinition subscript;

	public Subscript(){

	}

	public Subscript(ScriptDefinition superscript, ScriptDefinition subscript) {
		this.superscript = superscript;
		this.subscript = subscript;
	}

	public ScriptDefinition getSuperscript_id() {
		return superscript;
	}

	public void setSuperscript_id(ScriptDefinition superscript_id) {
		this.superscript = superscript_id;
	}

	public ScriptDefinition getSubscript_id() {
		return subscript;
	}

	public void setSubscript_id(ScriptDefinition subscript_id) {
		this.subscript = subscript_id;
	}
}
