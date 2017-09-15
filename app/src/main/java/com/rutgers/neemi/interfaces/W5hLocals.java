package com.rutgers.neemi.interfaces;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface W5hLocals {

	List<String> getLocals(String local, Object pid) throws FileNotFoundException;
	
}
