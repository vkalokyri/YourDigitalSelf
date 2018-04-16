package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

public class Subscript implements Serializable {

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	ScriptDefinition superscript;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
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
