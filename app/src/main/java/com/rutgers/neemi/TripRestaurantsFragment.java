package com.rutgers.neemi;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptLocalValues;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TripRestaurantsFragment extends Fragment {


    private static final String TAG = "RestaurantsFragment";
    public static View myView;
    DatabaseHelper helper;
    static ArrayList<Script> listOfScripts = new ArrayList<Script>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    Integer[] imgid={
            R.drawable.restaurant
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        listOfScripts = (ArrayList<Script>) this.getArguments().get("listOfScripts");



//        ConfigReader config = new ConfigReader(getContext());
//        helper=DatabaseHelper.getHelper(getActivity());
//        PersonParser personParser = new PersonParser(helper);
//        ArrayList<ArrayList<KeyValuePair>> keyValues = personParser.parse();
//
//        XMLifyData xmlData = null;
//        try {
//            xmlData = new XMLifyData("people.xml", getContext());
//            XMLifyData.serializeRecord(keyValues, xmlData.fw);
//            xmlData.parseEnd();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            ER er = new ER(getContext());
//        } catch (TestException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//
//        /*get the keywords to search in the documents*/
//        Class matcherMerger = null;
//
//        try {
//            matcherMerger = matcherMerger = Class.forName(getContext().getAssets().open(config.getStr(PROPERTIES.MatcherMerger));
//            if (matcherMerger == null) {
//                throw new Exception("No MatcherMerger Class specified!");
//            } else if (!serf.ER.checkMatcherMergerInterface(matcherMerger)) {
//                throw new Exception("Given MatcherMerger class does not implement SimpleMatcherMerger interface!");
//            } else {
//                er.runRSwoosh();
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            FileInputStream fis = getActivity().openFileInput(config.getStr(PROPERTIES.OutputFile));
//            XmlPullParser parser = Xml.newPullParser();
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
//            parser.setInput(fis, null);
//            parser.nextTag();
//            parser.nextTag();
//
//            parser.require(XmlPullParser.START_TAG, null, parser.getName());
//            int eventType = parser.getEventType();
//            while(eventType!=XmlPullParser.END_DOCUMENT ) {//|| !parser.getName().equals("definitions")){
//                //System.out.println("XMLPARSING = "+name);
//                // Starts by looking for the process
//                if (eventType != XmlPullParser.END_TAG) {
//                    parseRecord(parser);
//                    parser.nextTag();
//
//                }
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (XmlPullParserException e) {
//            e.printStackTrace();
//        }


         //findScriptInstances("restaurant");

        CustomListAdapter adapter=new CustomListAdapter(getActivity(), listOfScripts, imgid);
        ListView list= myView.findViewById(R.id.restaurant_list);
        list.setAdapter(adapter);


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        myView = inflater.inflate(R.layout.fragment_restaurants, container, false);
        ListView list1 =  (ListView) myView.findViewById(R.id.restaurant_list);
        myView.setBackgroundColor(getResources().getColor(android.R.color.white));


//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = prefs.edit();
//        TinyDB tinydb = new TinyDB(getContext());
//        editor.putBoolean("restaurantFirstTime", false);
//
//        if (!prefs.getBoolean("restaurantFirstTime", false)) {
//            // <---- run your one time code here
//            findScriptInstances();
//
//            ArrayList<Object> scriptObjects = new ArrayList<Object>();
//
//            for(Script s : listOfScripts){
//                scriptObjects.add((Object)s);
//                break;
//            }
//
//            tinydb.putListObject("restaurantScripts", scriptObjects);
//
////            // mark first time has runned.
//            editor.putBoolean("restaurantFirstTime", true);
////
////            Gson gson = new Gson();
////            ArrayList<String> objStrings = new ArrayList<String>();
////            for(Script obj : listOfScripts){
////                objStrings.add(gson.toJson(obj));
////            }
////            String[] myStringList = objStrings.toArray(new String[objStrings.size()]);
////            editor.putString("restaurantScripts", TextUtils.join("‚‗‚", myStringList)).apply();
////            editor.commit();
//        }else{
//            ArrayList<Object> scriptObjects = tinydb.getListObject("restaurantScripts", Script.class);
//
//            for(Object objs : scriptObjects){
//                listOfScripts.add((Script) objs);
//            }
//        }



        list1.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        ScriptFragment scriptFragment = new ScriptFragment();
                        Bundle arguments = new Bundle();
                        arguments.putSerializable("processes", listOfScripts);
                        arguments.putSerializable("position",position);
                        arguments.putSerializable("id",id);

                        scriptFragment.setArguments(arguments);

                        android.support.v4.app.FragmentTransaction scriptfragmentTrans = getFragmentManager().beginTransaction();
                        scriptfragmentTrans.add(R.id.frame,scriptFragment);
                        scriptfragmentTrans.addToBackStack(null);
                        scriptfragmentTrans.commit();
                        Toast.makeText(getContext(), "Pressed!", Toast.LENGTH_LONG).show();
                    }
                }
        );

        return myView;
    }


    public class CustomListAdapter extends ArrayAdapter<Script> {

        private final Activity context;
        private final List<Script> itemname;
        private final Integer[] imgid;

        public CustomListAdapter(Activity context, List<Script> scripts, Integer[] imgid) {
            super(context, R.layout.restaurantsview, scripts);
            // TODO Auto-generated constructor stub
            this.context=context;
            this.itemname=scripts;
            this.imgid=imgid;
        }

        public View getView(int position,View view,ViewGroup parent) {


            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.restaurantsview, null,false);
            LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayout);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            Place place = null;


