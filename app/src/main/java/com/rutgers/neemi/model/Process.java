package com.rutgers.neemi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Process{

	String id;
	String name;
	List<Locals> locals;
	HashMap<String, Task> tasksMap;
	List<Task> tasks;
	List<Process> subprocesses;
	float score=0;

	public Process(){	
		this.tasksMap=new HashMap<String, Task>();
		this.tasks = new ArrayList<Task>();
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

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void addTask(Task task) {
		this.tasks.add(task);
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
