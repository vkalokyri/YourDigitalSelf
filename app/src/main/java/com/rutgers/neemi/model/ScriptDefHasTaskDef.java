package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "ScriptDefHasTaskDef")
public class ScriptDefHasTaskDef implements Serializable {

    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    ScriptDefinition script;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    TaskDefinition task;

    public ScriptDefHasTaskDef() {
        // for ormlite
    }

    public ScriptDefHasTaskDef(TaskDefinition task, ScriptDefinition script) {
        this.task = task;
        this.script = script;
    }


    public ScriptDefinition getScript() {
        return script;
    }

    public void setScript(ScriptDefinition script) {
        this.script = script;
    }

    public TaskDefinition getTask() {
        return task;
    }

    public void setTask(TaskDefinition task) {
        this.task = task;
    }
}
