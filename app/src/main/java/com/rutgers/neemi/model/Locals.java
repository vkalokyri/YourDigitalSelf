package com.rutgers.neemi.model;

import java.util.ArrayList;
import java.util.List;

public class Locals {

	String w5h_label;
	List<String> value;
		

	public Locals(){
		value = new ArrayList<String>();
	}
	
	public String getW5h_label() {
		return w5h_label;
	}


	public void setW5h_label(String w5h_label) {
		this.w5h_label = w5h_label;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public void addValue(String addedvalue) {
		this.value.add(addedvalue);
	}

	

	
	
	
	
}
