package com.rutgers.neemi.jsonTriggers;


import android.content.Context;

import com.rutgers.neemi.interfaces.Clues;
import com.rutgers.neemi.interfaces.Triggers;
import com.rutgers.neemi.interfaces.W5hLocals;
import com.rutgers.neemi.parser.TriggersFactory;

public class JsonFactory extends TriggersFactory {

	public Triggers getTriggers(Context context){
		return new JsonTriggers(context);
	}

	public Clues getClues(Context context){
		return new JsonClues(context);
	}

	@Override
	public W5hLocals getLocals(Context context) {
		// TODO Auto-generated method stub
		return new JsonLocals(context);
	}

	
	
}
