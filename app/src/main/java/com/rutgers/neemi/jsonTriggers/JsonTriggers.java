package com.rutgers.neemi.jsonTriggers;

import android.content.Context;

import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.LinkedList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;


public class JsonTriggers implements Triggers {
	
	
	ConfigReader config;
	LinkedList<String> strongTriggers;
	LinkedList<String> weakTriggers;
	
	public JsonTriggers(Context context){
		config = ConfigReader.getInstance();
		 try {
			 	InputStream fis = context.getAssets().open(config.getStr(PROPERTIES.TRIGGERS_FILE));
				
				//create JsonReader object
				JsonReader jsonReader = Json.createReader(fis);
					 
	            JsonObject jsonObject = jsonReader.readObject();
	            jsonReader.close();
	    		fis.close();
	            
	    		JsonObject scriptObject = jsonObject.getJsonObject(config.getStr(PROPERTIES.SCRIPT));


	    		
	    		JsonArray jsonStrongArray = scriptObject.getJsonArray("strongTriggers");
	    		this.strongTriggers = new LinkedList<String>();
	    		for(JsonValue value : jsonStrongArray){
	    			strongTriggers.add(value.toString());
	    		}
	 
	    		JsonArray jsonWeakArray = scriptObject.getJsonArray("weakTriggers");
	    		this.weakTriggers = new LinkedList<String>();
	    		for(JsonValue value : jsonWeakArray){
	    			weakTriggers.add(value.toString());
	    		}
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
	@Override
	public LinkedList<String> getStrongTriggers() {
		
		return this.strongTriggers;
	}

	@Override
	public LinkedList<String> getWeakTriggers() {
		return this.weakTriggers;
	}

}
