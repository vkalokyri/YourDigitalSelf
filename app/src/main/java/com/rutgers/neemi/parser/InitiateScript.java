package com.rutgers.neemi.parser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;
import com.rutgers.neemi.util.Utilities;

public class InitiateScript {

	public static HashMap<Object,Object> triggers_Clues = new HashMap<Object,Object>();
	//public static Map<String, LinkedList<Document>> taskPids = new HashMap<String, LinkedList<Document>>();
	public static List<Script> listOfProcesses = new ArrayList<Script>();
	public static ScriptParser sp;
	public static Utilities util = new Utilities();
	public static ConfigReader config;//ConfigReader.getInstance();
	public static String scriptKeywords;
	public static Date start_date;
	public static Date end_date;
	public static TriggersFactory JsonTriggerFactory;
	static Triggers scriptTriggers;
	static Clues clues;
	//public static String source;

	
	
	public void init() throws IOException, ParseException{
		InputStream fis = this.getClass().getResourceAsStream("/resources/"+config.getStr(PROPERTIES.KEYWORDS_FILE));
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);
	    String keywords="";
	    String line;
	    while ((line = br.readLine()) != null) {
	    	keywords=keywords+line+"|";
	    }
	    System.err.println(keywords);
	    this.scriptKeywords=keywords.substring(0, keywords.length()-1);
	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    
	    //Get the strong triggers and clues for triggers of the script		
//        JsonTriggerFactory = TriggersFactory.getTriggersFactory(TriggersFactory.json);
//		scriptTriggers= JsonTriggerFactory.getTriggers();
//        clues = JsonTriggerFactory.getClues();
	    
	    this.start_date=(Date)formatter.parse(config.getStr(PROPERTIES.START_DATE));
	    this.end_date=(Date)formatter.parse(config.getStr(PROPERTIES.END_DATE));
	    //sp = new ScriptParser();
	    
	    
	}
	
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		
		InitiateScript script = new InitiateScript();
		try {
			script.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
        for (int i=0;i<scriptTriggers.getStrongTriggers().size();i++){
			String strongTrigger = scriptTriggers.getStrongTriggers().get(i);
			//triggers_Clues.put(strongTrigger, clues.getClues(strongTrigger.substring(1,strongTrigger.length()-1), "restaurant"));
			//printTriggersAndClues(triggers_Clues);		        
		}
        
        for (int i=0;i<scriptTriggers.getWeakTriggers().size();i++){
			String weakTrigger = scriptTriggers.getWeakTriggers().get(i);
			//triggers_Clues.put(weakTrigger, clues.getClues(weakTrigger.substring(1,weakTrigger.length()-1), "restaurant"));
			//printTriggersAndClues(triggers_Clues);		        
		}
        
        List<Task> tasksRunning = findTaskInstancesInDatabase(triggers_Clues);
        System.err.println("Size of initial tasks = "+tasksRunning.size());
        
//        //extract locals per PID of everyTask
//        for (Task task:tasksRunning){
//        	  String taskName = task.getName();
//        	  System.out.println("Task is: "+taskName);
//        	  Object pid = task.getPid();
//        	  List<Object> locals = sp.extractLocalsFromProcess(taskName.replace("\"", ""));
//        	  List<LocalProperties> localValues = new ArrayList<LocalProperties>();
//
//        	  if (locals!=null){
//	        	  for(Object w5h:locals){
//	            	  LocalProperties w5hInfo = new LocalProperties();
//	        		  if (w5h instanceof Who){
//	        			  W5hLocals taskLocals = JsonTriggerFactory.getLocals();
//	        			  List<String> localValue = taskLocals.getLocals(((Who)w5h).getName(), pid);
//	        			  w5hInfo.setW5h_label(((Who)w5h).getName());
//	        			  w5hInfo.setValue(localValue);
//	        			  System.err.println(((Who)w5h).getName());
//	        			  if (localValue!=null){
//	        				  for (String v:localValue)
//	        					  System.err.println(v);
//	        			  }
//	        		  }if (w5h instanceof What){
//	        			  W5hLocals taskLocals = JsonTriggerFactory.getLocals();
//	        			  List<String> localValue = taskLocals.getLocals(((What)w5h).getName(), pid);
//	        			  w5hInfo.setW5h_label(((What)w5h).getName());
//	        			  w5hInfo.setValue(localValue);
//	        			  System.err.println(((What)w5h).getName());
//	        			  if (localValue!=null){
//	        				  for (String v:localValue)
//	        					  System.err.println(v);
//	        			  }
//	        		  }	if (w5h instanceof When){
//	        			  W5hLocals taskLocals = JsonTriggerFactory.getLocals();
//	        			  List<String> localValue = taskLocals.getLocals(((When)w5h).getName(), pid);
//	        			  w5hInfo.setW5h_label(((When)w5h).getName());
//	        			  w5hInfo.setValue(localValue);
//	        			  System.err.println(((When)w5h).getName());
//	        			  if (localValue!=null){
//	        				  for (String v:localValue)
//	        					  System.err.println(v);
//	        			  }
//	        		  }	if (w5h instanceof Where){
//	        			  W5hLocals taskLocals = JsonTriggerFactory.getLocals();
//	        			  List<String> localValue = taskLocals.getLocals(((Where)w5h).getName(), pid);
//	        			  w5hInfo.setW5h_label(((Where)w5h).getName());
//	        			  w5hInfo.setValue(localValue);
//	        			  System.err.println(((Where)w5h).getName());
//	        			  if (localValue!=null){
//	        				  for (String v:localValue)
//	        					  System.err.println(v);
//	        			  }
//	        		  }	if (w5h instanceof How){
//	        			  System.out.println(((How)w5h).getName());
//	        		  }	if (w5h instanceof Why){
//	        			  System.out.println(((Why)w5h).getName());
//	        		  }
//	            	  localValues.add(w5hInfo);
//	        	  }
//
//        	  }
//        	  task.setLocals(localValues);
//        }
//
//        //createOneProcessPerTask and assign score
//        for (Task task:tasksRunning){
//        	Process process = new Process();
//        	process.addTask(task);
//        	process.setLocals(task.getLocals());
//        	Process newProcess = util.assignScore(process);
//        	listOfProcesses.add(newProcess);
//        }
//
//
//
//        util.saveInExcel(listOfProcesses);
        
        //categorize documents
//        List<String> taskNames = sp.getAllScriptTaskNames();
//        
//        for (String task:taskNames){
//        	System.out.println(task);
//        }
        
        //mergeProcessesByEventDate(listOfProcesses);
        
        //startMerging
        //extractLocals();
        //mergeThreads();	
	}
	
	
	public static void categorizePIDs(){
		
	}
	
	
