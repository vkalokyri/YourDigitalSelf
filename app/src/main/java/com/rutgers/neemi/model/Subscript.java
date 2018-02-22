package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;

public class Subscript {

	@DatabaseField(foreign = true, columnName = "superscript_id")
	ScriptDefinition superscript_id;
	@DatabaseField(foreign = true, columnName = "subscript_id")
	ScriptDefinition subscript_id;

	public Subscript(){

	}

	public ScriptDefinition getSuperscript_id() {
		return superscript_id;
	}

	public void setSuperscript_id(ScriptDefinition superscript_id) {
		this.superscript_id = superscript_id;
	}

	public ScriptDefinition getSubscript_id() {
		return subscript_id;
	}

	public void setSubscript_id(ScriptDefinition subscript_id) {
		this.subscript_id = subscript_id;
	}
}
