package com.rutgers.neemi.parser;


import android.content.Context;

import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.interfaces.W5hLocals;
import com.rutgers.neemi.jsonTriggers.JsonFactory;

public abstract class TriggersFactory {
	
	public static final int json=1;
	public static final int xml=2;
	public static final int owl=3;


	public abstract Triggers getTriggers(Context context);
	public abstract Clues getClues(Context context);
	public abstract W5hLocals getLocals(Context context);

	public static TriggersFactory getTriggersFactory(int whichFactory){
		switch(whichFactory){
			case json: 
				return new JsonFactory();
			default:
				return null;
		}
	}
	
}