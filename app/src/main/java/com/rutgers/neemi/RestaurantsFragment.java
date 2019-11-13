package com.rutgers.neemi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rutgers.neemi.model.Data;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Message;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.StayPoint;
import com.rutgers.neemi.model.TaskLocalValues;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.ScriptDefHasTaskDef;
import com.rutgers.neemi.model.Subscript;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.parser.TriggersFactory;
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
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonString;


public class RestaurantsFragment extends Fragment {


    private static final String TAG = "RestaurantsFragment";
    public static View myView;
    String scriptKeywords;
    static Triggers scriptTriggers;
    static Clues clues;
    //public static HashMap<Object, Object> triggers_Clues = new HashMap<>();
    DatabaseHelper helper;
    List<Task> tasksRunning = new ArrayList<>();
    static ArrayList<Script> listOfScripts = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    RuntimeExceptionDao<ScriptDefinition, String> scriptDefDao;
    RuntimeExceptionDao<Subscript, String> subscriptsDao;
    RuntimeExceptionDao<TaskDefinition, String> taskDefDao;
    RuntimeExceptionDao<ScriptDefHasTaskDef, String> scriptHasTasksDao;
    RuntimeExceptionDao<ScriptLocalValues, String> scriptlocalValuesDao;
    RuntimeExceptionDao<TaskLocalValues, String> tasklocalValuesDao;

