package com.rutgers.neemi.model;

import android.content.Context;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@DatabaseTable(tableName = "Script")
public class Script implements Serializable {

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField(foreign = true, foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES ScriptDefinition(_id) ON DELETE CASCADE")
	ScriptDefinition scriptDefinition;
	@DatabaseField
	String timestamp;
	@DatabaseField
	float score=0;
	//String name;
	//String ofType;

	ArrayList<ScriptLocalValues> scriptlocalValues;
//	HashMap<String, Task> tasksMap;
	List<Task> tasks;
	ArrayList<Script> subscripts;

	public Script(){
		//this.tasksMap=new HashMap<String, Task>();
		this.tasks = new ArrayList<Task>();
		this.scriptlocalValues=new ArrayList<ScriptLocalValues>();
		this.subscripts=new ArrayList<Script>();
	}

	public ArrayList<ScriptLocalValues> getLocalValues() {
		return scriptlocalValues;
	}
	public void setLocals(ArrayList<ScriptLocalValues> localValues) {
		this.scriptlocalValues = localValues;
	}
	public void addLocalValue(ScriptLocalValues sublocal) {
		this.scriptlocalValues.add(sublocal);
	}

	public ScriptDefinition getScriptDefinition() {
		return scriptDefinition;
	}

	public void setScriptDefinition(ScriptDefinition scriptDefinition) {
		this.scriptDefinition = scriptDefinition;
	}

//	public HashMap<String, Task> getTaskMap() {
//		return tasksMap;
//	}
//	public void setTaskMap(HashMap<String, Task> tasks) {
//		this.tasksMap = tasks;
//	}
//	public void addTaskMap(String name, Task task) {
//		this.tasksMap.put(name, task);
//	}
//
	public List<Task> getTasks() {
		return tasks;
	}
	public void  setTasks(List<Task> tasks) {
		this.tasks=tasks;
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
//
//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}
//
	public ArrayList<Script> getSubscripts() {
		return subscripts;
	}

	public void setSubscripts(ArrayList<Script> subscripts) {
		this.subscripts = subscripts;
	}

	public void addSubscript(Script subscript) {
		this.subscripts.add(subscript);
	}

//	public String getOfType() {
//		return ofType;
//	}
//
//	public void setOfType(String ofType) {
//		this.ofType = ofType;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}


	public void merge(Script b){
		ScriptDefinition s1 = this.getScriptDefinition();
		ScriptDefinition s2 = b.getScriptDefinition();
		if (s1.getName().equalsIgnoreCase(s2.getName())){
			//merge the non equal values
			this.getLocalValues().addAll(b.getLocalValues()); //merge local properties

			for(int i=0;i<this.getLocalValues().size();i++){
				for(int j=i+1;j<this.getLocalValues().size();j++){
					if (this.getLocalValues().get(i).equals(this.getLocalValues().get(j))){
						this.getLocalValues().remove(j);
					}
				}
			}

			ArrayList<Script> temp = new ArrayList<Script>();
			if (this.getSubscripts().size()>0){
				if(b.getSubscripts().size()>0){
					for (Script bSubscript : b.getSubscripts()) {
						boolean merged=false;
						for(Script thisSubscript: this.getSubscripts()) {

							if(thisSubscript.getScriptDefinition().getName().equalsIgnoreCase(bSubscript.getScriptDefinition().getName()) && thisSubscript.getScriptDefinition().getOfType().equalsIgnoreCase(bSubscript.getScriptDefinition().getOfType())) {
								thisSubscript.merge(bSubscript);
								merged=true;
							}else{


							}
						}
						if(!merged)
							temp.add(bSubscript);
					}
					if (temp!=null){
						if(temp.size()>0){
							this.getSubscripts().addAll(temp);
						}
					}
					//the second doesn't have any other subscripts
				}else{
					if(b.getTasks().size()>0){
						this.getTasks().addAll(b.getTasks());
					}
				}
			}else{
				this.getSubscripts().addAll(b.getSubscripts());
			}
			this.getTasks().addAll(b.getTasks());
		}
	}



	public Script assignScore(Context context){
		ConfigReader config = new ConfigReader(context);
		List<Task> tasks = this.getTasks();
		float addedScore = 0;

		float instanceScore = this.getScore();
		for (Task processTask : tasks) {
			Object pid = processTask.getPid();
			if (pid != null) {
				if (pid instanceof Transaction) {
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.BANK_WEIGHT));
				} else if (pid instanceof Email) {
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.EMAIL_WEIGHT));
					String from = ((Email) pid).getFrom().email;
					if (((String) from).contains("opentable.com")) {
						addedScore = Float.parseFloat(config.getStr(PROPERTIES.OPENTABLE_WEIGHT));
					} else if (((String) from).contains("calendar-notification@google.com")) {
						addedScore = Float.parseFloat(config.getStr(PROPERTIES.GCAL_WEIGHT));
					}
				} else if (pid instanceof Event) {
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.GCAL_WEIGHT));
				} else if (pid instanceof Photo) {
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.FACEBOOK_WEIGHT));
				}else if(pid instanceof Message){
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.MESSENGER_WEIGHT));
				}
			} else if (processTask.getList_of_pids() != null) {
			  	if (processTask.getList_of_pids().get(0) instanceof Place) {
					addedScore = Float.parseFloat(config.getStr(PROPERTIES.GPS_WEIGHT));
				}
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
