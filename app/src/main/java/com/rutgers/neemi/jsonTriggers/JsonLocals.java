package com.rutgers.neemi.jsonTriggers;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Transaction;
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
	public ArrayList<String> getLocals(String local, Object pid, Context context) throws IOException {
		this.fis = context.getAssets().open(config.getStr(PROPERTIES.LOCALS_FILE));
		this.jsonReader = Json.createReader(fis);
		JsonObject jsonObject = jsonReader.readObject();
		ArrayList<String> localValues = new ArrayList<String>();

		JsonObject localObject = jsonObject.getJsonObject(local);
		if (localObject!=null){
			for (String objectClass: localObject.keySet()){
				if (pid.getClass().getSimpleName().equalsIgnoreCase(objectClass) ){
					JsonValue attributeName = localObject.get(objectClass);
					if (objectClass.equalsIgnoreCase("Email")){
						if (attributeName.toString().equalsIgnoreCase("\"from\"")){
							localValues.add(((Email)pid).getFrom());
						}
						else if (attributeName.toString().equalsIgnoreCase("\"to\"")){
							localValues.add(((Email)pid).getTo());
						}
						else if (attributeName.toString().equalsIgnoreCase("\"cc\"")){
							localValues.add(((Email)pid).getCc());
						}
						else if (attributeName.toString().equalsIgnoreCase("\"bcc\"")){
							localValues.add(((Email)pid).getBcc());
						}
						else if (attributeName.toString().equalsIgnoreCase("\"date\"")){
							localValues.add(((Email)pid).getDate().toString());
						}
					}else if (objectClass.equalsIgnoreCase("Transaction")){
						if (attributeName.toString().equalsIgnoreCase("\"merchant_name\"")){
							localValues.add(((Transaction)pid).getMerchant_name());
						}
						else if (attributeName.toString().equalsIgnoreCase("\"date\"")){
							localValues.add(String.valueOf(((Transaction)pid).getDate()));
						}
					}
				}
			}
		}
		return localValues;

	}
        

            
            
            
            
            
		


}
