package com.rutgers.neemi.interfaces;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface W5hLocals {

	List<String> getLocals(String local, Object pid, Context context) throws IOException;
	
}
