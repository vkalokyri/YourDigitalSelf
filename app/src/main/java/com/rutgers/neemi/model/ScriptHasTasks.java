package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "ScriptHasTasks")
public class ScriptHasTasks implements Serializable {

    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Script(_id) ON DELETE CASCADE")
    Script script;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Task(_id) ON DELETE CASCADE")
    Task task;

    public ScriptHasTasks() {
        // for ormlite
    }

    public ScriptHasTasks(Task task, Script script) {
        this.task = task;
        this.script = script;
    }


    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
