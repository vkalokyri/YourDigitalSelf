package com.rutgers.neemi.util;

/**
 * Application's global constant variables defined in file.properties
 */
public enum PROPERTIES {
	

	TRIGGERS_FILE("script_triggers"),
	LOCALS_FILE("locals_file"),
	RESTAURANT_SCRIPT_FILE("restaurant_script_file"),
	RESTAURANT_SCRIPT("restaurant_script"),
	TRIP_SCRIPT_FILE("trip_script_file"),
	TRIP_SCRIPT("trip_script"),
	CLUES("clues_file"),
	DB_NAME("database_name"),
	DB_HOSTNAME("db_hostname"),
	DB_PORT("db_port"),
	PARSED_COLLECTION("parsed_collection"),
	RESTAURANT_KEYWORDS_FILE("restaurant_keywords_file"),
	TRIP_KEYWORDS_FILE("trip_keywords_file"),
	START_DATE("start_date"),
	END_DATE("end_date"),
	BANK_WEIGHT("bank_weight"),
	GCAL_WEIGHT("gcal_weight"),
	EMAIL_WEIGHT("email_weight"),
	OPENTABLE_WEIGHT("openTable_weight"),
	FACEBOOK_WEIGHT("facebook_weight"),
	FOURSQUARE_WEIGHT("foursquare_weight"),
	TWITTER_WEIGHT("twitter_weight"),
	GMAPS("google_maps_API"),
	INSTAGRAM_BASE_URL("instagram_base_url"),
	INSTAGRAM_CLIENT_ID("instagram_API"),
	INSTAGRAM_REDIRECT_URI("instagram_redirect_url"),
	PLAID_CLIENT_ID("plaid_client_id"),
	PLAID_SECRET("plaid_secret"),
	File_Source("FileSource"),
	OutputFile("OutputFile"),
	MatcherMerger("MatcherMerger"),
	EmailThreshold("EmailThreshold"),
	NameThreshold("NameThreshold"),
	Classifier("Classifier");


	
	private String strVal;
	private PROPERTIES(final String pstrVal) {
		strVal = pstrVal;
	}
	
	public String toString() {
		return strVal;
	}
}