//	public static void mergeProcessesByEventDate(List<Process> listOfProcesses){
//		LinkedList<String> strongTriggers = scriptTriggers.getStrongTriggers();
//		for (Process process:listOfProcesses){
//        	for (Task task:process.getTasks()){
//        		String taskName = task.getName();
//        		if (strongTriggers.contains(taskName)){
//        			System.err.println("It's a strong trigger task");
//            		String source = task.getPid().getString("source");
//            		if (source.equals("gcal")){
//
//            		}
//
//        		}
//        		for (LocalProperties sublocals:task.getLocals()){
//        			if (sublocals.getValue()!=null){
//	        			sublocals.getValue().toString();
//        			}else{
//        			}
//
//        		}
//
//        	}
//        }
//	}
	
	
//	public static void mergeThreads(LinkedList<Document> pids){
//		
//		//merge into email/facebook threads
//        List<List<Document>> mergedEmailsByThread=util.getEmailThreads(pids);        
//        for (List<Document> mergedPids:mergedEmailsByThread){
//        	Process process = new Process();
//        	process.setPids(mergedPids);        	
//        	listOfProcesses.add(process);
//        }
//		System.err.println("Total processes running after threading ="+listOfProcesses.size());
//		
//		//assignScores
//		for (Process process:listOfProcesses){
//			util.assignScore(process);
//		}
//		
//		Collections.sort(listOfProcesses, new Comparator<Process>() {
//	        @Override public int compare(Process p1, Process p2) {
//	            return Float.compare(p2.getScore(),p1.getScore()); // Ascending
//	        }
//
//	    });
//	}
	
	
	
	
	public static List<Task> findTaskInstancesInDatabase(HashMap<Object, Object> triggers_Clues) {
		List<Task> tasksRunning = new ArrayList<Task>();

		for (HashMap.Entry<Object, Object> entry : triggers_Clues.entrySet()) {
			String key = (String) entry.getKey();
			System.err.println("Trigger = " + key);
			List<HashMap<Object, Object>> values = (List<HashMap<Object, Object>>) entry.getValue();
			if (values != null) {
				for (HashMap<Object, Object> value : values) {
					String source = null;
					for (HashMap.Entry<Object, Object> clues : value.entrySet()) {
						String andOr = (String) clues.getKey();
						System.out.println("SubTask = " + andOr);

						if (andOr.equals("and")) {
							HashMap<String, Object> clueMap = (HashMap<String, Object>) clues.getValue();
							for (HashMap.Entry<String, Object> cluesKeyValues : clueMap.entrySet()) {
								String clueKey = cluesKeyValues.getKey();
								System.out.println("clue key = " + clueKey);
								System.out.println("Clue value = " + cluesKeyValues.getValue());
								System.out.println("Clue value class = " + cluesKeyValues.getValue().getClass());
								Object clueValue = cluesKeyValues.getValue();
								if (clueKey.equalsIgnoreCase("source")) {
									source = (String) clueValue;
								}
							}
						}
					}
				}
			}
		}
		return tasksRunning;
	}


