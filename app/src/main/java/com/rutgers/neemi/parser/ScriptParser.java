package com.rutgers.neemi.parser;

import android.content.Context;
import android.util.Xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.rutgers.neemi.model.Locals;
import com.rutgers.neemi.model.Process;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

public class ScriptParser {


	Map<String, Object> scriptElements;

	public ScriptParser(){
		scriptElements=new HashMap<String, Object>();
	}

	public Map<String, Object> start(String filename, Process parentProcess, Context context) throws IOException {

		InputStream fis = context.getAssets().open(filename);
		try {
			parse(fis, parentProcess, context);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scriptElements;
	}

	private static final String ns = null;

	public Process parse(InputStream in, Process parentProcess, Context context) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return extractXml(parser,parser.getName(),parentProcess,context);

		} finally {
			in.close();
		}
	}


	private Process extractXml(XmlPullParser parser, String element, Process parentProcess, Context context) throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, ns, element);
		int eventType = parser.getEventType();
		Process process =null;
		while(eventType!=XmlPullParser.END_DOCUMENT ){//|| !parser.getName().equals("definitions")){
			String name = parser.getName();
			//System.out.println("XMLPARSING = "+name);
			// Starts by looking for the process
			if (name.equals("process") && eventType!=XmlPullParser.END_TAG ) {
				String id =parser.getAttributeValue(null,"id");
				//System.err.println("I found process = "+id);
				process = new Process();
				process.setId(id);
				scriptElements.put(id, process);
				//extractXml(parser, name);
			}else if (name.equals("task")) {
				//System.err.println("I found task");
				Task task = new Task();
				task = readTask(parser, task);
				//System.out.println("Task = "+task.getName());
				process.addTask(task.getName(), task);
				//scriptElements.put(task.getName(),task);
				scriptElements.put(process.getId(),process);
				// eventType = parser.nextTag();
			}else if(name.equals("locals")){
				//System.err.println("I found process locals");
				process = readProcessLocals(parser,process);
				scriptElements.put(process.getId(), process);
			}else if(name.equals("callActivity")){
				//System.err.println("I found callActivity");
				String activityName = parser.getAttributeValue(null,"calledElement");
				start(activityName+".xml", process, context);

				// scriptElements.put(process.getId(),process);
				skip(parser);
			}
			eventType = parser.nextTag();
		}

		return parentProcess;
	}

	private Task readTask(XmlPullParser parser, Task task) throws IOException, XmlPullParserException {

		parser.require(XmlPullParser.START_TAG, ns, "task");
		//System.err.println("I set taskName");
		String taskName = parser.getAttributeValue(null,"id");
		task.setName(taskName);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if(name.equals("locals")) {
				//System.err.println("I found task locals");
				task = readTaskLocals(parser, task);
			}
		}
		return task;

	}

	// For the tags title and summary, extracts their text values.
	private Task readTaskLocals(XmlPullParser parser, Task task) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "locals");
		while (parser.next() != XmlPullParser.END_TAG || !parser.getName().equals("locals")) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String name = parser.getName();
				//System.out.println("locals extracting = " + name);
				// Starts by looking for the process
				if (name.equals("who")) {
					String whoValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("who");
					local.addValue(whoValue);
					task.addSubLocal(local);
				} else if (name.equals("what")) {
					String whatValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("what");
					local.addValue(whatValue);
					task.addSubLocal(local);
				} else if (name.equals("when")) {
					String whenValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("when");
					local.addValue(whenValue);
					task.addSubLocal(local);
				} else if (name.equals("where")) {
					String whereValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("where");
					local.addValue(whereValue);
					task.addSubLocal(local);
				}
			}
		}
		return task;
	}

	// For the tags title and summary, extracts their text values.
	private Process readProcessLocals(XmlPullParser parser, Process process) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "locals");
		while (parser.next() != XmlPullParser.END_TAG || !parser.getName().equals("locals")) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String name = parser.getName();
				//System.out.println("locals extracting = " + name);
				// Starts by looking for the process
				if (name.equals("who")) {
					String whoValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("who");
					local.addValue(whoValue);
					process.addSubLocal(local);
				} else if (name.equals("what")) {
					String whatValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("what");
					local.addValue(whatValue);
					process.addSubLocal(local);
				} else if (name.equals("when")) {
					String whenValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("when");
					local.addValue(whenValue);
					process.addSubLocal(local);
				} else if (name.equals("where")) {
					String whereValue = parser.getAttributeValue(null, "name");
					Locals local = new Locals();
					local.setW5h_label("where");
					local.addValue(whereValue);
					process.addSubLocal(local);
				}
			}
		}
		return process;
	}


	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}

}