//            for (Task task: itemname.get(position).getTasks()) {
//                if (task.getPid() instanceof Transaction) {
//                    place = ((Transaction) task.getPid()).getPlace();
//                    break;
//                } else if (task.getPid() instanceof Photo) {
//                    place = ((Photo) task.getPid()).getPlace();
//                    break;
//                } else if (task.getPid() instanceof Feed) {
//                    place = ((Feed) task.getPid()).getPlace();
//                    break;
//                }
//            }
//            if (place != null) {
//                byte[] image = place.getImage();
//                if (image != null) {
//                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
//                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 40, 40, false));
//                }
//            }else{
                imageView.setImageResource(imgid[0]);

 //           }



            Script script = itemname.get(position);//.getScriptDefinition();
            HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
                    //txtTitle.setText(itemname.get(position).getScore()+", "+String.valueOf(((Email)processTask.getPid()).get_id()));


            ArrayList<ScriptLocalValues> localValues = script.getLocalValues();
            if (localValues != null) {
                for (ScriptLocalValues localValue : localValues) {
                    if (localValue != null) {
                        LocalProperties lp = localValue.getLocalProperties();
                        if (lp != null) {
                            String w5h_value = lp.getW5h_value();
                            if (map.containsKey(w5h_value)) {
                                ArrayList<String> values = map.get(w5h_value);
                                values.add(localValue.getLocal_value());
                                map.put(w5h_value, values);
                            } else {
                                if (w5h_value != null) {
                                    ArrayList<String> values = new ArrayList<String>();
                                    values.add(localValue.getLocal_value());
                                    map.put(w5h_value, values);
                                }
                            }
                        }
                    }
                }
            }

            for (String localLabel:map.keySet()) {
                StringBuilder sb = new StringBuilder();
                    for (String localValue : map.get(localLabel)) {
                        sb.append(localValue);
                        sb.append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length() - 1);
               // }

                LinearLayout textLayout = new LinearLayout(context);
                textLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(10, 5, 5, 0);
                textLayout.setLayoutParams(llp);

                TextView localTextView = new TextView(this.getContext());
                localTextView.setTextColor(Color.parseColor("#99CCFF"));
                localTextView.setText(getString(R.string.local, localLabel + " : "));

                TextView localValueTextView = new TextView(this.getContext());
                localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
                localValueTextView.setText(getString(R.string.local, sb.toString()));

                textLayout.addView(localTextView);
                textLayout.addView(localValueTextView);
                linearLayout.addView(textLayout);

            }

            return rowView;
        };
    }


}


