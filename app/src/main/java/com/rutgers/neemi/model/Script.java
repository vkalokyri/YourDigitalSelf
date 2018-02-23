package com.rutgers.neemi.model;

import android.content.Context;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Script {

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField(foreign = true, columnName = "script_id")
	ScriptDefinition scriptDefinition;
	@DatabaseField
	String timestamp;
	@DatabaseField
	float score=0;

	ArrayList<LocalValues> localValues;
	HashMap<String, Task> tasksMap;
	List<Task> tasks;
	ArrayList<Script> subscripts;




	public Script(){
		this.tasksMap=new HashMap<String, Task>();
		this.tasks = new ArrayList<Task>();
		this.localValues=new ArrayList<LocalValues>();
		this.subscripts=new ArrayList<Script>();
	}

	public ArrayList<LocalValues> getLocalValues() {
		return localValues;
	}
	public void setLocals(ArrayList<LocalValues> localValues) {
		this.localValues = localValues;
	}
	public void addLocalValue(LocalValues sublocal) {
		this.localValues.add(sublocal);
	}


	public ScriptDefinition getScriptDefinition() {
		return scriptDefinition;
	}

	public void setScriptDefinition(ScriptDefinition scriptDefinition) {
		this.scriptDefinition = scriptDefinition;
	}

	public HashMap<String, Task> getTaskMap() {
		return tasksMap;
	}
	public void setTaskMap(HashMap<String, Task> tasks) {
		this.tasksMap = tasks;
	}
	public void addTaskMap(String name, Task task) {
		this.tasksMap.put(name, task);
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void addTask(Task task) {
		this.tasks.add(task);
	}

	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Script> getSubscripts() {
		return subscripts;
	}

	public void setSubscripts(ArrayList<Script> subscripts) {
		this.subscripts = subscripts;
	}

	public void addSubscript(Script subscript) {
		this.subscripts.add(subscript);
	}


	//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}\


	public Script assignScore(Context context){
		ConfigReader config = new ConfigReader(context);
		List<Task> tasks = this.getTasks();

		float instanceScore = this.getScore();
		for (Task processTask : tasks) {
			Object pid = processTask.getPid();
			float addedScore=0;
			if (pid instanceof Payment ){
				addedScore = Float.parseFloat(config.getStr(PROPERTIES.BANK_WEIGHT));
			}else if (pid instanceof Email){
				addedScore = Float.parseFloat(config.getStr(PROPERTIES.EMAIL_WEIGHT));
				String from =((Email)pid).getFrom();
				if(((String)from).contains("member_services@opentable.com")){
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.OPENTABLE_WEIGHT));
				}
				else if(((String)from).contains("calendar-notification@google.com")){
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.GCAL_WEIGHT));
				}


			}else if (pid instanceof Event){
				addedScore = Float.parseFloat(config.getStr(PROPERTIES.GCAL_WEIGHT));
			}else if (pid instanceof Photo){
				addedScore = Float.parseFloat(config.getStr(PROPERTIES.FACEBOOK_WEIGHT));
			}
			float newScore = scoreFunction(instanceScore, addedScore);
			instanceScore=newScore;
			this.setScore(newScore);
		}
		return this;

	}

	public float scoreFunction(float previousScore,float addedScore){
		return (1-((1-previousScore)*(1-addedScore)));
	}



}
