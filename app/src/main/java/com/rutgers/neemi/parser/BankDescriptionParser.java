package com.rutgers.neemi.parser;

import android.content.Context;
import android.util.Log;

import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.util.Channel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;

public class BankDescriptionParser {

	Transaction transaction;
	InputStream fis;

	String CHAN_SEP = "(#\\s+-\\s+|\\s+/\\s+)";
	String TYPE_STUB = "^((?<type>\\w+)"+CHAN_SEP+")?" ;
	String DOLLAR_AMOUNT = "(?<amount>-?\\$[-\\d\\.]+)";

	Pattern check_re = Pattern.compile("^SH DRAFT(#(\\s+-\\s+)?\\s*(?<checkno>\\d+)?)?$");
	Pattern atm_re = Pattern.compile(TYPE_STUB + "ATM (?<date>\\d{4} \\d{4}) (?<auth>\\d{6}) (?<description>.+)$");
	Pattern	credit_card_re = Pattern.compile(TYPE_STUB + "(?<date>\\d\\d-\\d\\d-\\d\\d) (?<description>.+) auth# (?<auth>\\d+)$");
	Pattern	pos_re = Pattern.compile(TYPE_STUB + "POS (?<date>\\d{4} \\d{4}) (?<auth>\\d{6}) (?<description>.+)$");
	Pattern	deposit_re = Pattern.compile("^DEPOSIT" + CHAN_SEP + "?\\s*(?<description>.*)$");
	Pattern	dividend_re = Pattern.compile("^(DIVIDEND|Dividend|Savings)(#?$|" + CHAN_SEP + "(?<description>.*)$)");
	Pattern	transfer_re = Pattern.compile("^(TRANSFER|Transfer)($|\\s*(?<acctDescr>.*)" + CHAN_SEP + "(?<description>.+)$)");
	Pattern	fee_end = Pattern.compile(CHAN_SEP + "(?<description>.*?)\\s*" + DOLLAR_AMOUNT + "?$");
	Pattern	fee_re = Pattern.compile("^(?<type>FEE)" + fee_end);
	Pattern	rev_fee_re = Pattern.compile("^(?<type>REV FEE)" + fee_end);
	Pattern	other_re = Pattern.compile(TYPE_STUB + "(?<description>.*)");

	// Sub filters for some transactions
	Pattern phone_end_re = Pattern.compile("^(?<description>.*) (?<phone>[-0-9\\.]{7,15})$");
	Pattern zip_end_re = Pattern.compile("^(?<description>.*) (?<zip>\\d{5})$");
	Pattern clean_phone_re = Pattern.compile("[^\\d]");


