package com.rutgers.neemi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Locals;
import com.rutgers.neemi.model.Process;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.parser.InitiateScript;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Utilities {

	
	public void saveInExcel(List<Process> listOfProcesses){
		//Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
         
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(ConfigReader.getInstance().getStr(PROPERTIES.DB_NAME));
          
        //This data needs to be written (Object[])
//        Map<String, Object[]> data = new TreeMap<String, Object[]>();
//        data.put("1", new Object[] {"ID", "NAME", "LASTNAME"});
//        data.put("2", new Object[] {1, "Amit", "Shukla"});
//        data.put("3", new Object[] {2, "Lokesh", "Gupta"});
//        data.put("4", new Object[] {3, "John", "Adwards"});
//        data.put("5", new Object[] {4, "Brian", "Schultz"});
        
        
        
        int rownum = 0;
        for (Process process:listOfProcesses){
            Row row = sheet.createRow(rownum++);
    		int cellnum = 0;
    		Cell cell = row.createCell(cellnum++);
    		cell.setCellValue("Score");
        	for (Task task:process.getTasks()){
        		for (Locals sublocals:task.getLocals()){
        			cell = row.createCell(cellnum++);
            		cell.setCellValue(sublocals.getW5h_label());
        		}
	    		row = sheet.createRow(rownum++);
	    		cellnum = 0;
	    		cell = row.createCell(cellnum++);
	    		cell.setCellValue(process.getScore());
        		for (Locals sublocals:task.getLocals()){
        			if (sublocals.getValue()!=null){
	        			cell = row.createCell(cellnum++);
	            		cell.setCellValue(sublocals.getValue().toString());
        			}else{
        				cellnum++;
        			}
        		}
	    		//Document pid = task.getPid();
	    		//cell = row.createCell(cellnum++);
	    		//cell.setCellValue(((Document)pid.get("data")).toJson());
        	}
        }
        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File(ConfigReader.getInstance().getStr(PROPERTIES.SCRIPT)+".xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("Written successfully on disk.");
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }


	
//	public List<List<Document>> getEmailThreads(LinkedList<Document> scripts){
//		List<List<String>> threads=new ArrayList<List<String>>();
//		List<List<Document>> mergedScripts = new ArrayList<List<Document>>();
//		HashMap<String,Document> map= new HashMap<String,Document>();
//
//		for (Document doc:scripts){
//			String source = (String) doc.get("source");
//			List<Document> newInstance = new ArrayList<Document>();
//			if (source.equals("gmail")){
//				List<String> messageIds=new ArrayList<String>();
//				boolean found_related_emails=false;
//				Document data = (Document) doc.get("data");
//		    	ArrayList whatArray = (ArrayList) data.get("what");
//		    	for (int i=0;i<whatArray.size();i++){
//		    		Document what = (Document)whatArray.get(i);
//		    		//getRelatedEmails
//		    		if (((String)what.get("key")).equals("related_email")){
//		    			found_related_emails=true;
//		    			String [] relatedEmails = ((String) ((ArrayList)what.get("value")).get(0)).split(" ");
//		    			for (String relatedEmail:relatedEmails){
//			    			messageIds.add(relatedEmail);
//		    			}
//
//		    			messageIds.add((String) doc.get("feed_id"));
//		    			map.put((String) doc.get("feed_id"), doc);
//		    		}
//		    	}
//				if (!found_related_emails){
//					messageIds.add((String) doc.get("feed_id"));
//					map.put((String) doc.get("feed_id"), doc);
//				}
//				threads.add(messageIds);
//			}else{
//				newInstance.add(doc);
//				mergedScripts.add(newInstance);
//			}
//		}
//
//		threads=removeSubsets(threads);
//		for (List<String> feedIds:threads){
//			List<Document> newInstance = new ArrayList<Document>();
//			for (String feedId:feedIds){
//				if (map.get(feedId)!=null){
//					newInstance.add(map.get(feedId));
//				}
//			}
//			mergedScripts.add(newInstance);
//		}
//
//
//		return mergedScripts;
//
//
//	}
//
//	public List<List<String>> removeSubsets(List<List<String>> listOfAll) {
//
//		List<List<String>> valuesToRemove=new ArrayList<List<String>>();
//
//		for (List<String> list:listOfAll){
//			for (List<String> sublist:listOfAll){
//				if (removeSubsets(list,sublist)){
//					valuesToRemove.add(sublist);
//				}
//			}
//		}
//		listOfAll.removeAll(valuesToRemove);
//
//		return listOfAll;
//
//	}
//
////	public static void main(String[] args) {
////		List<String> list=new ArrayList<String>(Arrays.asList("valia","eirini","maria","kath"));
////		List<List<String>> all = new ArrayList<List<String>>();
////		List<String> sublist=new ArrayList<String>(Arrays.asList("maria","valia"));
////		List<List<String>> valuesToRemove=new ArrayList<List<String>>();
////
////		all.add(list);
////		all.add(sublist);
////		for (List<String> thread1:all){
////			System.out.println(thread1);
////			for (List<String> thread2:all){
////				if (removeSubsets(thread1,thread2)){
////					System.out.println("Remove"+thread2);
////					valuesToRemove.add(thread2);
////				}
////			}
////		}
////
////
////	}
//
//
//	public boolean removeSubsets(List<String> list, List<String> sublist) {
//		return list.containsAll(sublist) && list.size()!=sublist.size();
//	  }
//
//
//	public void extractLocals(List<List<Document>> scripts){
//		for (List<Document> script:scripts){
//			for (Document pid:script){
//
//			}
//		}
//	}
//
	public Process assignScore(Process process){
		List<Task> tasks = process.getTasks();
		float instanceScore = process.getScore();
		for (Task task:tasks){
			Object pid = task.getPid();
			String source = pid.getClass().toString();
			float addedScore=0;
			if (source.equals("Payment") ){
				addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.BANK_WEIGHT));
			}else if (source.equals("EMAIL")){
				addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.EMAIL_WEIGHT));
				String from =((Email)pid).getFrom();
				if(((String)from).contains("member_services@opentable.com")){
					addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.OPENTABLE_WEIGHT));
				}
		    			//else if(((String)who.get("value")).contains("calendar-notification@google.com")){
		    			//	addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.GCAL_WEIGHT));
		    			//}


			}else if (source.equals("gcal")){
				addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.GCAL_WEIGHT));
			}else if (source.equals("facebook")){
				addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.FACEBOOK_WEIGHT));
			}else if (source.equals("foursquare")){
				addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.FOURSQUARE_WEIGHT));
			}else if (source.equals("twitter")){
				addedScore = Float.parseFloat(InitiateScript.config.getStr(PROPERTIES.TWITTER_WEIGHT));
			}
			float newScore = scoreFunction(instanceScore, addedScore);
			instanceScore=newScore;
			process.setScore(newScore);
		}
		return process;

	}
	
	public float scoreFunction(float previousScore,float addedScore){
		return (1-((1-previousScore)*(1-addedScore)));
	}
	
}
