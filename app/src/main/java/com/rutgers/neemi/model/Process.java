package com.rutgers.neemi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Process{

	String id;
	String name;
	List<Locals> locals;
	HashMap<String, Task> tasks;
	List<Process> subprocesses;
	float score=0;

	public Process(){	
		this.tasks=new HashMap<String, Task>();
		this.locals=new ArrayList<Locals>();
		this.subprocesses=new ArrayList<Process>();
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

	public HashMap<String, Task> getTasks() {
		return tasks;
	}
	public void setTasks(HashMap<String, Task> tasks) {
		this.tasks = tasks;
	}
	public void addTask(String name, Task task) {
		this.tasks.put(name, task);
	}


	public List<Process> getSubprocesses() {
		return subprocesses;
	}

	public void setSubprocesses(List<Process> subprocesses) {
		this.subprocesses = subprocesses;
	}

	public void addSubprocess(Process process) {
		this.subprocesses.add(process);
	}

	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
