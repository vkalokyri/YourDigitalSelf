package com.rutgers.neemi.jsonTriggers;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.interfaces.W5hLocals;
import com.rutgers.neemi.parser.TriggersFactory;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;



public class JsonLocals implements W5hLocals{
	
	
	ConfigReader config;
	JsonReader jsonReader;
	InputStream fis;
	
	
	public JsonLocals(Context context){
		config = ConfigReader.getInstance();
		 try {				
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}



		@Override
		public List<String> getLocals(String local, Object pid) throws FileNotFoundException {
			try{
        		this.fis =  this.getClass().getResourceAsStream("/resources/"+config.getStr(PROPERTIES.LOCALS_FILE));
        	}catch (Exception e){
    	 		System.err.println(e);
    	 	}
    		this.jsonReader = Json.createReader(fis);
            JsonObject jsonObject = jsonReader.readObject();
			List<String> locals = new ArrayList<String>();

            String source = (String)pid.getClass().toString();

            JsonObject localObject = jsonObject.getJsonObject(local);
            if (localObject!=null){
	            JsonValue attributeName = (JsonValue)localObject.get(source);
				locals.add(attributeName.toString());
            }
            return locals;

		}
        

            
            
            
            
            
		


}
