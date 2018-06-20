package com.rutgers.neemi.jsonTriggers;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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

import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.interfaces.W5hLocals;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.FeedWithTags;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.parser.TriggersFactory;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;



public class JsonLocals implements W5hLocals{
	
	
	ConfigReader config;
	JsonReader jsonReader;
	InputStream fis;
	DatabaseHelper helper;

	public JsonLocals(Context context){
		helper = DatabaseHelper.getHelper(context);
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
							localValues.add(((Email)pid).getFrom().getName());
						}
						else if (attributeName.toString().equalsIgnoreCase("\"to\"")){
							for (Person to : ((Email)pid).getTo()) {
								if(to!=null) {
									if (to.getName()!=null && !to.getName().isEmpty()) {
										localValues.add(to.getName());
									} else if (to.getEmail()!=null && !to.getEmail().isEmpty()){
										localValues.add(to.getEmail());
									}
								}
							}
						}
						else if (attributeName.toString().equalsIgnoreCase("\"cc\"")){
							for (Person to : ((Email)pid).getCc()) {
								if(to!=null) {
									if (to.getName()!=null && !to.getName().isEmpty()) {
										localValues.add(to.getName());
									} else if (to.getEmail()!=null && !to.getEmail().isEmpty()){
										localValues.add(to.getEmail());
									}
								}
							}
						}
						else if (attributeName.toString().equalsIgnoreCase("\"bcc\"")){
							for (Person to : ((Email)pid).getBcc()) {
								if(to!=null) {
									if (to.getName()!=null && !to.getName().isEmpty()) {
										localValues.add(to.getName());
									} else if (to.getEmail()!=null && !to.getEmail().isEmpty()){
										localValues.add(to.getEmail());
									}
								}
							}
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
					}else if (objectClass.equalsIgnoreCase("Feed")){
						if (attributeName.toString().equalsIgnoreCase("\"creator_id\"")){
							localValues.add(((Feed)pid).getCreator().getName());
						}else if (attributeName.toString().equalsIgnoreCase("\"message\"")) {
							localValues.add(String.valueOf(((Feed) pid).getMessage()));
						}else if (attributeName.toString().equalsIgnoreCase("\"created_time\"")){
								localValues.add(String.valueOf(((Feed)pid).getCreated_time()));
						}else if (attributeName.toString().equalsIgnoreCase("\"place_id\"")){
							if (((Feed)pid).getPlace().getName()!=null){
								localValues.add(String.valueOf(((Feed) pid).getPlace().getName()));
							}else if (((Feed)pid).getPlace().getCity()!=null) {
								localValues.add(String.valueOf(((Feed) pid).getPlace().getCity()));
							}

						}else if (attributeName.toString().equalsIgnoreCase("\"FeedWithTags\"")) {
							try {
								ArrayList<Person> tags = helper.getFeedWithTags(((Feed)pid).get_id());
								for(Person p:tags){
									localValues.add(p.getName());
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}


						}
// else if(attributeName.toString().equalsIgnoreCase("\"person_id\"")) {
//							localValues.add(((Feed) pid).getId());
//						}
					}
				}
			}
		}
		return localValues;

	}
        

            
            
            
            
            
		


}
