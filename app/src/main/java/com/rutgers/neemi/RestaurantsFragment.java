package com.rutgers.neemi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.interfaces.W5hLocals;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.LocalValues;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.ScriptHasTasks;
import com.rutgers.neemi.model.Subscript;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.parser.TriggersFactory;
import com.rutgers.neemi.parser.ScriptParser;
import com.rutgers.neemi.util.ApplicationManager;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;
import com.rutgers.neemi.util.Utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonString;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.rutgers.neemi.parser.InitiateScript.JsonTriggerFactory;
import static com.rutgers.neemi.parser.InitiateScript.util;


public class RestaurantsFragment extends Fragment {


    private static final String TAG = "RestaurantsFragment";
    View myView;
    String scriptKeywords;
    static Triggers scriptTriggers;
    static Clues clues;
    public static HashMap<Object,Object> triggers_Clues = new HashMap<Object,Object>();
    DatabaseHelper helper;
    List<Task> tasksRunning=new ArrayList<Task>();
    ArrayList<Script> listOfScripts = new ArrayList<Script>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    RuntimeExceptionDao<ScriptDefinition, String> scriptDefDao;
    RuntimeExceptionDao<Subscript, String> subscriptsDao;
    RuntimeExceptionDao<TaskDefinition, String> taskDefDao;
    RuntimeExceptionDao<ScriptHasTasks, String> scriptHasTasksDao;
    RuntimeExceptionDao<LocalValues, String> localValuesDao;



