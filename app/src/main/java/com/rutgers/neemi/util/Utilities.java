package com.rutgers.neemi.util;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.Task;

public class Utilities {

    static Context context;
    static DatabaseHelper helper;

    GeoApiContext gmapsContext = new GeoApiContext.Builder()
            .apiKey("AIzaSyDe8nWbXFA6ESFS6GnQtYPPsXzYmLz3Lf0")
            .build();

    private static Utilities util;



    public static synchronized Utilities getUtilities(Context context)
    {
        if (util == null) {
            helper = DatabaseHelper.getHelper(context);
            util = new Utilities(context);

        }
        return util;
    }

    public Utilities(Context context) {
        this.context=context;
        helper = DatabaseHelper.getHelper(context);
    }


    public ArrayList<Place> findTransactionPlaces(PlacesSearchResponse gmapsResponse) throws InterruptedException, ApiException, IOException {
        ArrayList<Place> listOfPlaces = new ArrayList();
        if (gmapsResponse.results != null) {
            for  (PlacesSearchResult place: gmapsResponse.results) {
                Place placeExists = helper.placeExistsById(place.placeId);
                if (placeExists == null) {
                    placeExists = new Place();
                    placeExists.setName(place.name);
                    placeExists.setStreet(place.formattedAddress);
                    placeExists.setId(place.placeId);
                    placeExists.setLatitude(place.geometry.location.lat);
                    placeExists.setLongitude(place.geometry.location.lng);
                    if (place.photos != null) {
                        PhotoResult photoResult = PlacesApi.photo(gmapsContext, place.photos[0].photoReference).maxWidth(400)
                                .await();
                        byte[] image = photoResult.imageData;
                        placeExists.setImage(image);
                    }
                    helper.getPlaceDao().create(placeExists);
                    for (String placeCategory : place.types) {
                        Category categoryExists = helper.placeCategoryExists(placeCategory);
                        if (categoryExists == null) {
                            Category newCategory = new Category();
                            newCategory.setCategoryName(placeCategory);
                            helper.getCategoryDao().create(newCategory);
                            PlaceHasCategory placeHasCategories = new PlaceHasCategory(placeExists, newCategory);
                            helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                        } else {
                            PlaceHasCategory trans_categories = new PlaceHasCategory(placeExists, categoryExists);
                            helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                        }
                    }
                    listOfPlaces.add(placeExists);
                }else{
                    listOfPlaces.add(placeExists);
                }
            }
        }
        return listOfPlaces;
    }

	
//	public void saveInExcel(List<Process> listOfProcesses){
//		//Blank workbook
//        XSSFWorkbook workbook = new XSSFWorkbook();
//
//        //Create a blank sheet
//        XSSFSheet sheet = workbook.createSheet(ConfigReader.getInstance().getStr(PROPERTIES.DB_NAME));
//
//        //This data needs to be written (Object[])
////        Map<String, Object[]> data = new TreeMap<String, Object[]>();
////        data.put("1", new Object[] {"ID", "NAME", "LASTNAME"});
////        data.put("2", new Object[] {1, "Amit", "Shukla"});
////        data.put("3", new Object[] {2, "Lokesh", "Gupta"});
////        data.put("4", new Object[] {3, "John", "Adwards"});
////        data.put("5", new Object[] {4, "Brian", "Schultz"});
//
//
//
//        int rownum = 0;
//        for (Process process:listOfProcesses){
//            Row row = sheet.createRow(rownum++);
//    		int cellnum = 0;
//    		Cell cell = row.createCell(cellnum++);
//    		cell.setCellValue("Score");
//        	for (Task task:process.getTasks()){
//        		for (LocalProperties sublocals:task.getLocals()){
//        			cell = row.createCell(cellnum++);
//            		cell.setCellValue(sublocals.getW5h_label());
//        		}
//	    		row = sheet.createRow(rownum++);
//	    		cellnum = 0;
//	    		cell = row.createCell(cellnum++);
//	    		cell.setCellValue(process.getScore());
//        		for (LocalProperties sublocals:task.getLocals()){
//        			if (sublocals.getValue()!=null){
//	        			cell = row.createCell(cellnum++);
//	            		cell.setCellValue(sublocals.getValue().toString());
//        			}else{
//        				cellnum++;
//        			}
//        		}
//	    		//Document pid = task.getPid();
//	    		//cell = row.createCell(cellnum++);
//	    		//cell.setCellValue(((Document)pid.get("data")).toJson());
//        	}
//        }
//        try
//        {
//            //Write the workbook in file system
//            FileOutputStream out = new FileOutputStream(new File(ConfigReader.getInstance().getStr(PROPERTIES.SCRIPT)+".xlsx"));
//            workbook.write(out);
//            out.close();
//            System.out.println("Written successfully on disk.");
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }


	
//	public List<List<Task>> getEmailThreads(List<Task> tasks){
//		List<List<String>> threads=new ArrayList<List<String>>();
//		List<List<Task>> mergedScripts = new ArrayList<List<Task>>();
//		HashMap<String,Task> map= new HashMap<String,Task>();
//
//		for (Task task:tasks){
//			List<Task> newInstance = new ArrayList<Task>();
//			if (task.getPid() instanceof Email){
//				List<String> messageIds=new ArrayList<String>();
//				boolean found_related_emails=false;
//				Email data = (Email) task.getPid();
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





}
