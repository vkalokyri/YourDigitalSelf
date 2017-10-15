package com.rutgers.neemi;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.interfaces.W5hLocals;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Locals;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.Process;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.parser.TriggersFactory;
import com.rutgers.neemi.parser.ScriptParser;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rutgers.neemi.parser.InitiateScript.JsonTriggerFactory;


public class RestaurantsFragment extends Fragment {

    View myView;
    String scriptKeywords;
    static Triggers scriptTriggers;
    static Clues clues;
    public static HashMap<Object,Object> triggers_Clues = new HashMap<Object,Object>();
    DatabaseHelper helper;
    List<Task> tasksRunning=new ArrayList<Task>();
    static Map <String, Object> scriptElements;

    Integer[] imgid={
            R.drawable.restaurant
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_restaurants, container, false);
        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


        helper=new DatabaseHelper(getActivity());

        try{
            ConfigReader config = new ConfigReader(getContext());
            String filename = config.getStr(PROPERTIES.SCRIPT_FILE);
            System.out.println(filename);
            this.scriptElements=new HashMap<String, Object>();
            this.scriptElements = new ScriptParser().start(filename,null,getContext());

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
                //printTriggersAndClues(triggers_Clues);
            }

            for (int i=0;i<scriptTriggers.getWeakTriggers().size();i++){
                String weakTrigger = scriptTriggers.getWeakTriggers().get(i);
                triggers_Clues.put(weakTrigger, clues.getClues(weakTrigger.substring(1,weakTrigger.length()-1), "restaurant",getContext()));
                //printTriggersAndClues(triggers_Clues);
            }

            List <Task> tasksrunning= findTaskInstancesInDatabase(triggers_Clues);
            for (Task task:tasksrunning) {
                String taskName = task.getName();
                System.out.println("Task is: " + taskName);
                Object pid = task.getPid();
                if (pid instanceof Email){
                    System.err.println("Task = "+ taskName+ ", Email = "+((Email) pid).get_id());
                }else if (pid instanceof Payment){
                    System.err.println("Task = "+ taskName+ ", Payment = "+((Payment) pid).getName());
                }
                List<Locals> taskLocals = extractTaskLocals(taskName);
                List<Locals> localValues = new ArrayList<Locals>();

                if (taskLocals!=null){
                    for(Locals w5h:taskLocals){
                        Locals w5hInfo = new Locals();
                        for (String w5hValue: w5h.getValue() ){
                            W5hLocals locals = JsonTriggerFactory.getLocals(getContext());
                            List<String> localValue = locals.getLocals(w5hValue, pid, getContext());
                            w5hInfo.setW5h_label(w5hValue);
                            w5hInfo.setValue(localValue);
                            System.err.println(w5h.getW5h_label());
                            if (localValue!=null){
                                for (String v:localValue)
                                    System.err.println(v);
                            }
                        }
                        localValues.add(w5hInfo);
                    }

                }
                task.setLocals(localValues);
            }

