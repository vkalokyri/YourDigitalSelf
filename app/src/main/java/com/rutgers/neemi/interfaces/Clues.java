package com.rutgers.neemi.interfaces;

import android.content.Context;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Clues {

	public List<HashMap<Object, Object>> getClues(String task, String onObject, Context context) throws FileNotFoundException;
	
}
