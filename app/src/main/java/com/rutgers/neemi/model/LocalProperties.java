package com.rutgers.neemi.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "LocalProperties")
public class LocalProperties implements Serializable {

	@DatabaseField(generatedId = true)
	int label_id;

	@DatabaseField
	String w5h_label;

	@DatabaseField
	String w5h_value;

	//@DatabaseField(foreign=true, foreignAutoRefresh=true)
	//ScriptDefinition script;

	//@DatabaseField(foreign=true, foreignAutoRefresh=true)
	//TaskDefinition task;


	public LocalProperties(int id, String w5h_label, String w5h_value){
		this.label_id=id;
		this.w5h_label=w5h_label;
		this.w5h_value=w5h_value;
	}

	public LocalProperties(){
	}
	
	public String getW5h_label() {
		return w5h_label;
	}


	public void setW5h_label(String w5h_label) {
		this.w5h_label = w5h_label;
	}

	public String getW5h_value() {
		return w5h_value;
	}

	public void setW5h_value(String w5h_value) {
		this.w5h_value = w5h_value;
	}

//	public ScriptDefinition getScriptDef() {
//		return script;
//	}
//
//	public void setScriptDef(ScriptDefinition script) {
//		this.script = script;
//	}
//
//	public TaskDefinition getTaskDef() {
//		return task;
//	}
//
//	public void setTaskDef(TaskDefinition task) {
//		this.task = task;
//	}

	//	public ArrayList<String> getValue() {
//		return value;
//	}
//
//	public void setValue(ArrayList<String> value) {
//		this.value=value;
//	}
//
//	public void addValue(String addedvalue) {
//		this.value.add(addedvalue);
//	}

}
