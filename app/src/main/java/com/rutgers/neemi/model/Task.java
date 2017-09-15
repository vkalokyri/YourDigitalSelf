package com.rutgers.neemi.model;

import java.util.ArrayList;
import java.util.List;

public class Task {

	String id;
	String name;
	Object pid;
	List<Locals> locals;
	
	public Task(){
		this.locals=new ArrayList<Locals>();
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

	public Object getPid() {
		return pid;
	}

	public void setPid(Object pid) {
		this.pid = pid;
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
		
	
}
