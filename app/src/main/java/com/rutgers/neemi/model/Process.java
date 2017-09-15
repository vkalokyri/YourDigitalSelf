package com.rutgers.neemi.model;

import java.util.ArrayList;
import java.util.List;

public class Process{

	List<Locals> locals;
	List<Task> tasks;
	float score=0;
	
	
	public Process(){	
		this.tasks=new ArrayList<Task>();
		this.locals=new ArrayList<Locals>();
	}
	
	
	public List<Locals> getLocals() {
		return locals;
	}
	public void setLocals(List<Locals> locals) {
		this.locals = locals;
	}
	public void addSubLocal(Locals sublocal) {
		this.locals.add(sublocal);
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
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
	
	
	
}
