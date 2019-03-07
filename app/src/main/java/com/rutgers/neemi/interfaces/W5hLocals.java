package com.rutgers.neemi.interfaces;

import android.content.Context;

import com.rutgers.neemi.model.Task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface W5hLocals {

	ArrayList<String> getLocals(String local, Task task, Context context) throws IOException;
    ArrayList<String> getConstraints(String local, Context context, Task task) throws IOException;
	
}
