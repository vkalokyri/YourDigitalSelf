package com.rutgers.neemi.util;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.ScriptHasTasks;
import com.rutgers.neemi.model.Subscript;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.parser.ScriptParser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by suitcase on 12/11/18.
 */

public class ApplicationManager {

    private static final String TAG = "ApplicationManager";
    static Map<String, Object> scriptElements;
    RuntimeExceptionDao<ScriptDefinition, String> scriptDefDao;
    RuntimeExceptionDao<Subscript, String> subscriptsDao;
    RuntimeExceptionDao<TaskDefinition, String> taskDefDao;
    RuntimeExceptionDao<ScriptHasTasks, String> scriptHasTasksDao;
    RuntimeExceptionDao<LocalProperties, String> localPropertiesDao;

    DatabaseHelper helper;

    public ApplicationManager(){

    }

    public void initScript(DatabaseHelper helper, Context context){
        this.helper=helper;

        ConfigReader config = new ConfigReader(context);
        String filename = config.getStr(PROPERTIES.SCRIPT_FILE);
        Log.d(TAG, "Read script file: "+ filename);


        this.scriptElements=new HashMap<String, Object>();

            /*parse the script and get the script definitions*/
        try {
            this.scriptElements = new ScriptParser().start(filename,null,"restaurant",context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scriptDefDao = helper.getScriptDefDao();
        subscriptsDao = helper.getSubScriptDao();
        taskDefDao = helper.getTaskDefinitionDao();
        scriptHasTasksDao = helper.getScriptHasTasksDao();
        localPropertiesDao = helper.getLocalPropertiesDao();

        storeScriptDefinition(this.scriptElements);
    }


    public void storeScriptDefinition(Map<String, Object> scriptElements){
        for (String key: scriptElements.keySet()) {
            if (key != null){
                ScriptDefinition scriptDefinition = (ScriptDefinition)scriptElements.get(key);
                ScriptDefinition scriptDef = helper.scriptDefinitionExists(scriptDefinition.getName(),scriptDefinition.getArgument());
                if (scriptDef == null) {
                    scriptDefDao.create(scriptDefinition);
                    ArrayList<LocalProperties> locals = scriptDefinition.getLocalProperties();
                    for (LocalProperties local : locals) {
                        localPropertiesDao.create(local);
                    }
                }else{
                    scriptDef = scriptDefinition;
                }


                for (String taskName : scriptDefinition.getTaskMap().keySet()) {
                    if (taskName != null) {
                        TaskDefinition taskDef = helper.taskDefinitionExists(taskName);
                        if (taskDef == null) {
                            taskDef = scriptDefinition.getTaskMap().get(taskName);
                            taskDefDao.create(taskDef);
                        }
                        ScriptHasTasks scriptTasks = new ScriptHasTasks();
                        scriptTasks.setTask(taskDef);
                        scriptTasks.setScript(scriptDefinition);
                        scriptHasTasksDao.create(scriptTasks);
                    }
                }

                //readSubscripts
                ScriptDefinition superScript = (ScriptDefinition) scriptElements.get(key);
                for (ScriptDefinition subscript : superScript.getSubscripts()) {
                    storeScriptDefinition(subscript,superScript);
                }
            }
        }
    }

    public void storeScriptDefinition(ScriptDefinition subscript, ScriptDefinition superScript){
        if (subscript != null) {
            ScriptDefinition scriptDef = helper.scriptDefinitionExists(subscript.getName(),subscript.getArgument());
            if (scriptDef == null) {
                scriptDefDao.create(subscript); //edw nomizw
                ArrayList<LocalProperties> locals = subscript.getLocalProperties();
                for (LocalProperties local: locals) {
                    localPropertiesDao.create(local);
                }
            }else{
                subscript.setId(scriptDef.getId());
            }

            Subscript subscript1 = new Subscript();
            subscript1.setSuperscript_id(superScript);
            subscript1.setSubscript_id(subscript);
            subscriptsDao.create(subscript1);

            for (String taskName : subscript.getTaskMap().keySet()) {
                if (taskName != null) {
                    TaskDefinition taskDef = helper.taskDefinitionExists(taskName);
                    if (taskDef == null) {
                        taskDef = subscript.getTaskMap().get(taskName);
                        taskDefDao.create(taskDef);
                        ArrayList<LocalProperties> locals = taskDef.getLocals();
                        for (LocalProperties local: locals) {
                            localPropertiesDao.create(local);
                        }
                    }
                    ScriptHasTasks scriptTasks = new ScriptHasTasks();
                    scriptTasks.setTask(taskDef);
                    scriptTasks.setScript(subscript);
                    scriptHasTasksDao.create(scriptTasks);
                }
            }


            for (ScriptDefinition script : subscript.getSubscripts()) {
                storeScriptDefinition(script,subscript);
            }



        }


    }



}