//								if (clueValue instanceof BasicDBObject){
//									if ( ((BasicDBObject) clueValue).containsValue("false")){
//										clueValue=new BasicDBObject("key",((BasicDBObject) clueValue).get("key")).append("value", false);
//									}else if(((BasicDBObject) clueValue).containsValue("true")){
//										clueValue=new BasicDBObject("key",((BasicDBObject) clueValue).get("key")).append("value", true);
//									}else if( ((BasicDBObject) clueValue).containsValue("KEYWORDS_FILE")){
//										BasicDBObject regex = new BasicDBObject("$regex",scriptKeywords);
//										clueValue=new BasicDBObject("key",((BasicDBObject) clueValue).get("key")).append("value",regex );
//									}
//								}
//								if (clueKey.startsWith("data.")){
//									if (clueValue instanceof ArrayList){
//										for (Object clue:(ArrayList<Object>)clueValue){
//											if (((BasicDBObject)clue).get("value") instanceof BasicDBList){
//												BasicDBObject temp = new BasicDBObject("$in", ((BasicDBObject)clue).get("value"));
//												BasicDBObject temp3 = new BasicDBObject( "key", ((BasicDBObject)clue).get("key")).append("value", temp);
//												BasicDBObject elemMatchQuery = new BasicDBObject("$elemMatch", temp3);
//												orObj.add(new BasicDBObject(clueKey,elemMatchQuery));
//											}else{
//												BasicDBObject elemMatchQuery = new BasicDBObject("$elemMatch", clue);
//												orObj.add(new BasicDBObject(clueKey,elemMatchQuery));
//											}
//										}
//									}else{
//										BasicDBObject elemMatchQuery = new BasicDBObject("$elemMatch", clueValue);
//										andObj.add(new BasicDBObject(clueKey, elemMatchQuery));
//									}
//								}else{
//									andObj.add(new BasicDBObject(clueKey, clueValue));
//								}
//							}
//						}else if( andOr.equals("or")){
//							HashMap<String, Object> clueMap = (HashMap<String, Object>) clues.getValue();
//							for (HashMap.Entry<String, Object> cluesKeyValues : clueMap.entrySet()) {
//								String clueKey = cluesKeyValues.getKey();
//								System.out.println("clue key = "+clueKey);
//								Object clueValue = cluesKeyValues.getValue();
//								if (clueValue instanceof BasicDBObject){
//									if ( ((BasicDBObject) clueValue).containsValue("false")){
//										clueValue=new BasicDBObject("key",((BasicDBObject) clueValue).get("key")).append("value", false);
//									}else if(((BasicDBObject) clueValue).containsValue("true")){
//										clueValue=new BasicDBObject("key",((BasicDBObject) clueValue).get("key")).append("value", true);
//									}else if( ((BasicDBObject) clueValue).containsValue("KEYWORDS_FILE")){
//										BasicDBObject regex = new BasicDBObject("$regex",scriptKeywords);
//										clueValue=new BasicDBObject("key",((BasicDBObject) clueValue).get("key")).append("value",regex );
//									}
//								}else if (clueValue instanceof ArrayList){
//									for (Object clue:(ArrayList<Object>)clueValue){
//										if (((BasicDBObject)clue).get("value") instanceof BasicDBList){
//											BasicDBObject temp = new BasicDBObject("$in", ((BasicDBObject)clue).get("value"));
//											BasicDBObject temp3 = new BasicDBObject( "key", ((BasicDBObject)clue).get("key")).append("value", temp);
//											BasicDBObject elemMatchQuery = new BasicDBObject("$elemMatch", temp3);
//											orObj.add(new BasicDBObject(clueKey,elemMatchQuery));
//										}else if (clue instanceof BasicDBObject){
//												if ( ((BasicDBObject) clue).containsValue("false")){
//													clue=new BasicDBObject("key",((BasicDBObject) clue).get("key")).append("value", false);
//												}else if(((BasicDBObject) clue).containsValue("true")){
//													clue=new BasicDBObject("key",((BasicDBObject) clue).get("key")).append("value", true);
//												}else if( ((BasicDBObject) clue).containsValue("KEYWORDS_FILE")){
//													BasicDBObject regex = new BasicDBObject("$regex",scriptKeywords);
//													clue=new BasicDBObject("key",((BasicDBObject) clue).get("key")).append("value",regex );
//												}
//												orObj.add(new BasicDBObject(clueKey,new BasicDBObject("$elemMatch", clue)));
//										}else{
//											BasicDBObject elemMatchQuery = new BasicDBObject("$elemMatch", clue);
//											orObj.add(new BasicDBObject(clueKey,elemMatchQuery));
//										}
//									}
//								}else{
//									orObj.add(new BasicDBObject(clueKey, clueValue));
//								}
//							}
//
//
//						}
//					}
//					List<BasicDBObject> dateFilterQuery = getDateFilter(source);
//					if (dateFilterQuery!=null){
//						for (BasicDBObject dateObject:dateFilterQuery){
//							andObj.add(dateObject);
//						}
//					}
//					BasicDBObject orQuery = new BasicDBObject();
//					orQuery.put("$or", orObj);
//					System.out.println("the or query is = "+orQuery.toString());
//					if (!orObj.isEmpty()){
//						andObj.add(orQuery);
//					}
//					BasicDBObject finalQuery =  new BasicDBObject();
//					finalQuery.put("$and", andObj);
//					System.err.println("final query = "+ finalQuery.toString());
//					tasksRunning.addAll(queryDatabase(key, finalQuery));
//				}
//			}
//
//		}
//		return tasksRunning;
//
//
//	}
//
//	public static List<BasicDBObject> getDateFilter(String source){
//
//
//		BasicDBObject lessThanDate = null;
//		BasicDBObject greaterThanDate = null;
//		BasicDBObject dateGreater = new BasicDBObject("$gte",start_date);
//		BasicDBObject dateLess = new BasicDBObject("$lte",end_date);
//		boolean setFilter=false;
//		System.err.println("THE SOURCE IS" + source);
//
//		if (source.equalsIgnoreCase("mint")){
//			//lessThanDate = new BasicDBObject( "key", "date_posted").append("value", dateLess);
//			//greaterThanDate =  new BasicDBObject( "key", "date_posted").append("value", dateGreater);
//		}else if (source.equalsIgnoreCase("gcal")){
//			setFilter=true;
//			lessThanDate = new BasicDBObject( "key", "start.dateTime").append("value", dateLess);
//			greaterThanDate =  new BasicDBObject( "key", "start.dateTime").append("value", dateGreater);
//		}else if (source.equalsIgnoreCase("gmail")){
//			setFilter=true;
//			lessThanDate = new BasicDBObject( "key", "created_at").append("value", dateLess);
//			greaterThanDate =  new BasicDBObject( "key", "created_at").append("value", dateGreater);
//		}else if (source.equalsIgnoreCase("facebook")){
////			setFilter=true;
////			lessThanDate = new BasicDBObject( "key", "created_at").append("value", dateLess);
////			greaterThanDate =  new BasicDBObject( "key", "created_at").append("value", dateGreater);
//		}else if (source.equalsIgnoreCase("foursquare")){
////			setFilter=true;
////			lessThanDate = new BasicDBObject( "key", "created_at").append("value", dateLess);
////			greaterThanDate =  new BasicDBObject( "key", "created_at").append("value", dateGreater);
//		}else if (source.equalsIgnoreCase("firefox")){
//			setFilter=true;
//			lessThanDate = new BasicDBObject( "key", "date/time").append("value", dateLess);
//			greaterThanDate =  new BasicDBObject( "key", "date/time").append("value", dateGreater);
//		}
//
//		if (setFilter){
//			BasicDBObject elemMatchLessQuery = new BasicDBObject("$elemMatch", lessThanDate);
//			BasicDBObject elemMatchGreaterQuery = new BasicDBObject("$elemMatch", greaterThanDate);
//			List<BasicDBObject> dateQuery =new ArrayList<BasicDBObject>();
//			dateQuery.add(new BasicDBObject("data.when", elemMatchLessQuery));
//			dateQuery.add(new BasicDBObject("data.when", elemMatchGreaterQuery));
//			return dateQuery;
//		}else{
//			return null;
//		}
//
//
//
//	}
//
//
//	//fills up the pids
//	public static List<Task> queryDatabase(String taskName, BasicDBObject finalQuery){
//
//		String database_name = config.getStr(PROPERTIES.DB_NAME);
//
//		MongoClient client = new MongoClient(config.getStr(PROPERTIES.DB_HOSTNAME),Integer.parseInt(config.getStr(PROPERTIES.DB_PORT)));
//		MongoDatabase database = client.getDatabase(database_name);
//		MongoCollection<Document> collection = database.getCollection(config.getStr(PROPERTIES.PARSED_COLLECTION));
//		List<Task> runningTasks = new ArrayList<Task>();
//
//
//		  FindIterable<Document> documentIterator = collection.find(finalQuery);
//		  MongoCursor<Document> cursor  = documentIterator.iterator();
//		  int count=0;
//			try {
//			    while (cursor.hasNext()) {
//			    	count++;
//			    	Document data = (Document)cursor.next();
//					//LinkedList<Document> pids = new LinkedList<Document>();
//			    	//pids.add(data);
//
//			    	Task task = new Task();
//			    	task.setName(taskName);
//			    	task.setPid(data);
//			    	runningTasks.add(task);
//		        	//Map<String, LinkedList<Document>> taskPid = new HashMap<String, LinkedList<Document>>();
//		        	//taskPid.put(taskName, pids);
//		        	//process.setTaskPids(taskPid);
//		        	//listOfProcesses.add(process);
//
//			    }
//			}catch(Exception e){
//
//			}
//
//			return runningTasks;
//
//
//	}
//
//
//	public static void printTriggersAndClues(Map<String, Object>triggers_Clues){
//
//		for (HashMap.Entry<String, Object> entry : triggers_Clues.entrySet()) {
//			  String key = entry.getKey();
//			  System.out.println("Trigger = "+key);
//			  System.out.println("Trigger = "+entry.getValue());
//		}
//	}
//
//	public static void printTriggersAndClues(HashMap<Object,Object> triggers_Clues){
//
//		for (HashMap.Entry<Object,Object> entry : triggers_Clues.entrySet()) {
//			  String key = (String)entry.getKey();
//			  System.out.println("Trigger = "+key);
//			  List<HashMap<Object,Object>> values = (List<HashMap<Object,Object>>)entry.getValue();
//			  if (values!=null){
//				  for (HashMap<Object, Object> value:values){
//					  for (HashMap.Entry<Object, Object> clues : value.entrySet()) {
//						  String task = (String)clues.getKey();
//						  System.out.println("SubTask = "+task);
//						  HashMap<Object, Object> clue = (HashMap<Object, Object>)clues.getValue();
//						  for (HashMap.Entry<Object, Object> cluesKeyValues : clue.entrySet()) {
//							  String clueKey = (String)cluesKeyValues.getKey();
//							  System.out.println("clue key = "+clueKey);
//							  Object clueValue = cluesKeyValues.getValue();
//							  if (clueValue instanceof ArrayList){
//								  for (int i=0;i<((ArrayList)clueValue).size();i++)
//									  System.out.println("Clue value = "+((ArrayList)clueValue).get(i));
//							  }else{
//								  System.out.println("Clue value = "+clueValue);
//							  }
//
//						  }
//					  }
//				  }
//			  }
//		}
//
//	}
	

}
