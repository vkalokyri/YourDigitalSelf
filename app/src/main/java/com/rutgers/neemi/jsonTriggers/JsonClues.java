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
import com.rutgers.neemi.parser.InitiateScript;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import static com.couchbase.lite.replicator.RemoteRequest.JSON;


public class JsonClues implements Clues{
	
	
	ConfigReader config;
	LinkedList<String> clues;
	JsonReader jsonReader;
	HashMap<String, LinkedList<String>> cluesMapping;
	InputStream fis;
	
	
	public JsonClues(Context context){
		config = ConfigReader.getInstance();
		 try {

				//this.jsonReader.close();
	    		//fis.close();


	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}

        
        @Override
    	public  List<HashMap<Object,Object>> getClues(String task, String onObject, Context context) throws FileNotFoundException {
			//create JsonReader object
			try {
				this.fis = context.getAssets().open(config.getStr(PROPERTIES.CLUES));
			} catch (Exception e) {
				System.err.println(e);
			}
			this.jsonReader = Json.createReader(fis);
			JsonObject jsonObject = jsonReader.readObject();

			JsonObject taskObject = jsonObject.getJsonObject(task); //makeAPayment



			JsonArray jsonArray = taskObject.getJsonArray(onObject); //restaurant

			List<HashMap<Object, Object>> finalMap = new ArrayList<HashMap<Object, Object>>();
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject firstarr = (JsonObject) jsonArray.get(i); //first json object
				//System.out.println("FirstItem = "+firstarr);
				Iterator<String> iter = firstarr.keySet().iterator();  //get inside the first json object
				final HashMap<Object, Object> taskMap = new HashMap<Object, Object>();
				while (iter.hasNext()) {
					//e.g. payBycreditCard, <category,[restaurant,fastfood]>
					final HashMap<Object, Object> fromWhereMap = new HashMap<Object, Object>();
					String key = (String) iter.next(); //payByCreditCard key
					System.out.println("KEY paybycreditcard = "+key);
					JsonObject value = firstarr.getJsonObject(key); //from, where object
					System.out.println("key = "+value);
					Iterator<String> valueIter = value.keySet().iterator(); //iterate the from/where
					while (valueIter.hasNext()) {
						String valueKey = (String) valueIter.next(); //valueKey=from, valuekey = where
						System.out.println("key2 = " +valueKey);
						if (valueKey.equals("from")) {
							ArrayList<Object> stringArray = new ArrayList<Object>();
							if (value.get(valueKey) instanceof JsonArray) {
								JsonArray tableNames = value.getJsonArray(valueKey);
								for (int j = 0; j < ((JsonArray) tableNames).size(); j++) {
									JsonValue stringValue = ((JsonArray) tableNames).get(j);
									stringArray.add(stringValue.toString());
								}
								fromWhereMap.put(valueKey, stringArray);
							}else if (value.get(valueKey) instanceof JsonString){
								stringArray.add(((JsonString)value.get(valueKey)).toString());
								fromWhereMap.put(valueKey, stringArray);
							}
						}
						if (valueKey.equals("where")) {
							final HashMap<Object, Object> OrAndmap = new HashMap<Object, Object>();
							JsonObject searchValues = value.getJsonObject(valueKey);
							Iterator<String> keyAndOr = searchValues.keySet().iterator(); //iterate the and/or
							while (keyAndOr.hasNext()) {
								String andOr = (String) keyAndOr.next();
								final HashMap<String, Object> whereConditionMap = new HashMap<String, Object>();
								if (andOr.equals("or") || andOr.equals("and")) {
									JsonObject condition = searchValues.getJsonObject(andOr);
									System.out.println(condition);
									if (condition != null) {
										Iterator<String> whereClauseIter = condition.keySet().iterator();
										while (whereClauseIter.hasNext()) {
											String attribute = (String) whereClauseIter.next();
											if (condition.get(attribute) instanceof JsonArray) {
												Object conditionValue = condition.getJsonArray(attribute);
												System.out.println(attribute);
												System.out.println(conditionValue);
												if (conditionValue instanceof JsonArray) {
													ArrayList<Object> stringArray = new ArrayList<Object>();
													for (int j = 0; j < ((JsonArray) conditionValue).size(); j++) {
														JsonValue stringValue = ((JsonArray) conditionValue).get(j);
														stringArray.add(stringValue.toString());
													}
													whereConditionMap.put(attribute, stringArray);

												} else if (conditionValue instanceof JsonString) {
													whereConditionMap.put(attribute, conditionValue);
												}
											}else if (condition.get(attribute) instanceof JsonString){
												whereConditionMap.put(attribute, condition.get(attribute));
											}
										}
									}
								}
								OrAndmap.put(andOr, whereConditionMap);
							}
							fromWhereMap.put(valueKey, OrAndmap);
						}
					}
					taskMap.put(key,fromWhereMap);
				}
				finalMap.add(taskMap);
			}
			return finalMap;
		}


}
