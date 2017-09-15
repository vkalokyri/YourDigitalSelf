package com.rutgers.neemi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.parser.InitiateScript;
import com.rutgers.neemi.parser.TriggersFactory;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.rutgers.neemi.parser.InitiateScript.JsonTriggerFactory;
import static com.rutgers.neemi.parser.InitiateScript.scriptKeywords;


public class RestaurantsFragment extends Fragment {

    View myView;
    String scriptKeywords;
    static Triggers scriptTriggers;
    static Clues clues;
    public static HashMap<Object,Object> triggers_Clues = new HashMap<Object,Object>();
    DatabaseHelper helper;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_restaurants, container, false);



        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        helper=new DatabaseHelper(getActivity());

        super.onActivityCreated(savedInstanceState);
        try{
            ConfigReader config = new ConfigReader(getContext());
            String filename = config.getStr(PROPERTIES.SCRIPT_FILE);
            System.out.println(filename);
            scriptParser(filename);


            InputStream fis = getContext().getAssets().open(config.getStr(PROPERTIES.KEYWORDS_FILE));
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String keywords="";
            String line;
            while ((line = br.readLine()) != null) {
                keywords=keywords+"\""+line+"\""+" OR ";
            }
            System.err.println(keywords);
            this.scriptKeywords=keywords.substring(0, keywords.length()-4);


            //Get the strong triggers and clues for triggers of the script
            JsonTriggerFactory = TriggersFactory.getTriggersFactory(TriggersFactory.json);
            scriptTriggers= JsonTriggerFactory.getTriggers(getContext());
            clues = JsonTriggerFactory.getClues(getContext());

            for (int i=0;i<scriptTriggers.getStrongTriggers().size();i++){
                String strongTrigger = scriptTriggers.getStrongTriggers().get(i);
                triggers_Clues.put(strongTrigger, clues.getClues(strongTrigger.substring(1,strongTrigger.length()-1), "restaurant",getContext()));
                printTriggersAndClues(triggers_Clues);
            }

            for (int i=0;i<scriptTriggers.getWeakTriggers().size();i++){
                String weakTrigger = scriptTriggers.getWeakTriggers().get(i);
                triggers_Clues.put(weakTrigger, clues.getClues(weakTrigger.substring(1,weakTrigger.length()-1), "restaurant",getContext()));
                //printTriggersAndClues(triggers_Clues);
            }
            findTaskInstancesInDatabase(triggers_Clues);
        }catch (IOException e){
            e.printStackTrace();
        }


    }


    public List<Task> findTaskInstancesInDatabase(HashMap<Object, Object> triggers_Clues){
        List<Task> tasksRunning=new ArrayList<Task>();

        for (HashMap.Entry<Object, Object> entry : triggers_Clues.entrySet()) {
            String key = (String)entry.getKey();
            System.err.println("Task = "+key);
            List<HashMap<Object, Object>> values = (List<HashMap<Object, Object>>) entry.getValue();
            if (values!=null) {
                for (HashMap<Object, Object> value : values) {
                    for (HashMap.Entry<Object, Object> subtasks : value.entrySet()) {
                        String subtask = (String)subtasks.getKey();
                        System.out.println("Subtask = " +subtask);
                        String query="select * ";
                        String fromClause=" from ";
                        String whereClause=" where ";
                        boolean foundKeywordsFile = false;
                        String keywordsSearchColumn="";
                        String fullTextQuery="";
                        String fromTable="";
                        HashMap<Object, Object> fromWhereValues = (HashMap<Object, Object>) subtasks.getValue();
                        for (HashMap.Entry<Object, Object> clues : fromWhereValues.entrySet()) {
                            String fromWhere = (String) clues.getKey();
                            System.out.print("FromWhere = " + fromWhere);
                            if (fromWhere.equals("from")) {
                                for (int i = 0; i < ((ArrayList) clues.getValue()).size(); i++) {
                                    System.out.println("From value = " + ((ArrayList) clues.getValue()).get(i));
                                    if (i == 0) {
                                        fromTable = ((ArrayList) clues.getValue()).get(i).toString();
                                        fromClause = fromClause + "`"+((ArrayList) clues.getValue()).get(i)+"`";
                                    } else {
                                        fromClause = fromClause + ",`" + ((ArrayList) clues.getValue()).get(i)+"`";
                                    }
                                }
                            } else if (fromWhere.equals("where")) {
                                HashMap<Object, Object> clue = (HashMap<Object, Object>) clues.getValue();
                                for (HashMap.Entry<Object, Object> cluesKeyValues : clue.entrySet()) {
                                    String clueKey = (String) cluesKeyValues.getKey();
                                    System.out.println("AndOrOr = " + clueKey);
                                    Object clueValue = cluesKeyValues.getValue();
                                    if (clueKey.equals("and") || clueKey.equals("or")) {
                                        HashMap<Object, Object> valuesOfAndOr = (HashMap<Object, Object>) clueValue;
                                        whereClause = whereClause + " ( ";
                                        for (HashMap.Entry<Object, Object> valueOfAndOr : valuesOfAndOr.entrySet()) {
                                            String andOrKey = (String) valueOfAndOr.getKey();
                                            Object andOrValue = valueOfAndOr.getValue();
                                            System.out.println("clue key = " + andOrKey);
                                            if (andOrValue instanceof ArrayList) {
                                                for (int i = 0; i < ((ArrayList) andOrValue).size(); i++) {
                                                    String item = ((ArrayList) andOrValue).get(i).toString();
                                                    System.out.println("Clue value = " + item);
                                                    if (item.equals("\"KEYWORDS_FILE\"")){
                                                        foundKeywordsFile = true;
                                                        keywordsSearchColumn = andOrKey;
                                                    }else {
                                                        //whereClause = whereClause + " ( `" + andOrKey+"`";
                                                        if (i == 0) {
                                                            whereClause = whereClause + " ( `" + andOrKey+"`" + " LIKE '%" + item.toString().replace("\"", "") + "%'";
                                                        } else {
                                                            whereClause = whereClause + " or `" + andOrKey + "` LIKE '%" + item.toString().replace("\"", "") + "%'";
                                                        }
                                                    }
                                                }
                                                whereClause = whereClause + " )";

                                            } else {
                                                whereClause = whereClause +" "+ andOrKey;
                                                System.out.println("Clue value = " + andOrValue.toString().replace("\"", ""));
                                                whereClause = whereClause + " = " + andOrValue.toString().replace("\"", "");
                                            }
                                            whereClause = whereClause + " " +clueKey ;
                                        }
                                    }
                                    whereClause=whereClause+")";
                                }
                            }
                        }
                        if (!foundKeywordsFile){
                            fromClause = fromClause.replace("\"", "");
                            query = query + fromClause + whereClause;
                            query=query.substring(0,query.length()-4) +");";
                            System.out.println("QUERY = " + query);
                            queryDatabase(query);
                        }else{
                            foundKeywordsFile=false;
                            fullTextQuery = fullTextSearch(keywordsSearchColumn,fromTable);
                        }
                    }

                }
            }
        }

        return tasksRunning;
    }

    public String fullTextSearch(String text_column, String fromTable){
        RuntimeExceptionDao<Event, String> calendarDao = helper.getEventDao();
        calendarDao.queryRaw("DROP TABLE IF EXISTS fts_table ");
        calendarDao.queryRaw("CREATE VIRTUAL TABLE fts_table USING fts3 ("+ text_column+ ")");
        calendarDao.queryRaw("INSERT INTO fts_table SELECT "+text_column+" from "+fromTable);
        String query = "select * from fts_table WHERE `"+ text_column +"` MATCH '("+ scriptKeywords+")'";
        System.out.println("FULLTEXTQUERY = " + query);
        GenericRawResults<String[]> rawResults = calendarDao.queryRaw(query);
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (results!=null){
            System.out.println("QueryReturn= " + results.size());
        }

        calendarDao.queryRaw("DROP TABLE fts_table");
        return query;
    }

    public void queryDatabase(String query){
        RuntimeExceptionDao<Event, String> calendarDao = helper.getEventDao();

        GenericRawResults<String[]> rawResults = calendarDao.queryRaw(query);
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (results!=null){
            System.out.println("QueryReturn= " + results.size());
        }

    }



    public void printTriggersAndClues(HashMap<Object,Object> triggers_Clues){

		for (HashMap.Entry<Object,Object> entry : triggers_Clues.entrySet()) {
			  String key = (String)entry.getKey();
			  System.out.println("Trigger = "+key);
			  List<HashMap<Object,Object>> values = (List<HashMap<Object,Object>>)entry.getValue();
			  if (values!=null){
				  for (HashMap<Object, Object> value:values){
					  for (HashMap.Entry<Object, Object> clues : value.entrySet()) {
						  String fromWhere = (String)clues.getKey();
						  System.out.println("From/Where = "+fromWhere);
                          if (fromWhere.equals("from")){
                              for (int i = 0; i < ((ArrayList) clues.getValue()).size(); i++)
                              System.out.println("Clue value = " + ((ArrayList) clues.getValue()).get(i));
                          }else {
                              HashMap<Object, Object> clue = (HashMap<Object, Object>) clues.getValue();
                              for (HashMap.Entry<Object, Object> cluesKeyValues : clue.entrySet()) {
                                  String clueKey = (String) cluesKeyValues.getKey();
                                  System.out.println("AndOrOr = " + clueKey);
                                  Object clueValue = cluesKeyValues.getValue();
                                  if (clueKey.equals("and") || clueKey.equals("or")) {
                                      HashMap<Object, Object> valuesOfAndOr = (HashMap<Object, Object>) clueValue;
                                      for (HashMap.Entry<Object, Object> valueOfAndOr : valuesOfAndOr.entrySet()) {
                                          String andOrKey = (String) valueOfAndOr.getKey();
                                          Object andOrValue = valueOfAndOr.getValue();
                                          System.out.println("clue key = " + andOrKey);
                                          if (andOrValue instanceof ArrayList) {
                                              for (int i = 0; i < ((ArrayList) andOrValue).size(); i++)
                                                  System.out.println("Clue value = " + ((ArrayList) andOrValue).get(i));
                                          } else {
                                              System.out.println("Clue value = " + andOrValue);
                                          }
                                      }
                                  }

                              }
                          }
					  }
				  }
			  }
		}

	}

    public void scriptParser(String filename) throws IOException {

        InputStream fis = getContext().getAssets().open(filename);
        try {
            parse(fis);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return extractDefinitions(parser,parser.getName());
        } finally {
            in.close();
        }
    }


    private List extractDefinitions(XmlPullParser parser, String element) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, element);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            System.out.println("XMLPARSING = "+name);
			// Starts by looking for the process
			if (name.equals("process")) {
                extractDefinitions(parser, name);
            }else if (name.equals("task")) {
                String taskName = readTask(parser);
                System.out.println("Task = "+taskName);
            }else if(name.equals("locals")){
                skip(parser);
            }else if(name.equals("callActivity")){
                String activityName = parser.getAttributeValue(null,"calledElement");
                scriptParser(activityName+".xml");
                skip(parser);
			} else {
				skip(parser);
			}
        }
        return entries;
    }

    private String readTask(XmlPullParser parser) throws IOException, XmlPullParserException {

        String taskName = null;
        parser.require(XmlPullParser.START_TAG, ns, "task");
        taskName = parser.getAttributeValue(null,"id");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("task")) {
                taskName = parser.getAttributeValue(null,"id");
            } else {
                skip(parser);
            }
        }
        return taskName;

    }


    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }




}


