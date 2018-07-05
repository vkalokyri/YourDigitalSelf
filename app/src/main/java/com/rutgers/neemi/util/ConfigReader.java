package com.rutgers.neemi.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import com.rutgers.neemi.util.PROPERTIES;

/**
 * Singleton class which provides cross-app access to app.properties configuration variables.
 */
public class ConfigReader {
	
	private final static String propsFile = "file.properties";
	private static Properties properties;
	private volatile static ConfigReader instance;
	
	public ConfigReader(Context context) {
		properties = new Properties();
		AssetManager assetManager = context.getAssets();
		try {
			properties.load(assetManager.open(ConfigReader.propsFile));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FATAL ERROR: Initializing project's properties");
		}
	}

	public Properties getProperties() {
		return properties;
	}

	private ConfigReader() {

	}


	/**
	 * Returns a singleton instance of the ConfigReader.
	 * @return
	 */
	public synchronized static ConfigReader getInstance() {
		if(ConfigReader.instance == null) {
			ConfigReader.instance = new ConfigReader();
		}
		return ConfigReader.instance;
	}
	
	/**
	 * Retrieves a String value of a property.
	 * @return String value of property
	 */
	public String getStr(final PROPERTIES penmProp) {
		return properties.getProperty(penmProp.toString());
	}
	
	public boolean getBool(final PROPERTIES penmProp) {
		String strVal = properties.getProperty(penmProp.toString());
		return strVal.equalsIgnoreCase("true") || strVal.equals("1");
	}
	
	
}
