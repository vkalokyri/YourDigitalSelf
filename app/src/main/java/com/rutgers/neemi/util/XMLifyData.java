package com.rutgers.neemi.util;

import android.content.Context;
import android.os.Bundle;

import com.rutgers.neemi.model.KeyValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class XMLifyData {

	public BufferedWriter fw;
	String xmlOutputFile;
	
	final static String RECORD_SET_OPEN = "<recordSet>\n";
	final static String RECORD_SET_CLOSED = "</recordSet>";
	
	public XMLifyData(String xmlOutputFile, Context context)	throws IOException
	{
		this.xmlOutputFile = xmlOutputFile;
		parseBegin(xmlOutputFile, context);


	}


	protected void parseBegin(String xmlOutputFile, Context context)	throws IOException{

        try {
            File file = context.getFileStreamPath(xmlOutputFile);
			FileOutputStream fos2 = context.openFileOutput(xmlOutputFile, Context.MODE_PRIVATE);
			fw  = new BufferedWriter(new OutputStreamWriter(fos2,"UTF-8"));//new FileWriter(xmlOutputFile);
			openRecordSet(fw);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	public void parseEnd() throws IOException{
		closeRecordSet(fw);
		fw.close();
	}
	
	public static void openRecordSet(Writer fw) throws IOException{
		fw.write(RECORD_SET_OPEN);
	}
	
	public static void closeRecordSet(Writer fw) throws IOException{
		fw.write(RECORD_SET_CLOSED);
	}
	
	public static void serializeRecord(ArrayList<ArrayList<KeyValuePair>> objects, Writer fw)	throws IOException
	{
		final String RECORD_OPEN = "<record>\n";
		final String RECORD_CLOSE = "</record>\n";
		final String ATTR_CLOSE = "</attribute>\n";
		final String NAME_OPEN = "<name>";
		final String NAME_CLOSE = "</name>\n";
		final String VALUE_OPEN = "<value>";
		final String VALUE_CLOSE = "</value>\n";
		
		for (ArrayList<KeyValuePair> listOfKeysValues: objects){
			fw.write(RECORD_OPEN);
			ArrayList<String> listOfNames = new ArrayList<String>();
			ArrayList<String> listOfEmails = new ArrayList<String>();
			//boolean multipleNames=false;
			for (KeyValuePair keyValue : listOfKeysValues){
				//fw.write("\t"+ATTR_OPEN);
				String key = keyValue.getKey();
				String value = keyValue.getValue();
				//Omar: Made the name an XML attribute
				if (key.equals("name")){
					listOfNames.add(value);
				}else{
					fw.write("\t" + "<attribute name=\"" + key + "\">\n");
					fw.write("\t\t"+VALUE_OPEN+escape(value) + VALUE_CLOSE);
					fw.write("\t"+ATTR_CLOSE);
				}
				
			}
			if (!listOfNames.isEmpty()){
				fw.write("\t" + "<attribute name=\"" + "name" + "\">\n");
				for (String listName: listOfNames){
					//fw.write("\t\t"+NAME_OPEN + key + NAME_CLOSE);
					fw.write("\t\t"+VALUE_OPEN+escape(listName) + VALUE_CLOSE);
				}
				fw.write("\t"+ATTR_CLOSE);
			}
			fw.write(RECORD_CLOSE);
			fw.flush();
		}
		
	}
		
		
	
	static String escape(String in)	{
		in = in.replaceAll("& ", "&amp; ");
		in = in.replaceAll("&", "&amp; ");
		in = in.replaceAll("\"", "&quot;");
		in = in.replaceAll("'", "&apos;");
		in = in.replaceAll("<", "");
		in = in.replaceAll(">", "");

		return in;
	}
	
}