    Integer[] imgid={
            R.drawable.restaurant
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        if (!prefs.getBoolean("firstTime", false)) {
//            // <---- run your one time code here
//            findScriptInstances();
//
//            // mark first time has runned.
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putBoolean("firstTime", true);
//            editor.commit();
//        }

        myView = inflater.inflate(R.layout.fragment_restaurants, container, false);
        ListView list1 =  (ListView) myView.findViewById(R.id.restaurant_list);

        list1.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        ScriptFragment scriptFragment = new ScriptFragment();
                        Bundle arguments = new Bundle();
                        arguments.putSerializable("processes", listOfScripts);
                        arguments.putSerializable("position",position);
                        arguments.putSerializable("id",id);

                        scriptFragment.setArguments(arguments);

                        android.support.v4.app.FragmentTransaction scriptfragmentTrans = getFragmentManager().beginTransaction();
                        scriptfragmentTrans.replace(R.id.frame,scriptFragment);
                        scriptfragmentTrans.commit();
                        Toast.makeText(getContext(), "Pressed!", Toast.LENGTH_LONG).show();
                    }
                }
        );

        return myView;
    }


    public void findScriptInstances(){

        helper=DatabaseHelper.getHelper(getActivity());
        scriptDefDao = helper.getScriptDefDao();
        subscriptsDao = helper.getSubScriptDao();
        taskDefDao = helper.getTaskDefinitionDao();
        scriptHasTasksDao = helper.getScriptHasTasksDao();
        localValuesDao = helper.getLocalValuesDao();

        scriptDefDao.queryRaw("delete from LocalValues;");


        try{
            ConfigReader config = new ConfigReader(getContext());
//            String filename = config.getStr(PROPERTIES.SCRIPT_FILE);
//            Log.d(TAG, "Read script file: "+ filename);
//            this.scriptElements=new HashMap<String, Object>();
//
//            /*parse the script and get the script definitions*/
//            this.scriptElements = new ScriptParser().start(filename,null,getContext());
//            storeScriptDefinition(this.scriptElements);

            /*get the keywords to search in the documents*/
            InputStream fis = getContext().getAssets().open(config.getStr(PROPERTIES.KEYWORDS_FILE));
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String keywords="";
            String line;
            while ((line = br.readLine()) != null) {
                keywords=keywords+"\""+line+"\""+" OR ";
            }
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

            /*find the initial tasks that are running*/
            List <Task> tasksrunning= findTaskInstancesInDatabase(triggers_Clues);

            /*extract all the local properties from the tasks*/
            for (Task task:tasksrunning) {
                String taskName = task.getName();
                System.out.println("Task is: " + taskName);
                Object pid = task.getPid();
                if (pid instanceof Event) {
                    System.err.println("Task = " + taskName + ", Event = " + ((Event) pid).get_id());
                } else if (pid instanceof Email) {
                    System.err.println("Task = " + taskName + ", Email = " + ((Email) pid).get_id());


                } else if (pid instanceof Payment) {
                    System.err.println("Task = " + taskName + ", Payment = " + ((Payment) pid).getName());
                }

                ArrayList<LocalProperties> taskLocals = null;
                taskLocals = helper.extractTaskLocals(taskName);


                if (taskLocals!=null){
                    for(LocalProperties w5h:taskLocals){
                        W5hLocals locals = JsonTriggerFactory.getLocals(getContext());
                        ArrayList<String> localValue = locals.getLocals(w5h.getW5h_value(), pid, getContext());
                        if (localValue.size()>0) {
                            for (String lValue:localValue) {

                                LocalValues w5hInfo = new LocalValues();
                                w5hInfo.setLocalProperties(w5h);
                                w5hInfo.setValue(lValue);
                                w5hInfo.setTask(task);
                                localValuesDao.create(w5hInfo);
                                task.addLocalValue(w5hInfo);

//                                System.err.println("EDW= "+w5h.getW5h_label());
//                                if (localValue!=null){
//                                    for (String v:localValue)
//                                        System.err.println(v);
//                                }

                                //task.setLocals(w5hInfo);
                            }
                        }
                        //localValues.add(w5hInfo);
                    }

                }
                //task.getLocals().add(localValues);
            }


            mergeThreads(tasksRunning);
            listOfScripts = createScriptPerTask(tasksRunning);
//           mergeScriptsByEventDate(listOfScripts);



            CustomListAdapter adapter=new CustomListAdapter(getActivity(), listOfScripts, imgid);
            ListView list= myView.findViewById(R.id.restaurant_list);
            list.setAdapter(adapter);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        findScriptInstances();


    }

    public ArrayList<Script> createScriptPerTask(List<Task> tasksRunning) throws SQLException {

        ArrayList<Script> scripts = new ArrayList<Script>();
        for(Task task:tasksRunning){
            //put all tasks local values under the abstract who, what, where dimensions
            HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
            for(LocalValues taskLocalValues:task.getLocalValues()){
                String w5hLabel = taskLocalValues.getLocalProperties().getW5h_label();
                if(map.containsKey(w5hLabel)){
                    ArrayList<String> values = map.get(w5hLabel);
                    values.add(taskLocalValues.getValue());
                    map.put(w5hLabel,values );
                }else{
                    if (w5hLabel!=null) {
                        ArrayList<String> values = new ArrayList<String>();
                        values.add(taskLocalValues.getValue());
                        map.put(w5hLabel, values);
                    }
                }
            }

            Script script = new Script();
            script.setScriptDefinition( helper.getTopScriptsByTask(task.getName()));
            script.addTask(task);


            for (LocalProperties localProp:script.getScriptDefinition().getLocalProperties()) {
                ArrayList<String> values = map.get(localProp.getW5h_label());
                if (values != null) {
                    for (String value : values) {
                        LocalValues scriptLocalValues = new LocalValues();
                        scriptLocalValues.setLocalProperties(localProp);
                        scriptLocalValues.setTask(task);
                        scriptLocalValues.setValue(value);
                        script.addLocalValue(scriptLocalValues);
                    }
                }
            }

            //add local values in the definitions

            ArrayList<ScriptDefinition> scriptDefinitionList =script.getScriptDefinition().getSubscripts();
            for (ScriptDefinition subscriptDef:scriptDefinitionList ){
                Script subscript = new Script();
                subscript.setScriptDefinition(subscriptDef);
                for (LocalProperties localProp:subscriptDef.getLocalProperties()) {
                    ArrayList<String> values = map.get(localProp.getW5h_label());
                    if (values != null) {
                        for (String value : values) {
                            LocalValues scriptLocalValues = new LocalValues();
                            scriptLocalValues.setLocalProperties(localProp);
                            scriptLocalValues.setTask(task);
                            scriptLocalValues.setValue(value);
                            subscript.addLocalValue(scriptLocalValues);
                        }
                    }
                }
                script.addSubscript(subscript);
            }





//            for (LocalProperties localProp:script.getScriptDefinition().getLocalProperties()){
//                ArrayList<String> values = map.get(localProp.getW5h_label());
//                if(values!=null) {
//                    for (String value : values) {
//                        LocalValues scriptLocalValues = new LocalValues();
//                        scriptLocalValues.setLocalProperties(localProp);
//                        scriptLocalValues.setTask(task);
//                        scriptLocalValues.setValue(value);
//                        script.addLocalValue(scriptLocalValues);
//                    }
//                }
//
//            }

            script.assignScore(getContext());
            scripts.add(script);
        }

        return scripts;
    }

//    public void storeScriptDefinition(Map<String, Object> scriptElements){
//        for (String key: scriptElements.keySet()) {
//            if (key != null){
//                ScriptDefinition scriptDefinition = (ScriptDefinition)scriptElements.get(key);
//                ScriptDefinition scriptDef = helper.scriptDefinitionExists(scriptDefinition.getName());
//                if (scriptDef == null) {
//                    scriptDefDao.create(scriptDefinition);
//                    scriptDef = scriptDefinition;
//                }
//
//
//                for (String taskName : scriptDef.getTaskMap().keySet()) {
//                    if (taskName != null) {
//                        TaskDefinition taskDef = helper.taskDefinitionExists(taskName);
//                        if (taskDef == null) {
//                            taskDef = scriptDef.getTaskMap().get(taskName);
//                            taskDefDao.create(taskDef);
//                        }
//                        ScriptHasTasks scriptTasks = new ScriptHasTasks();
//                        scriptTasks.setTask(taskDef);
//                        scriptTasks.setScript(scriptDef);
//                        scriptHasTasksDao.create(scriptTasks);
//                    }
//                }
//
//                //readSubscripts
//                ScriptDefinition superScript = (ScriptDefinition) scriptElements.get(key);
//                for (ScriptDefinition subscript : superScript.getSubscripts()) {
//                    storeScriptDefinition(subscript,superScript);
//                }
//            }
//        }
//    }
//
//    public void storeScriptDefinition(ScriptDefinition subscript, ScriptDefinition superScript){
//        if (subscript != null) {
//
//            ScriptDefinition scriptDef = helper.scriptDefinitionExists(subscript.getName());
//            if (scriptDef == null) {
//                scriptDefDao.create(subscript);
//            }else{
//                subscript = scriptDef;
//            }
//
//            Subscript subscript1 = new Subscript();
//            subscript1.setSuperscript_id(superScript);
//            subscript1.setSubscript_id(subscript);
//            subscriptsDao.create(subscript1);
//
//            for (String taskName : subscript.getTaskMap().keySet()) {
//                if (taskName != null) {
//                    TaskDefinition taskDef = helper.taskDefinitionExists(taskName);
//                    if (taskDef == null) {
//                        taskDef = subscript.getTaskMap().get(taskName);
//                        taskDefDao.create(taskDef);
//                    }
//                    ScriptHasTasks scriptTasks = new ScriptHasTasks();
//                    scriptTasks.setTask(taskDef);
//                    scriptTasks.setScript(subscript);
//                    scriptHasTasksDao.create(scriptTasks);
//                }
//            }
//
//
//            for (ScriptDefinition script : subscript.getSubscripts()) {
//                storeScriptDefinition(script,subscript);
//            }
//
//
//
//        }
//
//
//    }

    public void mergeScriptsByEventDate(List<Script> listOfScripts){
        Log.d(TAG,"SIZE OF PROCESSES: " +listOfScripts.size());
        HashMap<Date, List<Task>> hashMap = new HashMap<Date, List<Task>>();
        List<Script> mergedScripts = new ArrayList<Script>();
		for (Script process:listOfScripts){
        	for (Task task:process.getTasks()){
                Date extractedDate=null;
                if (task.getPid() instanceof Payment){
                    try {
                        extractedDate = sdf.parse(sdf.format(((Payment) task.getPid()).getDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else if (task.getPid() instanceof Email){
                    if (((Email) task.getPid()).getSubjectDate()!=null) {
                        try {
                            extractedDate = sdf.parse(sdf.format(((Email) task.getPid()).getSubjectDate()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }else{

                    }
                }else if (task.getPid() instanceof Event){
                    try {
                        extractedDate = sdf.parse(sdf.format(((Event) task.getPid()).getStartTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if(extractedDate!=null) {
                    if (!hashMap.containsKey(extractedDate)) {
                        List<Task> list = new ArrayList<Task>();
                        list.add(task);
                        hashMap.put(extractedDate, list);
                    } else {
                        hashMap.get(extractedDate).add(task);
                    }
                }
        	}
        }

        Log.d(TAG,"SIZE OF PROCESSES after: " +hashMap.size());
        for (Map.Entry entry : hashMap.entrySet()) {
            Log.d(TAG,"Date: " +entry.getKey());
            for (Task t:(List<Task>)entry.getValue()){
                if (t.getPid() instanceof Event) {
                    Log.d(TAG,"Event = " + ((Event) t.getPid()).getDescription());
                }else if (t.getPid() instanceof Email){
                    Log.d(TAG, "Email = "+((Email) t.getPid()).getSubject());
                }else if (t.getPid() instanceof Payment){
                    Log.d(TAG,"Payment = "+((Payment) t.getPid()).getName());
                }
            }
        }

    }


	public void mergeThreads(List<Task> tasks){

		//hashmap of key:threadId and value:task
        HashMap<String,List<Task>> mergeTasksByThread = new HashMap();
        for (Task task:tasks) {
            if (task.getPid() instanceof Email) {
                String key = ((Email) task.getPid()).getThreadId();
                List<Task> list = mergeTasksByThread.get(key);
                if (list == null) {
                    list = new ArrayList<Task>();
                    mergeTasksByThread.put(key, list);
                }
                list.add(task);
            }else {
                Script script = new Script();
                script.addTask(task);

               // script.setLocals(task.getLocals());
                script.assignScore(getContext());
                listOfScripts.add(script);
            }
        }

        for (HashMap.Entry<String, List<Task>>  mergedThreads: mergeTasksByThread.entrySet()) {
            List<Task> mergedtasks = mergedThreads.getValue();
            Script process = new Script();
            for (Task task:mergedtasks){
                process.addTask(task);
            }
            process.assignScore(getContext());
            listOfScripts.add(process);
        }



		System.err.println("Total processes running after threading ="+listOfScripts.size());

		Collections.sort(listOfScripts, new Comparator<Script>() {
	        @Override public int compare(Script p1, Script p2) {
	            return Float.compare(p2.getScore(),p1.getScore()); // Ascending
	        }

	    });
	}


//    public ArrayList<LocalProperties> extractTaskLocals(String taskName){
//        for (HashMap.Entry<String, Object> entry : scriptElements.entrySet()) {
//            String key = entry.getKey();
//            ScriptDefinition p = (ScriptDefinition) entry.getValue();
//            if  (p.getTaskMap().get(taskName)!=null){
//                return p.getTaskMap().get(taskName).getLocals();
//            }
//        }
//        return null;
//    }


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
                                        Log.d(TAG,"fromTable "+ fromTable);
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
                                            Log.d(TAG,"clue key "+ andOrKey);
                                            if (andOrValue instanceof ArrayList) {
                                                for (int i = 0; i < ((ArrayList) andOrValue).size(); i++) {
                                                    String item = ((ArrayList) andOrValue).get(i).toString();
                                                    Log.d(TAG,"Clue value = " + item);
                                                    //whereClause = whereClause + " ( `" + andOrKey+"`";
                                                    if (i == 0) {
                                                        whereClause = whereClause + " ( `" + andOrKey+"`" + " LIKE '%" + item.toString().replace("\"", "") + "%'";
                                                    } else {
                                                        whereClause = whereClause + " or `" + andOrKey + "` LIKE '%" + item.toString().replace("\"", "") + "%'";
                                                    }

                                                }
                                                whereClause = whereClause + " )";

                                            } else {
                                                if (((JsonString)andOrValue).getString().equals("KEYWORDS_FILE")) {

                                                    //if (i == 0) {
                                                    whereClause = whereClause + " `" + andOrKey + "` MATCH '(" + scriptKeywords + ")'";
                                                    //whereClause = whereClause + " ( `" + andOrKey+"`" + " LIKE '%" + item.toString().replace("\"", "") + "%'";
                                                    //} else {
                                                    //    whereClause = whereClause + " or `" + andOrKey + "` MATCH '("+ scriptKeywords+")'";
                                                    //}

                                                    foundKeywordsFile = true;
                                                    //keywordsSearchColumn = andOrKey;
                                                    whereClause = whereClause + " )";

                                                } else {
                                                    whereClause = whereClause + " " + andOrKey;
                                                    //System.out.println("Clue value = " + andOrValue.toString().replace("\"", ""));
                                                    whereClause = whereClause + " = '" + andOrValue.toString().replace("\"", "")+"'";
                                                }

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
                            if (fromClause.contains("Payment")){
                                query="select distinct Payment._id, Payment.name ";                            }
                            query = query + fromClause + whereClause;
                            query=query.substring(0,query.length()-4) +");";
                            System.out.println("QUERY = " + query);
                            try {
                                tasksRunning.addAll(queryDatabase(query,fromClause,subtask));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            foundKeywordsFile=false;
                            whereClause=whereClause.substring(0,whereClause.length()-4) +")";
                            tasksRunning.addAll(fullTextSearch(whereClause,fromTable, subtask));
                        }

                    }
                }
            }
        }

        return tasksRunning;
    }

    public List<Task> fullTextSearch(String whereClause, String fromTable, String subtask){

        fromTable=fromTable.toString().replace("\"", "");
        List<Task> tasks = new ArrayList<Task>();

        String query = "SELECT * FROM "+ fromTable +" WHERE \"_id\" IN (select \"_id\" from "+ fromTable + "_fts "+ whereClause +";"; //`"+ text_column +"` MATCH '("+ scriptKeywords+")')";
        query = query.replace(" or "," UNION select \"_id\" from " + fromTable + "_fts where ( " );
        System.out.println("FULLTEXTQUERY = " + query);
        //query = "SELECT * FROM Email WHERE \"_id\" IN (select \"_id\" from Email_fts  where  `textContent` MATCH '(\"restaurant\" OR \"dinner\" OR \"lunch\" )' UNION select \"_id\" from Email_fts  where `subject` MATCH '(\"restaurant\" OR \"dinner\" OR \"lunch\" )');";

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
                task.setOid(payment.getId());
                task.setPid(payment);
                task.setName(subtask);
                tasks.add(task);
            }
        }
        if (fromTable.equals("Event")){
            GenericRawResults<Event> rawResults = helper.getEventDao().queryRaw(query,helper.getEventDao().getRawRowMapper());
            for (Event event: rawResults){
                Task task = new Task();
                task.setOid(event.getId());
                task.setPid(event);
                task.setName(subtask);
                tasks.add(task);
            }
        }

        return tasks;
    }

    public List<Task> queryDatabase(String query, String fromTable, String subtask){
        List<Task> tasks = new ArrayList<Task>();


        GenericRawResults<String[]> rawResults = helper.getPaymentDao().queryRaw(query);
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (results!=null){
            if (results.size()>0){
                if (fromTable.contains("Payment")){
                    for (String[] tuple:results) {
                        Payment payment = new Payment();
                        payment.setId(tuple[0]);
                        payment.setName(tuple[1]);

                        String tempQuery = "select * from `Payment` where `_id`=" + tuple[0];
                        GenericRawResults<Payment> paymentData = helper.getPaymentDao().queryRaw(tempQuery, helper.getPaymentDao().getRawRowMapper());
                        for (Payment fullpayment : paymentData) {
                            Task task = new Task();
                            task.setOid(fullpayment.getId());
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



    private class CustomListAdapter extends ArrayAdapter<Script> {

        private final Activity context;
        private final List<Script> itemname;
        private final Integer[] imgid;

        public CustomListAdapter(Activity context, List<Script> scripts, Integer[] imgid) {
            super(context, R.layout.restaurantsview, scripts);
            // TODO Auto-generated constructor stub
            this.context=context;
            this.itemname=scripts;
            this.imgid=imgid;
        }

        public View getView(int position,View view,ViewGroup parent) {


            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.restaurantsview, null,true);
            LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayout);

           // TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);


            Script script = itemname.get(position);//.getScriptDefinition();
            HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
                    //txtTitle.setText(itemname.get(position).getScore()+", "+String.valueOf(((Email)processTask.getPid()).get_id()));
            imageView.setImageResource(imgid[0]);
            ArrayList<LocalValues> localValues = script.getLocalValues();
            if (localValues != null) {
                for (LocalValues localValue : localValues) {
                    if (localValue != null) {
                        LocalProperties lp = localValue.getLocalProperties();
                        if (lp != null) {
                            String w5h_value = lp.getW5h_value();
                            if (map.containsKey(w5h_value)) {
                                ArrayList<String> values = map.get(w5h_value);
                                values.add(localValue.getValue());
                                map.put(w5h_value, values);
                            } else {
                                if (w5h_value != null) {
                                    ArrayList<String> values = new ArrayList<String>();
                                    values.add(localValue.getValue());
                                    map.put(w5h_value, values);
                                }
                            }
                        }
                    }
                }
            }

            for (String localLabel:map.keySet()) {
                StringBuilder sb = new StringBuilder();
                for (String localValue:map.get(localLabel)) {
                    sb.append(localValue);
                    sb.append(", ");
                }
                sb.delete(sb.length()-2,sb.length()-1);

                LinearLayout textLayout = new LinearLayout(context);
                textLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(10, 5, 5, 0);
                textLayout.setLayoutParams(llp);

                TextView localTextView = new TextView(this.getContext());
                localTextView.setTextColor(Color.parseColor("#99CCFF"));
                localTextView.setText(getString(R.string.local, localLabel + " : "));

                TextView localValueTextView = new TextView(this.getContext());
                localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
                localValueTextView.setText(getString(R.string.local, sb.toString()));

                textLayout.addView(localTextView);
                textLayout.addView(localValueTextView);
                linearLayout.addView(textLayout);



            }



//                for (Task processTask : itemname.get(position).getTasks()) {
//                System.out.println("SIZE OF TASKS = "+itemname.get(position).getTasks().size());
//                if (processTask.getPid() instanceof Email){
//                    //txtTitle.setText(itemname.get(position).getScore()+", "+String.valueOf(((Email)processTask.getPid()).get_id()));
//                    imageView.setImageResource(imgid[0]);
//                    if (processTask.getLocalValues() != null) {
//                        for (LocalValues local : processTask.getLocalValues()) {
//                            if (local.getValue()!=null) {
//                                if (!local.getValue().toString().equalsIgnoreCase("[null]") && !local.getValue().toString().equalsIgnoreCase("[]")) {
//
//                                    LinearLayout textLayout = new LinearLayout(context);
//                                    textLayout.setOrientation(LinearLayout.HORIZONTAL);
//                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                                    llp.setMargins(10, 5, 5, 0);
//                                    textLayout.setLayoutParams(llp);
//
//                                    TextView localTextView = new TextView(this.getContext());
//                                    localTextView.setTextColor(Color.parseColor("#99CCFF"));
//                                    localTextView.setText(getString(R.string.local, local.getLocalProperties().getW5h_value() + " : "));
//
//                                    TextView localValueTextView = new TextView(this.getContext());
//                                    localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
//                                    localValueTextView.setText(getString(R.string.local, local.getValue().toString()));
//
//                                    textLayout.addView(localTextView);
//                                    textLayout.addView(localValueTextView);
//                                    linearLayout.addView(textLayout);
//                                }
//                            }
//                        }
//                    }
//
//                }else if (processTask.getPid() instanceof Payment) {
//                    //txtTitle.setText(itemname.get(position).getScore()+", "+((Payment) processTask.getPid()).getName());
//                    imageView.setImageResource(imgid[0]);
//                    if (processTask.getLocalValues() != null) {
//                        for (LocalValues local : processTask.getLocalValues()) {
//                            LinearLayout textLayout = new LinearLayout(context);
//                            textLayout.setOrientation(LinearLayout.HORIZONTAL);
//                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                            llp.setMargins(10, 5, 5, 0);
//                            textLayout.setLayoutParams(llp);
//
//                            TextView localTextView = new TextView(this.getContext());
//                            localTextView.setTextColor(Color.parseColor("#99CCFF"));
//                            localTextView.setText(getString(R.string.local, local.getLocalProperties().getW5h_value() + " : "));
//
//                            TextView localValueTextView = new TextView(this.getContext());
//                            localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
//                            localValueTextView.setText(getString(R.string.local, local.getLocalProperties().getW5h_value().toString()));
//
//                            textLayout.addView(localTextView);
//                            textLayout.addView(localValueTextView);
//                            linearLayout.addView(textLayout);
//
//                            localTextView = new TextView(this.getContext());
//                            llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                            llp.setMargins(5, 5, 5, 5);
//                            localTextView.setLayoutParams(llp);
//                            localTextView.setText(getString(R.string.local, local.getLocalProperties().getW5h_label() + " : " + local.getValue().toString()));
//                            linearLayout.addView(localTextView);
//                        }
//                    }
//                }
//            }
            return rowView;
        };
    }


}