	public BankDescriptionParser(Context context) {

		transaction = new Transaction();
		try {
			fis = context.getAssets().open("zips.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	
	public Transaction parser_memo(String memo, Date currentDate) throws IOException {
		this.transaction.setDescription(memo);
	    //checks
		Matcher matcher = check_re.matcher(memo);
		if (matcher.find()) {
			this.transaction.setPayment_method(Channel.CHECK.toString());
			String checkno = matcher.group(3);
			this.transaction.setMerchant_name("CHECK "+ checkno);
			this.transaction.setDescription(memo);
			return this.transaction;
		}
		//POS and ATM transactions
		parse_POS_ATM_transactions(Channel.POS,pos_re,memo,currentDate);
		parse_POS_ATM_transactions(Channel.ATM,atm_re,memo,currentDate);
		
		matcher = credit_card_re.matcher(memo);
		if (matcher.find()) {
			this.transaction.setPayment_method(Channel.POS.toString());
			this.transaction=parse_transaction(matcher.group(5));
			return this.transaction;
		}
		
		
		//transfers
	    matcher = transfer_re.matcher(memo);
	    if (matcher.find()) {
			this.transaction.setPayment_method(Channel.TRANSFER.toString());
			if(matcher.group(3)!=null) {
				this.transaction.setAccount_id(matcher.group(3));
			}
		
	        this.transaction.setDescription(memo);
	        return this.transaction;
	    }

	    //deposits
	    matcher = deposit_re.matcher(memo);
	    if (matcher.find()) {
			this.transaction.setPayment_method(Channel.DEPOSIT.toString());
			this.transaction.setDescription((matcher.group(2)==null ?  matcher.group(1): matcher.group(2)));
	        return this.transaction;
	    }

	    //dividends
	    matcher = dividend_re.matcher(memo);
	    if (matcher.find()) {
			this.transaction.setPayment_method(Channel.DIVIDEND.toString());
			this.transaction.setDescription((matcher.group(4)==null ?  matcher.group(1): matcher.group(4)));
	        return this.transaction;
	    }

	    //fees and rev fees
	   // for regex in (rev_fee_re, fee_re):
        matcher = rev_fee_re.matcher(memo);
        if (matcher.find()) {
            this.transaction.setPayment_method(Channel.FEE.toString());
            if (matcher.group(4)!=null) {
            		this.transaction.setAmount(_bash_amount(matcher.group(4)));
            }
			transaction.setDescription(matcher.group(3));
            return transaction;
        }
		
		//everything else
	    matcher = other_re.matcher(memo);
	    if (matcher.find() && matcher.group(2)!=null) {
	        String type = matcher.group(2).toLowerCase();
	        if (type.contains("fee")){
	        		this.transaction.setPayment_method(Channel.FEE.toString());
				this.transaction.setDescription(matcher.group(4).replaceAll("\\$[-\\d\\.]+", "" ).trim());
	            return this.transaction;

	        }else if (type.contains("withdraw") || type.contains("transfer")){
	        		this.transaction.setPayment_method(Channel.ATM.toString());
	            if (matcher.group(4)!=null)
					transaction.setDescription(matcher.group(4));
	            return this.transaction;
	        }
	    }

	    //fallback
		this.transaction=parse_transaction(memo);
		if (this.transaction.getDescription()==null)
			transaction.setDescription(memo);
		return this.transaction;
	     	
	}

	public float _bash_amount(String amount) {
     /*Sometimes the tx says "$-2.0-50". """*/
		amount = amount.replace("$", "");
	    if (amount.charAt(0) == '-'){
	        return -Float.parseFloat(amount.replace("-", ""));
	    }else {
	        return Float.parseFloat(amount);
		}
	}
	
	public void parse_POS_ATM_transactions(Channel channel_type, Pattern pattern, String memo, Date currentDate) throws IOException {
		
		Matcher matcher = pattern.matcher(memo);
	    if (matcher.find()) {
	    		this.transaction.setPayment_method(channel_type.toString());
            this.transaction = parse_transaction(matcher.group(6));
            
	      
	    }
	
	}
	
	public Transaction parse_transaction(String description) throws IOException {
	    String memo = description.trim();

	    String memo_guess = memo.substring(0, memo.length()-2).trim();
	    String state_guess = memo.substring(memo.length()-2).trim();
	    ZipData zipdata = new ZipData(this.fis);
	    ArrayList<String> cities = zipdata.cities_by_state.get(state_guess);
	   
	    
	    //We have a state match.
	    if (zipdata.cities_by_state.containsKey(state_guess)) {
			this.transaction.getPlace().setState(state_guess);

			//Does the listing end with a phone number?
	        Matcher matcher = phone_end_re.matcher(memo_guess);
	        if(matcher.find()) {
	            this.transaction.setMerchant_name(matcher.group(1));
	            this.transaction.getPlace().setPhone_number(matcher.group(2).replace(clean_phone_re.pattern(), ""));
	            return this.transaction;
	        }

		    //Does the listing end with a zip code?
		     matcher = zip_end_re.matcher(memo_guess);
		    	 if(matcher.find()) {
		    		 this.transaction.setMerchant_name(matcher.group(1));
		    		 this.transaction.getPlace().setZip(matcher.group(2));
		         this.transaction.getPlace().setCity(zipdata.cities_by_zip.get(transaction.getPlace().getZip()).get(0));
		         return this.transaction;
		    	 }
	
		      //Otherwise, try to match city.
			String desc_city = parse_city(cities, memo_guess);
			String[] descr_city = desc_city.split("_split_");
			if (descr_city.length>1) {
				transaction.setMerchant_name(descr_city[0]);
				transaction.getPlace().setCity(descr_city[1]);
			}else{
				transaction.setMerchant_name(descr_city[0]);
			}
			if (transaction.getPlace().getCity()!=null)
				return this.transaction;
	    }

	    //Fall back
	    transaction.setMerchant_name(memo);
	    return this.transaction;
	}
	
	public String parse_city(ArrayList<String> cities, String memo) {
	    
	    /*Split off a (potentially abbreviated) city stub from the end of the
	    memo string.  Assume: 
	    1. The city will come at the end of the memo string.
	    2. The city may contain deletions from the "real" city name, but not
	       insertions -- hence, the represented city name will not be longer
	       than the real city name.
	    3. The city abbreviation will be preceded by a space or the beginning
	       of the string.
	   */
	    String [] words = memo.split(" ");
	    List<String> wordsList = Arrays.asList(words);
	    int best_score = 0;
	    String best_city = null;
	    String remainder = null;
	    for (String city: cities) {
	        ArrayList<String> pot_words=new ArrayList<String>();
	        int run_length = -1; // initial space
	        ListIterator li = wordsList.listIterator(wordsList.size());
	        while(li.hasPrevious()) {        	
	        		String word = (String) li.previous();
	            if(run_length + word.length() + 1 <= city.length()) {
	                pot_words.add(word);
	                run_length += word.length() + 1; //add one for space
	            }else {
	                break;
	            }
	        }
	        StringBuilder sb = new StringBuilder();
			for(String s:pot_words){
				sb.append(s);
				sb.append(" ");
			}
	        String pot_city = sb.toString().trim().toUpperCase();
	        int pot_city_pos = pot_city.length() - 1;
	        int score = 0;
	        int cityLength = city.length();
	        for (int i=cityLength - 1; i>=0; i--) {
	            if (pot_city_pos < 0) {
	                score -= i;
	                break;
	            }if (city.charAt(i)==(pot_city.charAt(pot_city_pos))) {
	                score += 1;
	                pot_city_pos -= 1;
	            }else {
	                score -=1;
	            }
	        }
	        if (score > best_score) {
	            best_score = score;
	            best_city = city;

				sb = new StringBuilder();
				for(String s:words){
					sb.append(s);
					sb.append(" ");
				}
	            remainder = sb.toString().trim();
	            remainder = remainder.substring(0, remainder.length()-pot_city.length()-1);

	        }
	    }
	    if (best_city!=null ){//&& best_score > best_city.length() / 2) {
	        return remainder+"_split_"+best_city;
	    }else {
	        return memo+"_split_"+"";
	    }
	}
}

	
	
	class ZipData {
		
		public HashMap<String, ArrayList<String>> cities_by_state = new HashMap<String, ArrayList<String>> ();
        public HashMap<String, ArrayList<String>> cities_by_zip = new HashMap<String, ArrayList<String>> ();
        
        public ZipData(InputStream is) throws IOException{
        		readData(is);
        }
		
		public void readData(InputStream is) throws IOException {
			try (

					Reader reader = new BufferedReader(new InputStreamReader(is));
					CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
		        ) {
		            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
		            
	
		            for (CSVRecord csvRecord : csvRecords) {
		                // Accessing Values by Column Index
	
		                String zip = csvRecord.get(0);
		                String city = csvRecord.get(1);
		                String state = csvRecord.get(2);
		                
		                if (cities_by_state.containsKey(state)) {
		                		cities_by_state.get(state).add(city);
		                }else {
		                		ArrayList<String> cityList = new ArrayList<String>();
		                		cityList.add(city);
		                		cities_by_state.put(state, cityList);
		                }
		                	if (cities_by_zip.containsKey(zip)) {
		                		cities_by_zip.get(zip).add(city);
		                }else {
			                	ArrayList<String> cityList = new ArrayList<String>();
		                		cityList.add(city);
		                		cities_by_zip.put(zip, cityList);
		                }
		        		}
		        	}  
		}
	}
	