            CustomListAdapter adapter=new CustomListAdapter(getActivity(), tasksrunning, imgid);
            ListView list=(ListView) myView.findViewById(R.id.restaurant_list);
            list.setAdapter(adapter);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
    }


    public List<Locals> extractTaskLocals(String taskName){
        for (HashMap.Entry<String, Object> entry : scriptElements.entrySet()) {
            String key = entry.getKey();
            Process p = (Process) entry.getValue();
            if  (p.getTasks().get(taskName)!=null){
                return p.getTasks().get(taskName).getLocals();
            }
        }
        return null;
    }


    public List<Task> findTaskInstancesInDatabase(HashMap<Object, Object> triggers_Clues){

        for (HashMap.Entry<Object, Object> entry : triggers_Clues.entrySet()) {
            String key = (String)entry.getKey();
            System.err.println("Task = "+key);
            List<HashMap<Object, Object>> values = (List<HashMap<Object, Object>>) entry.getValue();
            if (values!=null) {
                for (HashMap<Object, Object> value : values) {
                    for (HashMap.Entry<Object, Object> subtasks : value.entrySet()) {
                        String subtask = (String)subtasks.getKey();
                        // System.out.println("Subtask = " +subtask);
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
                            //System.out.print("FromWhere = " + fromWhere);
                            if (fromWhere.equals("from")) {
                                for (int i = 0; i < ((ArrayList) clues.getValue()).size(); i++) {
                                    //System.out.println("From value = " + ((ArrayList) clues.getValue()).get(i));
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
                                    //System.out.println("AndOrOr = " + clueKey);
                                    Object clueValue = cluesKeyValues.getValue();
                                    if (clueKey.equals("and") || clueKey.equals("or")) {
                                        HashMap<Object, Object> valuesOfAndOr = (HashMap<Object, Object>) clueValue;
                                        whereClause = whereClause + " ( ";
                                        for (HashMap.Entry<Object, Object> valueOfAndOr : valuesOfAndOr.entrySet()) {
                                            String andOrKey = (String) valueOfAndOr.getKey();
                                            Object andOrValue = valueOfAndOr.getValue();
                                            //System.out.println("clue key = " + andOrKey);
                                            if (andOrValue instanceof ArrayList) {
                                                for (int i = 0; i < ((ArrayList) andOrValue).size(); i++) {
                                                    String item = ((ArrayList) andOrValue).get(i).toString();
                                                    //System.out.println("Clue value = " + item);
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
                                                //System.out.println("Clue value = " + andOrValue.toString().replace("\"", ""));
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
                            if (fromTable.contains("Payment")){
                                query="select distinct Payment._id, Payment.name ";                            }
                            query = query + fromClause + whereClause;
                            query=query.substring(0,query.length()-4) +");";
                            //System.out.println("QUERY = " + query);
                            tasksRunning.addAll(queryDatabase(query,fromTable,subtask));
                        }else{
                            foundKeywordsFile=false;
                            tasksRunning.addAll(fullTextSearch(keywordsSearchColumn,fromTable, subtask));
                        }

                    }
                }
            }
        }

        return tasksRunning;
    }

    public List<Task> fullTextSearch(String text_column, String fromTable, String subtask){

        fromTable=fromTable.toString().replace("\"", "");
        List<Task> tasks = new ArrayList<Task>();

        String query = "SELECT * FROM "+ fromTable +" WHERE \"_id\" IN (select \"_id\" from "+ fromTable + "_fts WHERE `"+ text_column +"` MATCH '("+ scriptKeywords+")')";
        System.out.println("FULLTEXTQUERY = " + query);
        if (fromTable.equals("Email")){
            GenericRawResults<Email> rawResults = helper.getEmailDao().queryRaw(query,helper.getEmailDao().getRawRowMapper());
            for (Email email: rawResults){
                Task task = new Task();
                task.setPid(email);
                task.setName(subtask);
                tasks.add(task);
            }
        }
        if (fromTable.equals("Payment")){
            GenericRawResults<Payment> rawResults = helper.getPaymentDao().queryRaw(query,helper.getPaymentDao().getRawRowMapper());
            for (Payment payment: rawResults){
                Task task = new Task();
                task.setPid(payment);
                task.setName(subtask);
                tasks.add(task);
            }
        }
        if (fromTable.equals("Event")){
            GenericRawResults<Event> rawResults = helper.getEventDao().queryRaw(query,helper.getEventDao().getRawRowMapper());
            for (Event event: rawResults){
                Task task = new Task();
                task.setPid(event);
                task.setName(subtask);
                tasks.add(task);
            }
        }

        return tasks;
    }

    public List<Task> queryDatabase(String query, String fromTable, String subtask){
        List<Task> tasks = new ArrayList<Task>();
        RuntimeExceptionDao<Event, String> calendarDao = helper.getEventDao();
        RuntimeExceptionDao<Payment, String> paymentDao = helper.getPaymentDao();


        GenericRawResults<String[]> rawResults = calendarDao.queryRaw(query);
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (results!=null){
            //System.out.println("QueryReturn= " + results.size());
            if (results.size()>0){
                if (fromTable.contains("Payment")){
                    for (String[] tuple:results){
                        Payment payment= new Payment();
                        payment.setId(tuple[0]);
                        payment.setName(tuple[1]);

                        GenericRawResults<Payment> paymentData = paymentDao.queryRaw("select * from Payment where `_id`="+tuple[0], paymentDao.getRawRowMapper());
                        for (Payment fullpayment: paymentData){
                            Task task = new Task();
                            task.setPid(fullpayment);
                            task.setName(subtask);
                            tasks.add(task);
                        }
                    }

                }
            }
        }
        return tasks;
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






    private class CustomListAdapter extends ArrayAdapter<Task> {

        private final Activity context;
        private final List<Task> itemname;
        private final Integer[] imgid;

        public CustomListAdapter(Activity context, List<Task> tasks, Integer[] imgid) {
            super(context, R.layout.restaurantsview, tasks);
            // TODO Auto-generated constructor stub

            this.context=context;
            this.itemname=tasks;
            this.imgid=imgid;
        }

        public View getView(int position,View view,ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.restaurantsview, null,true);
            LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayout);



            TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);


            if (itemname.get(position).getPid() instanceof Email){
                txtTitle.setText(String.valueOf(((Email)itemname.get(position).getPid()).get_id()));
                imageView.setImageResource(imgid[0]);
                if(itemname.get(position).getLocals()!=null) {
                    for(Locals local : itemname.get(position).getLocals()) {
                        TextView  localTextView = new TextView(this.getContext());
                        localTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        localTextView.setText(getString(R.string.local, local.getW5h_label()+ " : " + local.getValue().toString()));
                        linearLayout.addView(localTextView);
                    }
                }

            }else if (itemname.get(position).getPid() instanceof Payment) {
                txtTitle.setText(((Payment)itemname.get(position).getPid()).getName());
                imageView.setImageResource(imgid[0]);
                if(itemname.get(position).getLocals()!=null) {
                    for(Locals local : itemname.get(position).getLocals()) {

                        TextView  localTextView = new TextView(this.getContext());
                        localTextView.setText(getString(R.string.local, local.getW5h_label()+ " : " + local.getValue().toString()));
                        linearLayout.addView(localTextView);
                    }
                }
            }


            return rowView;

        };
    }


}