    Integer[] imgid = {
            R.drawable.restaurant
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        findScriptInstances("restaurant");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        myView = inflater.inflate(R.layout.fragment_restaurants, container, false);
        ListView list1 = (ListView) myView.findViewById(R.id.restaurant_list);
        myView.setBackgroundColor(getResources().getColor(android.R.color.white));


        list1.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        ScriptFragment scriptFragment = new ScriptFragment();
                        Bundle arguments = new Bundle();
                        arguments.putSerializable("processes", listOfScripts);
                        arguments.putSerializable("position", position);
                        arguments.putSerializable("id", id);

                        scriptFragment.setArguments(arguments);

                        android.support.v4.app.FragmentTransaction scriptfragmentTrans = getFragmentManager().beginTransaction();
                        scriptfragmentTrans.add(R.id.frame, scriptFragment);
                        scriptfragmentTrans.addToBackStack(null);
                        scriptfragmentTrans.commit();
                        Toast.makeText(getContext(), "Pressed!", Toast.LENGTH_LONG).show();
                    }
                }
        );

        return myView;
    }


    public void findScriptInstances(String scriptName) {

        helper = DatabaseHelper.getHelper(getActivity());
        scriptDefDao = helper.getScriptDefDao();
        subscriptsDao = helper.getSubScriptDao();
        taskDefDao = helper.getTaskDefinitionDao();
        scriptHasTasksDao = helper.getScriptHasTasksDao();
        scriptlocalValuesDao = helper.getScriptLocalValuesDao();
        tasklocalValuesDao = helper.getTaskLocalValuesDao();


        try {
            ConfigReader config = new ConfigReader(getContext());
            InputStream fis = null;

            /*get the keywords to search in the documents*/
            if (scriptName.equalsIgnoreCase("restaurant")) {
                fis = getContext().getAssets().open(config.getStr(PROPERTIES.RESTAURANT_KEYWORDS_FILE));
            } else if (scriptName.equalsIgnoreCase("trip")) {
                fis = getContext().getAssets().open(config.getStr(PROPERTIES.TRIP_KEYWORDS_FILE));
            }
            //InputStream fis = getContext().getAssets().open(config.getStr(PROPERTIES.KEYWORDS_FILE));
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String keywords = "";
            String line;
            while ((line = br.readLine()) != null) {
                keywords = keywords + "\"" + line + "\"" + " OR ";
            }
            this.scriptKeywords = keywords.substring(0, keywords.length() - 4);

            //Get the strong triggers and clues for triggers of the script
            TriggersFactory JsonTriggerFactory = TriggersFactory.getTriggersFactory(TriggersFactory.json);
            scriptTriggers = JsonTriggerFactory.getTriggers(getContext(), scriptName);
            clues = JsonTriggerFactory.getClues(getContext());
            HashMap<Object, Object> triggers_Clues = new HashMap<>();


            for (int i = 0; i < scriptTriggers.getStrongTriggers().size(); i++) {
                String strongTrigger = scriptTriggers.getStrongTriggers().get(i);
                String[] strongArray = strongTrigger.split("<");
                triggers_Clues.put(strongTrigger, clues.getClues(strongArray[0].substring(1), strongArray[1].substring(0, strongArray[1].length() - 2), getContext()));
                //printTriggersAndClues(triggers_Clues);
            }


            /*find the initial tasks that are running*/
            List<Task> strongTasksFound = findTaskInstancesInDatabase(triggers_Clues);

            HashMap<Object, Object> weakTriggers = new HashMap<>();
            for (int i = 0; i < scriptTriggers.getWeakTriggers().size(); i++) {
                String weakTrigger = scriptTriggers.getWeakTriggers().get(i);
                String[] weakArray = weakTrigger.split("<");
                weakTriggers.put(weakTrigger, clues.getClues(weakArray[0].substring(1), weakArray[1].substring(0, weakArray[1].length() - 2), getContext()));
                //printTriggersAndClues(weakTriggers);
            }

            List<Task> tasksRunning = setLocalValuesInTheTasks(strongTasksFound); //tasks from strong triggers
            List<Task> newTasks =  findTasksByWhereInText(tasksRunning, weakTriggers);
            tasksRunning.addAll(newTasks); //add weak tasks that contain the where from the strong triggers
            ArrayList<Task> photoTasks = addPhotosInTheScript(tasksRunning);
            tasksRunning.addAll(setLocalValuesInTheTasks(photoTasks));


            List<Task> weakTasksFound = findTaskInstancesInDatabase(weakTriggers);

            tasksRunning = eliminateCommonTasks(weakTasksFound);


            setLocalValuesInTheTasks(tasksRunning); //add unique weak tasks not retrieved before


            ArrayList<ArrayList<Task>> tasks = mergeTasksByEventDate(tasksRunning);
            ArrayList<ArrayList<Task>> tasksThreads = mergeThreads(tasks);
            listOfScripts = createScriptPerTask(tasksThreads);
            listOfScripts = mergeScriptsByWhenAndWhere(listOfScripts);



            //sort them
            Collections.sort(listOfScripts, new Comparator<Script>() {
                @Override
                public int compare(Script p1, Script p2) {
                    return Float.compare(p2.getScore(), p1.getScore()); // Ascending
                }

            });

            listOfScripts.removeIf(n -> (n.getScore() < 0.3));


            CustomListAdapter adapter = new CustomListAdapter(getActivity(), listOfScripts, imgid);
            ListView list = myView.findViewById(R.id.restaurant_list);
            list.setAdapter(adapter);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<Task> addPhotosInTheScript(List<Task> tasks) throws SQLException {
        ArrayList<Task> newTasks = new ArrayList<>();
        for(Task t:tasks){
            if(t.getName().equals("StayingInVenue")){
                Place p = (Place)t.getList_of_pids().get(0);
                long arrive = p.getSp().getArrive();
                long leave = p.getSp().getLeave();
                ArrayList<Photo> photos = helper.getPhotosInInterval(arrive,leave);
                if (photos!=null) {
                    for (Photo photo : photos) {
                        Task task = new Task();
                        task.setPid(photo);
                        task.setName("TakePictures");
                        task.setOid(photo.getId());
                        ScriptDefinition sd = helper.getScriptDefinition("attendEatingOutEvent","restaurant");
                        Script script = new Script();
                        script.setScriptDefinition(sd);
                        task.setScript(script);
                        task.setTaskDefinition(new TaskDefinition("TakePictures"));
                        newTasks.add(task);
                    }
                }

            }
        }
        return newTasks;
    }

    public List<Task> eliminateCommonTasks(List<Task> tasks){

        Set<Task> s= new HashSet<>();

        for (Task t:tasks){
            if(!s.add(t)){  // java.util.Set only unique object so if object will not be added in Set it will return false so can consider it as Duplicate
                System.out.println(t.getPid().getClass().getCanonicalName());
                if(t.getPid() instanceof Email){
                    System.out.println(((Email)t.getPid()).getSubject());

                }else if(t.getPid() instanceof Message){
                    System.out.println(((Message)t.getPid()).getContent());

                }
            }
        }
        s.addAll(tasks);
        tasks = new ArrayList<>();
        tasks.addAll(s);

        return tasks;
    }

    public List<Task> findTasksByWhereInText(List<Task> tasks, HashMap<Object, Object> weakTriggers) throws SQLException, IOException {
        Log.d(TAG, "SIZE OF tasks before retrieving where in text: " + tasks.size());
        TriggersFactory JsonTriggerFactory = TriggersFactory.getTriggersFactory(TriggersFactory.json);
        HashSet whereValues = new HashSet();
        HashSet pids = new HashSet();

        ArrayList<Task> newTasks = new ArrayList<>();

        for (Task task:tasks) {
            if (task.getPid() instanceof Email) {
                pids.add(((Email) task.getPid()).getId());
            } else if (task.getPid() instanceof Message) {
                pids.add(((Message) task.getPid()).get_id());
            }
        }


        for (Task task:tasks) {
            for (TaskLocalValues tLocalValues : task.getLocalValues()) {
                String label = tLocalValues.getLocalProperties().getW5h_label();
                if (label.equals("where")) {
                    String whereValue = tLocalValues.getLocal_value().replace("'","''");
                    if (!whereValues.contains(whereValue)) {
                        whereValues.add(whereValue);
                        for (HashMap.Entry<Object, Object> entry : weakTriggers.entrySet()) {
                            String scriptType = ((String) entry.getKey()).replace("\"", "");

                            System.err.println("Script = " + scriptType);
                            String[] scriptArray = scriptType.split("<");
                            String scriptName = null;
                            String typeOf = null;
                            if (scriptArray != null) {
                                scriptName = scriptArray[0];
                                typeOf = scriptArray[1].substring(0, scriptArray[1].length() - 1);
                            } else {
                                scriptName = scriptType;
                            }
                            ScriptDefinition sd = helper.getScriptDefinition(scriptName, typeOf);
                            Script script = new Script();
                            script.setScriptDefinition(sd);
                            List<HashMap<Object, Object>> values = (List<HashMap<Object, Object>>) entry.getValue();
                            if (values != null) {
                                for (HashMap<Object, Object> value : values) {
                                    for (HashMap.Entry<Object, Object> subtasks : value.entrySet()) {
                                        String subtask = (String) subtasks.getKey();
                                        String query = "select * ";
                                        String fromClause = " from ";
                                        String whereClause = " where ";

                                        String fromTable = "";
                                        HashMap<Object, Object> fromWhereValues = (HashMap<Object, Object>) subtasks.getValue();
                                        for (HashMap.Entry<Object, Object> clues : fromWhereValues.entrySet()) {
                                            String fromWhere = (String) clues.getKey();
                                            //System.out.print("FromWhere = " + fromWhere);
                                            if (fromWhere.equals("from")) {
                                                for (int i = 0; i < ((ArrayList) clues.getValue()).size(); i++) {
                                                    //System.out.println("From value = " + ((ArrayList) clues.getValue()).get(i));
                                                    if (i == 0) {
                                                        fromTable = ((ArrayList) clues.getValue()).get(i).toString();
                                                        Log.d(TAG, "fromTable " + fromTable);
                                                        fromClause = fromClause + "`" + ((ArrayList) clues.getValue()).get(i) + "`";
                                                    } else {
                                                        fromClause = fromClause + ",`" + ((ArrayList) clues.getValue()).get(i) + "`";
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
                                                            Log.d(TAG, "clue key " + andOrKey);
                                                            whereClause = whereClause + " `" + andOrKey + "` MATCH '(" + whereValue + ")'";
                                                            whereClause = whereClause + " )";
                                                            whereClause = whereClause + " " + clueKey;
                                                        }
                                                    }
                                                    whereClause = whereClause + ")";
                                                }
                                            }
                                        }

                                        whereClause = whereClause.substring(0, whereClause.length() - 4) + ")";
                                        List<Task> dbTasks = fullTextSearch(whereClause, fromTable, subtask, script);


                                        //set the local values for these new tasks
                                        for (Task dbtask : dbTasks) {
                                            if(dbtask.getPid() instanceof Email){
                                                if(pids.contains(((Email) dbtask.getPid()).getId())){
                                                    continue;
                                                }else{
                                                    pids.add(((Email) dbtask.getPid()).getId());
                                                }
                                            }else if (dbtask.getPid() instanceof Message){
                                                if(pids.contains(((Message) dbtask.getPid()).get_id())){
                                                    continue;
                                                }else{
                                                    pids.add(((Message) dbtask.getPid()).get_id());
                                                }
                                            }
                                            String taskName = dbtask.getName();
                                            System.out.println("Task is: " + taskName);
                                            Object pid = dbtask.getPid();
                                            if (pid instanceof Email) {
                                                System.err.println("Task = " + taskName + ", Email = " + ((Email) pid).get_id());
                                                TaskLocalValues w5hInfo = new TaskLocalValues();
                                                w5hInfo.setLocalProperties(new LocalProperties("where", "whereIsEmailEvent"));
                                                w5hInfo.setLocal_value(whereValue);
                                                tasklocalValuesDao.create(w5hInfo);
                                                dbtask.addLocalValue(w5hInfo);
                                            } else if (pid instanceof Message) {
                                                System.err.println("Task = " + taskName + ", Message = " + ((Message) pid).get_id());
                                                TaskLocalValues w5hInfo = new TaskLocalValues();
                                                w5hInfo.setLocalProperties(new LocalProperties("where", "whereIsMessageEvent"));
                                                w5hInfo.setLocal_value(whereValue);
                                                tasklocalValuesDao.create(w5hInfo);
                                                dbtask.addLocalValue(w5hInfo);
                                            }
                                            ArrayList<LocalProperties> taskLocals = helper.extractTaskLocals(taskName);
                                            dbtask.getTaskDefinition().setLocals(taskLocals);

                                            if (taskLocals != null) {
                                                for (LocalProperties w5h : taskLocals) {
                                                    W5hLocals locals = JsonTriggerFactory.getLocals(getContext());
                                                    ArrayList<String> localValue = locals.getLocals(w5h.getW5h_value(), dbtask, getContext());
                                                    if (localValue.size() > 0) {
                                                        for (String lValue : localValue) {
                                                            TaskLocalValues w5hInfo = new TaskLocalValues();
                                                            w5hInfo.setLocalProperties(w5h);
                                                            w5hInfo.setLocal_value(lValue);
                                                            tasklocalValuesDao.create(w5hInfo);
                                                            dbtask.addLocalValue(w5hInfo);
                                                        }
                                                    }
                                                }
                                            }
                                            newTasks.add(dbtask);
                                        }

                                    }

                                }
                            }
                        }
                    }

                }
            }
        }

        Log.d(TAG, "SIZE OF tasks after retrieving where in text: " + newTasks.size());

        return newTasks;
    }



    public ArrayList<Task> setLocalValuesInTheTasks(List <Task> tasksFound) throws SQLException, IOException {
        TriggersFactory JsonTriggerFactory = TriggersFactory.getTriggersFactory(TriggersFactory.json);

        /*extract all the local properties from the tasks*/
        for (Task task:tasksFound) {
            String taskName = task.getName();
            System.out.println("Task is: " + taskName);
            Object pid = task.getPid();
            if (pid instanceof Event) {
                System.err.println("Task = " + taskName + ", Event = " + ((Event) pid).get_id());

            } else if (pid instanceof Email) {
                System.err.println("Task = " + taskName + ", Email = " + ((Email) pid).get_id());

            } else if (pid instanceof Transaction) {
                System.err.println("Task = " + taskName + ", Transaction = " + ((Transaction) pid).getMerchant_name());

            } else if (pid instanceof Feed) {
                System.err.println("Task = " + taskName + ", Feed = " + ((Feed) pid).getMessage());
            } else if (pid instanceof Message) {
                System.err.println("Task = " + taskName + ", Message = " + ((Message) pid).get_id());
            }else if (pid instanceof Photo) {
                System.err.println("Task = " + taskName + ", Photo = " + ((Photo) pid).get_id());
            }
            ArrayList<LocalProperties> taskLocals = helper.extractTaskLocals(taskName);

            task.getTaskDefinition().setLocals(taskLocals);

            if (taskLocals!=null){
                for(LocalProperties w5h:taskLocals){
                    W5hLocals locals = JsonTriggerFactory.getLocals(getContext());
                    ArrayList<String> localValue = locals.getLocals(w5h.getW5h_value(), task, getContext());
                    if (localValue.size()>0) {
                        for (String lValue:localValue) {
                            TaskLocalValues w5hInfo = new TaskLocalValues();
                            w5hInfo.setLocalProperties(w5h);
                            w5hInfo.setLocal_value(lValue);
                            //w5hInfo.setTask(task);
                            tasklocalValuesDao.create(w5hInfo);
                            task.addLocalValue(w5hInfo);
                        }
                    }
                }
            }
        }


        return (ArrayList<Task>) tasksFound;
    }




    public ArrayList<Script> createScriptPerTask(ArrayList<ArrayList<Task>> taskThreads) throws SQLException, IOException {
        ArrayList<Script> scripts = new ArrayList<Script>();

        for(ArrayList<Task> tasksRunning:taskThreads) {
            //put all tasks local values under the abstract who, what, where dimensions

            HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

            ArrayList<Script> scriptList = new ArrayList<Script>();

            for (Task task : tasksRunning) {
                Script script = new Script();
                script.setScriptDefinition(new ScriptDefinition());
                ScriptDefinition taskScriptDefinition = task.getScript().getScriptDefinition();
                script.addTask(task);
                HashSet<String> values = null;
                //Get Task's localValues e.g. whenPaid, wherePaymentOccured and put them under when, where, who etc.
                for (TaskLocalValues taskLocalValues : task.getLocalValues()) {
                    String w5hLabel = taskLocalValues.getLocalProperties().getW5h_value();
                   // String w5hLabel = taskLocalValues.getLocalProperties().getW5h_label();
                    if (map.containsKey(w5hLabel)) {
                        values = map.get(w5hLabel);
                    } else {
                        if (w5hLabel != null) {
                            values = new HashSet<>();
                        }
                    }
                    if (taskLocalValues.getLocal_value() != null) {
                        //HashSet<String> values = map.get(w5hLabel);
                        if (w5hLabel.equalsIgnoreCase("who")) {
                            String whoValue = taskLocalValues.getLocal_value();
                            String[] whoNames = whoValue.split("<");
                            if (whoNames.length > 1) {
                                if (whoNames[0].contains("\"")) {
                                    values.add(whoNames[0].substring(1, whoNames[0].length() - 2));
                                } else {
                                    values.add(whoNames[0]);
                                }
                                map.put(w5hLabel, values);
                            } else {
                                values.add(whoValue);
                                map.put(w5hLabel, values);
                            }
                        } else if (w5hLabel.startsWith("when")) {
                            String localValue = taskLocalValues.getLocal_value();
                            Date extractedDate = null;
                            String parsedDate = null;
                           // values.add(localValue);
                            try {
//                                long timestamp = Long.valueOf(localValue);
//                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//                                cal.setTimeInMillis(timestamp);
//                                parsedDate = DateFormat.format("yyyy-MM-dd", cal).toString();


                                Format format = new SimpleDateFormat("yyyy-MM-dd");
                                extractedDate = new Date(localValue);
                                parsedDate = format.format(extractedDate);
                            } catch (Exception e) {
//                                e.printStackTrace();
//                                Calendar cal = Calendar.getInstance();
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//                                try {
//                                    cal.setTime(sdf.parse(localValue));// all done
//                                } catch (ParseException e1) {
//                                    e1.printStackTrace();
//                                }

                                Format format = new SimpleDateFormat("yyyy-MM-dd");
                                extractedDate = new Date(Long.parseLong(localValue));
                                parsedDate = format.format(extractedDate);
                            }
                            if (parsedDate != null) {
                                values.add(parsedDate);
                                map.put(w5hLabel, values);
                            }
                        } else {
                            values.add(taskLocalValues.getLocal_value());
                            map.put(w5hLabel, values);
                        }

                    }

                }

                taskScriptDefinition.addTaskMap(task.getName(), task.getTaskDefinition());
                script.setScriptDefinition(helper.getScriptDefinition(taskScriptDefinition.getId(), taskScriptDefinition));

                // script.setScriptDefinition(helper.getTopScripts(scriptName, ofType, tasksRunning));

//            script.setTasks(tasksRunning);

                script = this.setLocalValuesInSuperscripts(script, map, task);



                scriptList.add(script);

            }


            Script mergedScript = mergeScripts(scriptList);

            mergedScript.assignScore(getContext());

            scripts.add(mergedScript);

        }

        return scripts;
    }



    public Script mergeScripts(ArrayList<Script> scriptList){


        Script a =null;
        if (scriptList.size()>0){
            a = scriptList.get(0);
            for (int i=1;i<scriptList.size();i++){
                a.merge(scriptList.get(i));
            }

        }

        return a;
    }


    public Script deleteLocalValuesInSuperscripts(Script script, String where) throws IOException {


        //remove script local values
        ArrayList<ScriptLocalValues> copyOfScriptLocalValues = new ArrayList<>();
        copyOfScriptLocalValues.addAll(script.getLocalValues());
        for(ScriptLocalValues scriptLocalValues : copyOfScriptLocalValues){
            String label = scriptLocalValues.getLocalProperties().getW5h_label();
            if (label.equals("where") && !where.equals(scriptLocalValues.getLocal_value())) {
                script.getLocalValues().remove(scriptLocalValues);
            }
        }

        //remove task local values
        for(Task task: script.getTasks()) {
            if(task.getList_of_pids()!=null) {
                ArrayList<TaskLocalValues> copyOfLocalValues = new ArrayList<>();
                copyOfLocalValues.addAll(task.getLocalValues());
                for (TaskLocalValues taskLocalValues : copyOfLocalValues) {
                    String label = taskLocalValues.getLocalProperties().getW5h_label();
                    if (label.equals("where") && !where.equals(taskLocalValues.getLocal_value())) {
                        task.getLocalValues().remove(taskLocalValues);

                    }
                }
            }
        }

        ArrayList<Script> scriptList = script.getSubscripts();

        if (scriptList.size()>0) {
            for (Script subscript : scriptList) {
                deleteLocalValuesInSuperscripts(subscript, where);
            }
        }
        return script;
    }


    public Script setLocalValuesInSuperscripts(Script script, HashMap<String, HashSet<String>> map, Task task) throws IOException {

        TriggersFactory JsonTriggerFactory = TriggersFactory.getTriggersFactory(TriggersFactory.json);

        W5hLocals locals = JsonTriggerFactory.getLocals(getContext());


        for (LocalProperties localProp : script.getScriptDefinition().getLocalProperties()) {
            ArrayList<String> constraints = locals.getConstraints(localProp.getW5h_value(), getContext(), task);
            for (String constraint: constraints) {
                HashSet<String> localvalues = map.get(constraint);
                if (localvalues != null) {
                    for (String value : localvalues) {
                        ScriptLocalValues scriptLocalValues = new ScriptLocalValues();
                        scriptLocalValues.setLocalProperties(localProp);
                        //scriptLocalValues.setTask(task);
                        scriptLocalValues.setLocal_value(value);
                        script.addLocalValue(scriptLocalValues);
                    }
                }
            }
        }

        ArrayList<ScriptDefinition> scriptDefinitionList = script.getScriptDefinition().getSubscripts();

        if (scriptDefinitionList.size()>0) {
            for (ScriptDefinition subscriptDef : scriptDefinitionList) {
                Script subscript = new Script();
                subscript.setScriptDefinition(subscriptDef);
                subscript = setLocalValuesInSuperscripts(subscript, map, task);
                script.addSubscript(subscript);
            }
        }else{
            script.addTask(task);
        }

        return script;
    }


    public ArrayList<ArrayList<Task>> mergeTasksByEventDate(List<Task> tasks){
        Log.d(TAG,"SIZE OF tasks before merging by event date: " +tasks.size());
        ArrayList<ArrayList<Task>> listofMergedTasks = new ArrayList<ArrayList<Task>>();
        HashMap<Date, ArrayList<Task>> hashMap = new HashMap<Date, ArrayList<Task>>();

        for (Task task:tasks){
            Date extractedDate=null;
            if (task.getPid() instanceof Transaction){
                try {
                    extractedDate = sdf.parse(sdf.format(((Transaction) task.getPid()).getDate()));
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
                }
                else if (((Email) task.getPid()).getBodyDate()!=null) {
                    try {
                        extractedDate = sdf.parse(sdf.format(((Email) task.getPid()).getBodyDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }else if (task.getPid() instanceof Event){
                try {
                    extractedDate = sdf.parse(sdf.format(((Event) task.getPid()).getStartTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else if (task.getPid() instanceof Message){
                if (((Message) task.getPid()).getContentDate()!=null) {
                    try {
                        extractedDate = sdf.parse(sdf.format(((Message) task.getPid()).getContentDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }else if (task.getPid() instanceof Feed){
                try {
                    extractedDate = sdf.parse(sdf.format(((Feed) task.getPid()).getCreated_time()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else if (task.getPid() instanceof Photo){
                try {
                    extractedDate = sdf.parse(sdf.format(((Photo) task.getPid()).getCreated_time()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else if (task.getList_of_pids()!=null) {
                try {
                    extractedDate = sdf.parse(sdf.format(((Place) task.getList_of_pids().get(0)).getSp().getArrive()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if(extractedDate!=null) {
                if (!hashMap.containsKey(extractedDate)) {
                    ArrayList<Task> list = new ArrayList<Task>();
                    list.add(task);
                    hashMap.put(extractedDate, list);
                } else {
                    hashMap.get(extractedDate).add(task);
                }
            }else{
                ArrayList<Task> notMergedTask = new ArrayList<Task>();
                notMergedTask.add(task);
                listofMergedTasks.add(notMergedTask);
            }

        }


        for (Date date: hashMap.keySet()){
            boolean isTransaction=false;
            boolean isEvent=false;
            boolean isGps=false;


            ArrayList<Task> list = new ArrayList<>();
            for(int i=0;i<hashMap.get(date).size();i++){
                if (hashMap.get(date).get(i).getPid() instanceof Transaction){
                    if(!isTransaction){
                        isTransaction=true;
                        list.add(hashMap.get(date).get(i));
                    }else{
                        ArrayList<Task> list2 = new ArrayList<>();
                        list2.add(hashMap.get(date).get(i));
                        listofMergedTasks.add(list2);
                    }
                }else if (hashMap.get(date).get(i).getPid() instanceof Event){
                    if(!isEvent){
                        isEvent=true;
                        list.add(hashMap.get(date).get(i));
                    }else{
                        ArrayList<Task> list2 = new ArrayList<>();
                        list2.add(hashMap.get(date).get(i));
                        listofMergedTasks.add(list2);
                    }
                }else if (hashMap.get(date).get(i).getList_of_pids().size()>0){
                    if(!isGps){
                        isGps=true;
                        list.add(hashMap.get(date).get(i));
                    }else{
                        ArrayList<Task> list2 = new ArrayList<>();
                        list2.add(hashMap.get(date).get(i));
                        listofMergedTasks.add(list2);
                    }
                }else{
                    list.add(hashMap.get(date).get(i));
                }
            }
            listofMergedTasks.add(list);
            //listofMergedTasks.add(hashMap.get(date));
        }



        Log.d(TAG,"SIZE OF tasks AFTER merging by event date: " +listofMergedTasks.size());

        return listofMergedTasks;


//        Log.d(TAG,"SIZE OF PROCESSES after: " +hashMap.size());
//        for (Map.Entry entry : hashMap.entrySet()) {
//            Log.d(TAG,"Date: " +entry.getKey());
//            for (Task t:(List<Task>)entry.getValue()){
//                if (t.getPid() instanceof Event) {
//                    Log.d(TAG,"Event = " + ((Event) t.getPid()).getDescription());
//                }else if (t.getPid() instanceof Email){
//                    Log.d(TAG, "Email = "+((Email) t.getPid()).getSubject());
//                }else if (t.getPid() instanceof Transaction){
//                    Log.d(TAG,"Transaction = "+((Transaction) t.getPid()).getName());
//                }
//            }
//        }

    }




    public ArrayList<Script> mergeScriptsByWhenAndWhere(List<Script> scripts) throws SQLException {

        Log.d(TAG,"SIZE OF scripts before merging: " +scripts.size());
        ArrayList<Script> listofMergedScripts = new ArrayList<>();
        HashMap<String, ArrayList<Script>> scriptHashMap = new HashMap<>();
        HashMap<String, ArrayList<Script>> transactionsHashMap = new HashMap<>();
        List<Script> copyOfScripts = new ArrayList<>();
        copyOfScripts.addAll(scripts);


        for (Script script:scripts) {
            String when = null;
            String where = null;

            if (script.getTasks().get(0).getPid() instanceof Transaction) {
                //get the when and where values from scripts (in case of GPS put all the possible wheres into whereList)
                for (ScriptLocalValues scriptLocalValues : script.getLocalValues()) {
                    String label = scriptLocalValues.getLocalProperties().getW5h_label();
                    if (label.equals("when")) {
                        when = scriptLocalValues.getLocal_value();
                    } else if (label.equals("where")) {
                        ArrayList<Place> officialPlaces = helper.getOfficialNameOfTranscationPlace(((Transaction) script.getTasks().get(0).getPid()).get_id());
                        where = officialPlaces.get(0).getName();
                    }
                }
                String where_when = where + when;
                if (transactionsHashMap.containsKey(where_when)) {
                    transactionsHashMap.get(where_when).add(script);
                } else {
                    ArrayList<Script> list = new ArrayList<>();
                    list.add(script);
                    transactionsHashMap.put(where_when, list);
                }
                copyOfScripts.remove(script);
            }
//            }else if (script.getTasks().get(0).getPid() instanceof Email && ((Email) script.getTasks().get(0).getPid()).getFrom().getName().contains("OpenTable")){
//                for (ScriptLocalValues scriptLocalValues : script.getLocalValues()) {
//                    String label = scriptLocalValues.getLocalProperties().getW5h_label();
//                    if (label.equals("when")) {
//                        when = scriptLocalValues.getLocal_value();
//                    } else if (label.equals("where")) {
//                        where = scriptLocalValues.getLocal_value();
//                    }
//                }
//                String where_when = where+when;
//                if(transactionsHashMap.containsKey(where_when)){
//                    transactionsHashMap.get(where_when).add(script);
//                }else {
//                    ArrayList<Script> list = new ArrayList<>();
//                    list.add(script);
//                    transactionsHashMap.put(where_when,list);
//                }
//                copyOfScripts.remove(script);
//            }

        }

        HashMap<String, ArrayList<Script>> leftTransactions = new HashMap<>(transactionsHashMap); //hashmap for where->scripts

        if(copyOfScripts.size()<scripts.size()) { //that means we have bank transactions

            for (Script script : copyOfScripts) {

                String when = null;
                ArrayList<String> whereList = new ArrayList<>();

                //get the when and where values from scripts (in case of GPS put all the possible wheres into whereList)
                for (ScriptLocalValues scriptLocalValues : script.getLocalValues()) {
                    String label = scriptLocalValues.getLocalProperties().getW5h_label();
                    if (label.equals("when")) {
                        when = scriptLocalValues.getLocal_value();
                    } else if (label.equals("where")) {
                        whereList.add(scriptLocalValues.getLocal_value());
                    }
                }

                if (when != null && whereList.size() != 0) {
                    boolean added=false;

                    for (String where : whereList) {
                        String where_when = where + when;

                        //if where is the same and when is +-2days (because of Transactions)
                        if (transactionsHashMap.size() != 0) {
                            String plus2Days = plusNDays(when, 2);
                            String plus1Day = plusNDays(when, 1);
                            String where_when1 = where + plus1Day;
                            String where_when2 = where + plus2Days;

                            if (transactionsHashMap.containsKey(where_when)) {
                                added=true;
                                try {
                                    script = deleteLocalValuesInSuperscripts(script, where);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
//                            for(Task task: script.getTasks()) {
//                                if(task.getList_of_pids()!=null) {
//                                    ArrayList<TaskLocalValues> copyOfLocalValues = new ArrayList<>();
//                                    copyOfLocalValues.addAll(task.getLocalValues());
//                                    for (TaskLocalValues taskLocalValues : copyOfLocalValues) {
//                                        String label = taskLocalValues.getLocalProperties().getW5h_label();
//                                        if (label.equals("where") && !where.equals(taskLocalValues.getLocal_value())) {
//                                            task.getLocalValues().remove(taskLocalValues);
//                                            ArrayList<ScriptLocalValues> copyOfScriptLocalValues = new ArrayList<>();
//                                            copyOfScriptLocalValues.addAll(script.getLocalValues());
//                                            for(ScriptLocalValues scriptLocalValues : copyOfScriptLocalValues){
//                                                if(scriptLocalValues.getLocal_value().equals(taskLocalValues.getLocal_value())){
//                                                    script.getLocalValues().remove(scriptLocalValues);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                                if (scriptHashMap.containsKey(where_when)) {
                                    scriptHashMap.get(where_when).add(script);
                                } else {
                                    ArrayList<Script> list = new ArrayList<>();
                                    list.add(script);
                                    scriptHashMap.put(where_when, list);
                                    list.addAll(transactionsHashMap.get(where_when));
                                    leftTransactions.remove(where_when);
                                }
                                break;
                            } else if (transactionsHashMap.containsKey(where_when1)) {
                                added=true;
                                try {
                                    script = deleteLocalValuesInSuperscripts(script, where);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
//                            for(Task task: script.getTasks()) {
//                                if(task.getList_of_pids()!=null) {
//                                    ArrayList<TaskLocalValues> copyOfLocalValues = new ArrayList<>();
//                                    copyOfLocalValues.addAll(task.getLocalValues());
//                                    for (TaskLocalValues taskLocalValues : copyOfLocalValues) {
//                                        String label = taskLocalValues.getLocalProperties().getW5h_label();
//                                        if (label.equals("where") && !where.equals(taskLocalValues.getLocal_value())) {
//                                            task.getLocalValues().remove(taskLocalValues);
//                                            ArrayList<ScriptLocalValues> copyOfScriptLocalValues = new ArrayList<>();
//                                            copyOfScriptLocalValues.addAll(script.getLocalValues());
//                                            for(ScriptLocalValues scriptLocalValues : copyOfScriptLocalValues){
//                                                if(scriptLocalValues.getLocal_value().equals(taskLocalValues.getLocal_value())){
//                                                    script.getLocalValues().remove(scriptLocalValues);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                                if (scriptHashMap.containsKey(where + plus1Day)) {
                                    scriptHashMap.get(where + plus1Day).add(script);
                                } else {
                                    ArrayList<Script> list = new ArrayList<>();
                                    list.add(script);
                                    scriptHashMap.put(where + plus1Day, list);
                                    list.addAll(transactionsHashMap.get(where_when1));
                                    leftTransactions.remove(where_when1);
                                }
                                break;
                            } else if (transactionsHashMap.containsKey(where_when2)) {
                                added=true;
                                try {
                                    script = deleteLocalValuesInSuperscripts(script, where);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
//                            for(Task task: script.getTasks()) {
//                                if(task.getList_of_pids()!=null) {
//                                    ArrayList<TaskLocalValues> copyOfLocalValues = new ArrayList<>();
//                                    copyOfLocalValues.addAll(task.getLocalValues());
//                                    for (TaskLocalValues taskLocalValues : copyOfLocalValues) {
//                                        String label = taskLocalValues.getLocalProperties().getW5h_label();
//                                        if (label.equals("where") && !where.equals(taskLocalValues.getLocal_value())) {
//                                            task.getLocalValues().remove(taskLocalValues);
//                                            ArrayList<ScriptLocalValues> copyOfScriptLocalValues = new ArrayList<>();
//                                            copyOfScriptLocalValues.addAll(script.getLocalValues());
//                                            for(ScriptLocalValues scriptLocalValues : copyOfScriptLocalValues){
//                                                if(scriptLocalValues.getLocal_value().equals(taskLocalValues.getLocal_value())){
//                                                    script.getLocalValues().remove(scriptLocalValues);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                                if (scriptHashMap.containsKey(where + plus2Days)) {
                                    scriptHashMap.get(where + plus2Days).add(script);
                                } else {
                                    ArrayList<Script> list = new ArrayList<>();
                                    list.add(script);
                                    list.addAll(transactionsHashMap.get(where_when2));
                                    scriptHashMap.put(where + plus2Days, list);
                                    leftTransactions.remove(where_when2);
                                }

                                break;
                            }
                        } else {
                            if (scriptHashMap.containsKey(where_when)) {
                                scriptHashMap.get(where_when).add(script);
                            } else {
                                ArrayList<Script> list = new ArrayList<>();
                                list.add(script);
                                scriptHashMap.put(where_when, list);
                            }
                        }
                    }
                    if(!added){
                        listofMergedScripts.add(script);

                    }

                } else {
                    //not mergedScript
                    // if (script.getScore()>0.5) {
                    listofMergedScripts.add(script);
                    //}
                }
            }
        }else{
            listofMergedScripts.addAll(scripts);
        }

        for(String tr:leftTransactions.keySet()){
            listofMergedScripts.addAll(leftTransactions.get(tr));
//            for(Script s:leftTransactions.get(tr)){
//               // if (s.getScore()>0.5) {
//                    listofMergedScripts.add(s);
//                //}
//            }

        }


        for (String whenAndWhere: scriptHashMap.keySet()) {
            Script mergedScript;
            mergedScript = scriptHashMap.get(whenAndWhere).get(0);
            for (int i=1; i< scriptHashMap.get(whenAndWhere).size();i++) {
                mergedScript.merge(scriptHashMap.get(whenAndWhere).get(i));
            }
            mergedScript.assignScore(getContext());
            //if (mergedScript.getScore()>0.5) {
            listofMergedScripts.add(mergedScript);
            //}
        }

        Log.d(TAG,"SIZE OF scripts after merging: " +listofMergedScripts.size());

        return listofMergedScripts;

    }


    public String plusNDays(String day1, int difference){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar plusNDays = Calendar.getInstance(Calendar.getInstance().getTimeZone());
        try {
            plusNDays.setTimeInMillis(simpleDateFormat.parse(day1).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        plusNDays.add(Calendar.DATE, +difference);
        String formatted = simpleDateFormat.format(plusNDays.getTime());

        return formatted;
    }

    public boolean hasNdaysDifference(String day1, String day2, int difference){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar minusTwoDays = Calendar.getInstance(Calendar.getInstance().getTimeZone());
        try {
            minusTwoDays.setTimeInMillis(simpleDateFormat.parse(day1).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        minusTwoDays.add(Calendar.DATE, -difference);

        Calendar plusTwoDays = Calendar.getInstance(Calendar.getInstance().getTimeZone());
        try {
            plusTwoDays.setTimeInMillis(simpleDateFormat.parse(day1).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        plusTwoDays.add(Calendar.DATE, +difference);

        Calendar hashedWhen = Calendar.getInstance(Calendar.getInstance().getTimeZone());
        try {
            hashedWhen.setTimeInMillis(simpleDateFormat.parse(day2).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (hashedWhen.compareTo(plusTwoDays) <= 0 && hashedWhen.compareTo(minusTwoDays) >= 0) {
            return true;
        }
        return false;
    }



	public ArrayList<ArrayList<Task>> mergeThreads(ArrayList<ArrayList<Task>> tasks){

        System.err.println("Total processes running before threading ="+tasks.size());

        ArrayList<ArrayList<Task>> listofMergedTasks = new ArrayList<>();

		//hashmap of key:threadId and value:task
        HashMap<String,ArrayList<Task>> mergeTasksByThread = new HashMap();
        ArrayList<ArrayList<Task>> alreadyMergedTasks=new ArrayList<>();


        for (ArrayList<Task> tasksInAScript: tasks){
            String previous_thread_id=null;
            ArrayList<Task> tasklist = null;
            if(tasksInAScript.size()>1) {
                alreadyMergedTasks.add(tasksInAScript);
                continue;
            }
            for (Task task : tasksInAScript) {
                if (task.getPid() instanceof Email) {
                    String key = ((Email) task.getPid()).getThreadId();
                    previous_thread_id = key;
                    ArrayList<Task> list = mergeTasksByThread.get(key);
                    if (list == null) {
                        list = new ArrayList<>();
                        list.add(task);
                        mergeTasksByThread.put(key, list);
                    } else {
                        list.add(task);
                    }
                } else if (task.getPid() instanceof Message) {
                    String msg = ((Message) task.getPid()).getContent();
                    if (msg.contains("voted for") || msg.contains("changed vote to") || msg.contains("removed vote for") || msg.contains("responded Going to") || msg.contains("responded Can't Go to")) {
                        continue;
                    }
                    String thread = ((Message) task.getPid()).getThread();
                    int thread_id = ((Message) task.getPid()).getThread_id();
                    previous_thread_id = thread + "_" + thread_id;

                    ArrayList<Task> list = mergeTasksByThread.get(previous_thread_id);
                    if (list == null) {
                        list = new ArrayList<>();
                        list.add(task);
                        mergeTasksByThread.put(previous_thread_id, list);
                    } else {
                        list.add(task);
                    }


                } else {
                    if (previous_thread_id != null) {
                        ArrayList<Task> list = mergeTasksByThread.get(previous_thread_id);
                        list.add(task);
                        mergeTasksByThread.put(previous_thread_id, list);
                    } else {
                        if (tasklist == null) {
                            tasklist = new ArrayList<>();
                            tasklist.add(task);
                        } else {
                            tasklist.add(task);
                        }

                    }
                }
            }
            if(tasklist!=null) {
                listofMergedTasks.add(tasklist);
            }
        }


        for (ArrayList<Task> alreadyMerged:alreadyMergedTasks) {
            String previous_thread_id=null;
            ArrayList<Task> tasklist = new ArrayList<>();
            for (Task task : alreadyMerged) {
                if (task.getPid() instanceof Email) {
                    String key = ((Email) task.getPid()).getThreadId();
                    previous_thread_id = key;
                    ArrayList<Task> list = mergeTasksByThread.get(key);
                    if (list != null) {
                        list.addAll(alreadyMerged);
                        break;
                    }
                } else if (task.getPid() instanceof Message) {
                    String msg = ((Message) task.getPid()).getContent();
                    if (msg.contains("voted for") || msg.contains("changed vote to") || msg.contains("removed vote for") || msg.contains("responded Going to") || msg.contains("responded Can't Go to")) {
                        continue;
                    }
                    String thread = ((Message) task.getPid()).getThread();
                    int thread_id = ((Message) task.getPid()).getThread_id();
                    previous_thread_id = thread + "_" + thread_id;

                    ArrayList<Task> list = mergeTasksByThread.get(previous_thread_id);
                    if (list != null) {
                        list.addAll(alreadyMerged);
                        break;
                    }
                } else {
                    if (previous_thread_id != null) {
                        ArrayList<Task> list = mergeTasksByThread.get(previous_thread_id);
                        if (list!=null) {
                            list.add(task);
                        }else{
                            list=new ArrayList<Task>();
                            list.add(task);
                        }
                        mergeTasksByThread.put(previous_thread_id, list);
                    } else {
                        tasklist.add(task);
                    }

                }
            }
            if(tasklist!=null && tasklist.size()>0) {
                listofMergedTasks.add(tasklist);
            }
        }







        for (String thread_id: mergeTasksByThread.keySet()) {
            ArrayList<Task> mergedtasks = mergeTasksByThread.get(thread_id);
            listofMergedTasks.add(mergedtasks);
        }

		System.err.println("Total processes running after threading ="+listofMergedTasks.size());

        return listofMergedTasks;
	}


    public List<Task> findTaskInstancesInDatabase(HashMap<Object, Object> triggers_Clues) throws SQLException{

        for (HashMap.Entry<Object, Object> entry : triggers_Clues.entrySet()) {
            String scriptType = ((String)entry.getKey()).replace("\"", "");;
            System.err.println("Script = "+scriptType);
            String[] scriptArray = scriptType.split("<");
            String scriptName=null;
            String typeOf=null;
            if (scriptArray!=null){
                scriptName = scriptArray[0];
                typeOf = scriptArray[1].substring(0,scriptArray[1].length()-1);
            }else{
                scriptName=scriptType;
            }
            ScriptDefinition sd = helper.getScriptDefinition(scriptName,typeOf);
            Script script = new Script();
            script.setScriptDefinition(sd);
            List<HashMap<Object, Object>> values = (List<HashMap<Object, Object>>) entry.getValue();
            if (values!=null) {
                for (HashMap<Object, Object> value : values) {
                    for (HashMap.Entry<Object, Object> subtasks : value.entrySet()) {
                        String subtask = (String)subtasks.getKey();
                        //System.out.println("Subtask = " +subtask);
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
                                                    if (andOrKey.contains("Transaction") || andOrKey.contains("Category") || andOrKey.contains("Feed") || andOrKey.contains("Person") || andOrKey.contains("Photo") || andOrKey.contains("Place")){
                                                        whereClause = whereClause + " = " + andOrValue.toString().replace("\"", "");
                                                    }else {
                                                        //System.out.println("Clue value = " + andOrValue.toString().replace("\"", ""));
                                                        whereClause = whereClause + " = '" + andOrValue.toString().replace("\"", "") + "'";
                                                    }
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
                            if (fromClause.contains("Transaction")){
                                query="select distinct `Transaction`._id, `Transaction`.merchant_name ";
                            }
                            if (fromClause.contains("Feed")){
                                query="select distinct `Feed`._id, `Feed`.message, `Feed`.place_id ";
                            }
                            if (fromClause.contains("Photo")){
                                query="select distinct `Photo`._id, `Photo`.name, `Photo`.place_id ";
                            }
                            if (fromClause.contains("StayPointHasPlaces")){
                                query="select distinct `StayPointHasPlaces`.stayPoint_id ";
                            }
                            query = query + fromClause + whereClause;
                            query=query.substring(0,query.length()-4) +");";
                            System.out.println("QUERY = " + query);
                            try {
                                List<Task> tasksFound = queryDatabase(query,fromClause,subtask,script);
                                System.out.println(tasksFound.size());
                                tasksRunning.addAll(tasksFound);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            foundKeywordsFile=false;
                            whereClause=whereClause.substring(0,whereClause.length()-4) +")";
                            tasksRunning.addAll(fullTextSearch(whereClause,fromTable, subtask, script));
                        }

                    }
                }
            }
        }

        return tasksRunning;
    }

    public List<Task> fullTextSearch(String whereClause, String fromTable, String subtask, Script script) throws SQLException {

        fromTable=fromTable.toString().replace("\"", "");
        List<Task> tasks = new ArrayList<Task>();

        String query = "SELECT * FROM "+ fromTable +" WHERE \"_id\" IN (select \"_id\" from "+ fromTable + "_fts "+ whereClause +";"; //`"+ text_column +"` MATCH '("+ scriptKeywords+")')";
        query = query.replace(" or "," UNION select \"_id\" from " + fromTable + "_fts where ( " );
        System.out.println("FULLTEXTQUERY = " + query);
        //query = "SELECT * FROM Email WHERE \"_id\" IN (select \"_id\" from Email_fts  where  `textContent` MATCH '(\"restaurant\" OR \"dinner\" OR \"lunch\" )' UNION select \"_id\" from Email_fts  where `subject` MATCH '(\"restaurant\" OR \"dinner\" OR \"lunch\" )');";

        if (fromTable.equals("Email")){
            GenericRawResults<Email> rawResults = helper.getEmailDao().queryRaw(query,helper.getEmailDao().getRawRowMapper());
            if (rawResults!=null) {
                for (Email email : rawResults.getResults()) {
                    Email emailUpdated = helper.getToCcBcc(email);
                    Task task = new Task();
                    task.setPid(emailUpdated);
                    task.setName(subtask);
                    task.setScript(script);
                    task.setTaskDefinition(new TaskDefinition(subtask));
                    tasks.add(task);
                }
            }
        }if (fromTable.equals("Message")){
            GenericRawResults<Message> rawResults = helper.getMessageDao().queryRaw(query,helper.getMessageDao().getRawRowMapper());
            if (rawResults!=null) {
                for (Message msg : rawResults.getResults()) {
                    Message msgUpdated = helper.getToMessage(msg);
                    Task task = new Task();
                    task.setPid(msgUpdated);
                    task.setName(subtask);
                    task.setScript(script);
                    task.setTaskDefinition(new TaskDefinition(subtask));
                    tasks.add(task);
                }
            }
        }
        if (fromTable.equals("Transaction")){
            GenericRawResults<Transaction> rawResults = helper.getTransactionDao().queryRaw(query,helper.getTransactionDao().getRawRowMapper());
            for (Transaction transaction: rawResults.getResults()){
                Task task = new Task();
                task.setOid(transaction.getId());
                task.setPid(transaction);
                task.setName(subtask);
                task.setTaskDefinition(new TaskDefinition(subtask));
                task.setScript(script);
                tasks.add(task);
            }
        }
        if (fromTable.equals("Event")){
            GenericRawResults<Event> rawResults = helper.getEventDao().queryRaw(query,helper.getEventDao().getRawRowMapper());
            for (Event event: rawResults.getResults()){
                Task task = new Task();
                task.setOid(event.getId());
                task.setPid(event);
                task.setName(subtask);
                task.setTaskDefinition(new TaskDefinition(subtask));
                task.setScript(script);
                tasks.add(task);

            }
        }

        return tasks;
    }

    public List<Task> queryDatabase(String query, String fromTable, String subtask, Script script) throws SQLException {
        List<Task> tasks = new ArrayList<Task>();


        GenericRawResults<String[]> rawResults = helper.getTransactionDao().queryRaw(query);
       // List<String[]> results = null;
        if (rawResults!=null){
           // if (rawResults.getResults().size()>0){
                if (fromTable.contains("Transaction")){
                    for (String[] tuple:rawResults.getResults()) {
                        Transaction transaction = new Transaction();
                        transaction.setId(tuple[0]);
                        transaction.setMerchant_name(tuple[1]);

                        String tempQuery = "select * from `Transaction` where `_id`=" + tuple[0];
                        GenericRawResults<Transaction> transactionData = helper.getTransactionDao().queryRaw(tempQuery, helper.getTransactionDao().getRawRowMapper());
//                        try {
//                            System.err.println("TransFOUND = " + transactionData.getResults().size());
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
                        try {
                            for (Transaction fulltransaction : transactionData.getResults()) {
                                Task task = new Task();
                                task.setOid(fulltransaction.getId());
                                task.setPid(fulltransaction);
                                task.setName(subtask);
                                task.setScript(script);
                                task.setTaskDefinition(new TaskDefinition(subtask));
                                tasks.add(task);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }


                    }
                }else if (fromTable.contains("Feed")){
                    for (String[] tuple:rawResults.getResults()) {
                        Feed feed = new Feed();
                        feed.setId(tuple[0]);
                        feed.setMessage(tuple[1]);

                        String tempQuery = "select * from `Feed` where `_id`=" + tuple[0];
                        GenericRawResults<Feed> feedData = helper.getFeedDao().queryRaw(tempQuery, helper.getFeedDao().getRawRowMapper());
//                        try {
//                            System.err.println("TransFOUND = " + transactionData.getResults().size());
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
                        try {
                            for (Feed fullFeed : feedData.getResults()) {
                                Task task = new Task();
                                task.setOid(fullFeed.getId());
                                task.setPid(fullFeed);
                                task.setName(subtask);
                                task.setScript(script);
                                task.setTaskDefinition(new TaskDefinition(subtask));
                                tasks.add(task);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (fromTable.contains("Photo")){
                    for (String[] tuple:rawResults.getResults()) {
                        Photo photo = new Photo();
                        photo.setId(tuple[0]);
                        photo.setName(tuple[1]);

                        String tempQuery = "select * from `Photo` where `_id`=" + tuple[0];
                        GenericRawResults<Photo> photoData = helper.getPhotoDao().queryRaw(tempQuery, helper.getPhotoDao().getRawRowMapper());

//                        try {
//                            System.err.println("TransFOUND = " + transactionData.getResults().size());
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
                        try {
                            for (Photo fullPhoto : photoData.getResults()) {
                                Task task = new Task();
                                task.setOid(fullPhoto.getId());
                                task.setPid(fullPhoto);
                                task.setName(subtask);
                                task.setScript(script);
                                task.setTaskDefinition(new TaskDefinition(subtask));
                                tasks.add(task);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (fromTable.contains("StayPointHasPlaces")){
                    for (String[] tuple:rawResults.getResults()) {


                        String tempQuery = "select Place.* from `Place`,`StayPointHasPlaces` where StayPointHasPlaces.stayPoint_id=" + tuple[0]+ " and StayPointHasPlaces.place_id = Place._id ";
                        GenericRawResults<Place> placeData = helper.getPlaceDao().queryRaw(tempQuery, helper.getPlaceDao().getRawRowMapper());

                        Task task = new Task();
                        task.setName(subtask);
                        task.setScript(script);
                        task.setTaskDefinition(new TaskDefinition(subtask));
                        tasks.add(task);
                        try {
                            for (Place fullplace : placeData.getResults()) {
                                String tempQ = "select * from StayPoint where _id=" + tuple[0];
                                GenericRawResults<StayPoint> sp = helper.getStayPointRuntimeDao().queryRaw(tempQ, helper.getStayPointRuntimeDao().getRawRowMapper());
                                fullplace.setSp(sp.getFirstResult());
                                task.addOid(fullplace.getId());
                                task.addPid(fullplace);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (fromTable.contains("Person")){

                    for (String[] tuple:rawResults.getResults()) {
                        Email email = new Email();
                        email.setId(tuple[0]);

                        String tempQuery = "select * from `Email` where `_id`=" + tuple[0];


                        GenericRawResults<Email> emailResults = helper.getEmailDao().queryRaw(tempQuery, helper.getEmailDao().getRawRowMapper());
                        if (emailResults != null) {
                            for (Email fullEmail : emailResults.getResults()) {
                                Email emailUpdated = helper.getToCcBcc(fullEmail);
                                Task task = new Task();
                                task.setPid(emailUpdated);
                                task.setName(subtask);
                                task.setScript(script);
                                task.setTaskDefinition(new TaskDefinition(subtask));
                                tasks.add(task);
                            }
                        }
                    }


                }
          //  }
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



    public class CustomListAdapter extends ArrayAdapter<Script> {

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
            View rowView=inflater.inflate(R.layout.restaurantsview, null,false);
            LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayout);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            Place place = null;


            for (Task task: itemname.get(position).getTasks()) {
                if (task.getPid() instanceof Transaction) {
                    try {
                        ArrayList<Place> places = helper.getOfficialNameOfTranscationPlace(((Transaction) task.getPid()).get_id());
                        if (places!=null){
                            place = helper.getPlace(places.get(0).get_id());
                            //place=places.get(0);
                            break;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                } else if (task.getPid() instanceof Photo) {
                    place = ((Photo) task.getPid()).getPlace();
                    break;
                } else if (task.getPid() instanceof Feed) {
                    place = ((Feed) task.getPid()).getPlace();
                    break;
                }
            }

            if (place != null) {
                byte[] image = place.getImage();
                if (image != null) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 40, 40, false));
                }else{
                    imageView.setImageResource(imgid[0]);
                }
            }else{
                imageView.setImageResource(imgid[0]);

            }



            Script script = itemname.get(position);//.getScriptDefinition();
            HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
                    //txtTitle.setText(itemname.get(position).getScore()+", "+String.valueOf(((Email)processTask.getPid()).get_id()));


            ArrayList<ScriptLocalValues> localValues = script.getLocalValues();
            if (localValues != null) {
                for (ScriptLocalValues localValue : localValues) {
                    if (localValue != null) {
                        LocalProperties lp = localValue.getLocalProperties();
                        if (lp != null) {
                            String w5h_value = lp.getW5h_value();
                            if (map.containsKey(w5h_value)) {
                                ArrayList<String> values = map.get(w5h_value);
                                values.add(localValue.getLocal_value());
                                map.put(w5h_value, values);
                            } else {
                                if (w5h_value != null) {
                                    ArrayList<String> values = new ArrayList<String>();
                                    values.add(localValue.getLocal_value());
                                    map.put(w5h_value, values);
                                }
                            }
                        }
                    }
                }
            }

            for (String localLabel:map.keySet()) {
                StringBuilder sb = new StringBuilder();
                ArrayList<String> list = map.get(localLabel);
                Set<String> hs = new HashSet<>();
                hs.addAll(list);
                list.clear();
                list.addAll(hs);

                for (String localValue :list) {
                        sb.append(localValue);
                        sb.append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length() - 1);
               // }

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

            return rowView;
        };
    }


}


