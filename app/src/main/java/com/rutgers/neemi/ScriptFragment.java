package com.rutgers.neemi;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rutgers.neemi.interfaces.NLevelView;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Message;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.StayPoint;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.model.TaskLocalValues;
import com.rutgers.neemi.model.Transaction;

import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import static com.facebook.FacebookSdk.getApplicationContext;


public class ScriptFragment extends Fragment{

    List<NLevelItem> list;
    ListView listView;
    View view;
    Integer[] imgid={
            R.drawable.restaurant,
            R.drawable.trips
    };

    //private GoogleMap mMap;

    //SupportMapFragment mapFragment;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scriptview, container, false);


        ArrayList listOfProcesses = (ArrayList) getArguments().getSerializable("processes");
        int position = (int) getArguments().getSerializable("position");
        long id  = (long) getArguments().getSerializable("id");




        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);
        // TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
//
//        mapFragment = (SupportMapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.scriptmap);
//        mapFragment.getMapAsync(this);


        Script script = (Script)listOfProcesses.get(position);//.getScriptDefinition();
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

        Place place = null;

        for (Task task: script.getTasks()) {
            if (task.getPid() instanceof Transaction) {
                try {
                    ArrayList<Place> places = DatabaseHelper.getHelper(getApplicationContext()).getOfficialNameOfTranscationPlace(((Transaction) task.getPid()).get_id());
                    if (places!=null){
                        place = DatabaseHelper.getHelper(getApplicationContext()).getPlace(places.get(0).get_id());
                        //place=places.get(0);
                        break;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else if (task.getPid() instanceof Photo) {
                place = ((Photo) task.getPid()).getPlace();
                break;
            } else if (task.getPid() instanceof Feed) {
                place = ((Feed) task.getPid()).getPlace();
                break;
            }
        }

        if (place != null) {
            byte[] image = place.getImage();
            if (image != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                imageView.setImageBitmap(bmp);//Bitmap.createScaledBitmap(bmp, , 40, false));
            }
        }else{
            if(script.getScriptDefinition().getName().equals("goingForATrip")) {
                imageView.setImageResource(imgid[1]);
            }else{
                imageView.setImageResource(imgid[0]);
            }

        }

        //imageView.setImageResource(imgid[0]);
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

            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(10, 5, 5, 0);
            textLayout.setLayoutParams(llp);

            TextView localTextView = new TextView(this.getContext());
            localTextView.setTextColor(Color.parseColor("#996666"));
            localTextView.setText(getString(R.string.local, localLabel + " : "));

            TextView localValueTextView = new TextView(this.getContext());
            localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
            localValueTextView.setText(getString(R.string.local, sb.toString()));

            textLayout.addView(localTextView);
            textLayout.addView(localValueTextView);
            linearLayout.addView(textLayout);

        }


        listView = (ListView) view.findViewById(R.id.lvExp);
        list = new ArrayList<NLevelItem>();

        for (final Script subscript: script.getSubscripts()) {

            final NLevelItem grandParent = new NLevelItem(subscript,null, new NLevelView() {

                @Override
                public View getView(NLevelItem item) {

                    return getScriptView(inflater, subscript); //makeAttendEatingOut
                }
            });
            list.add(grandParent);

            int numChildrenTask = subscript.getTasks().size();
            for (int k = 0; k < numChildrenTask; k++) {
                final Task task = subscript.getTasks().get(k);
                NLevelItem child = new NLevelItem(task,grandParent, new NLevelView() {

                    @Override
                    public View getView(NLevelItem item) {
                        return getTaskView(inflater,task, savedInstanceState); //PostOnFacebook
                    }
                });

                list.add(child);

            }


            int numChildren = subscript.getSubscripts().size();
            for (int j = 0; j < numChildren; j++) {
                final Script subsubscript = subscript.getSubscripts().get(j);
                NLevelItem parent = new NLevelItem(subsubscript,grandParent, new NLevelView() {

                    @Override
                    public View getView(NLevelItem item) {
                        return getScriptView(inflater, subsubscript); //makeAPayment
                    }
                });

                list.add(parent);

                numChildrenTask = subsubscript.getTasks().size();
                for (int k = 0; k < numChildrenTask; k++) {
                    final Task task = subsubscript.getTasks().get(k);
                    NLevelItem child = new NLevelItem(task,parent, new NLevelView() {

                        @Override
                        public View getView(NLevelItem item) {
                            return getTaskView(inflater,task, savedInstanceState); //payByCredit
                        }
                    });

                    list.add(child);

                }


            }


        }

        NLevelAdapter adapter = new NLevelAdapter(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                ((NLevelAdapter)listView.getAdapter()).toggle(arg2);
                ((NLevelAdapter)listView.getAdapter()).getFilter().filter();

            }
        });


        return view;
    }


    public View getScriptView(LayoutInflater inflater, Script subscript) {

        StringBuilder localsSb = new StringBuilder();
        HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

        for (ScriptLocalValues lv : subscript.getLocalValues()){
            String key=lv.getLocalProperties().getW5h_value();
            if(map.containsKey(key)){
                map.get(key).add(lv.getLocal_value());
            }else{
                HashSet<String> valueSet = new HashSet<String>();
                valueSet.add(lv.getLocal_value());
                map.put(key,valueSet);
            }
        }

        for(String w5hValue: map.keySet()){
            localsSb.append(w5hValue);
            localsSb.append(": ");
            for (String value: map.get(w5hValue)) {
                localsSb.append(value);
                localsSb.append(",");
            }
            localsSb.deleteCharAt(localsSb.length()-1);
            localsSb.append("\n");
        }



        View view = inflater.inflate(R.layout.list_group, null);
        view.setBackgroundColor(Color.parseColor("#999999"));
        // TextView tv = (TextView) view.findViewById(R.id.item);

        TextView lblListHeader = (TextView) view.findViewById(R.id.lblListHeader);
        lblListHeader.setTextColor(Color.parseColor("#333333"));
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(subscript.getScriptDefinition().getName());

        TextView lblListHeaderLocals = (TextView) view.findViewById(R.id.lblListHeaderLocals);
        lblListHeaderLocals.setTextColor(Color.parseColor("#333333"));
        lblListHeaderLocals.setTypeface(null, Typeface.NORMAL);
        lblListHeaderLocals.setText(localsSb.toString());

        return view;

    }


    public View getTaskView(LayoutInflater inflater, Task childTask, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.list_item, null);



        view.setBackgroundColor(Color.parseColor("#999999"));

        TextView txtListChild = (TextView) view.findViewById(R.id.lblListItem);
        txtListChild.setText(childTask.getName());
        LinearLayout list_item_layout = (LinearLayout)view.findViewById(R.id.list_item_layout);
        TextView txtListHeader = (TextView) view.findViewById(R.id.relItemHeader);
        TextView txtListHeaderBody = (TextView) view.findViewById(R.id.relItem);
        final ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if(childTask.getPid() instanceof Email){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.gmail_icon));
            StringBuilder sb = new StringBuilder();
            for (Person p: ((Email) childTask.getPid()).getTo()){
                if(p!=null) {
                    if (p.getName()!=null && !p.getName().isEmpty()) {
                        sb.append(p.getName());
                        sb.append(", ");
                    } else if (p.getEmail()!=null && !p.getEmail().isEmpty()){
                        sb.append(p.getEmail());
                        sb.append(", ");
                    }
                }
            }
            for (Person p: ((Email) childTask.getPid()).getCc()){
                if(p!=null) {
                    if (p.getName()!=null && !p.getName().isEmpty()) {
                        sb.append(p.getName());
                        sb.append(", ");
                    } else if (p.getEmail()!=null && !p.getEmail().isEmpty()){
                        sb.append(p.getEmail());
                        sb.append(", ");
                    }
                }
            }
            for (Person p: ((Email) childTask.getPid()).getBcc()){
                if(p!=null) {
                    if (p.getName()!=null && !p.getName().isEmpty()) {
                        sb.append(p.getName());
                        sb.append(", ");
                    } else if (p.getEmail()!=null && !p.getEmail().isEmpty()){
                        sb.append(p.getEmail());
                        sb.append(", ");
                    }
                }
            }

            if(sb.length()>0) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            txtListHeader.setText(((Email) childTask.getPid()).getSubject());
            StringBuilder text = new StringBuilder();
            for(TaskLocalValues taskLocalValues : childTask.getLocalValues()){
                text.append(taskLocalValues.getLocalProperties().getW5h_value());
                text.append(": ");
                text.append(taskLocalValues.getLocal_value());
                text.append("\n");
            }
            //String text = "whoSent: "+((Email) childTask.getPid()).getFrom().getName()+" \n whoReceived: "+ sb.toString()+" \n whenSent: "+ ((Email) childTask.getPid()).getDate();
            txtListHeaderBody.setText(text);
        }else if(childTask.getPid() instanceof Message){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.messenger));
            StringBuilder sb = new StringBuilder();
            for (Person p: ((Message) childTask.getPid()).getTo()){
                if(p!=null) {
                    if (p.getName()!=null && !p.getName().isEmpty()) {
                        sb.append(p.getName());
                        sb.append(", ");
                    } else if (p.getEmail()!=null && !p.getEmail().isEmpty()){
                        sb.append(p.getEmail());
                        sb.append(", ");
                    }
                }
            }

            if(sb.length()>0) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            txtListHeader.setText(((Message) childTask.getPid()).getContent());
            StringBuilder text = new StringBuilder();
            for(TaskLocalValues taskLocalValues : childTask.getLocalValues()){
                text.append(taskLocalValues.getLocalProperties().getW5h_value());
                text.append(": ");
                text.append(taskLocalValues.getLocal_value());
                text.append("\n");
            }
            //String text = "whoSent: "+((Email) childTask.getPid()).getFrom().getName()+" \n whoReceived: "+ sb.toString()+" \n whenSent: "+ ((Email) childTask.getPid()).getDate();
            txtListHeaderBody.setText(text);
        }else if(childTask.getPid() instanceof Transaction){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.bank));
            txtListHeader.setText(((Transaction) childTask.getPid()).getMerchant_name());
            Date extractedDate = new Date(((Transaction) childTask.getPid()).getDate());
            Format format = new SimpleDateFormat("yyyy-MM-dd");
            String parsedDate = format.format(extractedDate);
           // String text = "whenPaid: "+parsedDate+" \n howMuchWasPaid: $"+((Transaction) childTask.getPid()).getAmount() ;
            StringBuilder text = new StringBuilder();
            for(TaskLocalValues taskLocalValues : childTask.getLocalValues()){
                if (taskLocalValues.getLocalProperties().getW5h_label().equalsIgnoreCase("when")){
                    Date date = new Date(Long.parseLong(taskLocalValues.getLocal_value()));
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    text.append(taskLocalValues.getLocalProperties().getW5h_value());
                    text.append(": ");
                    text.append(sf.format(date));
                    text.append("\n");
                }else{
                    text.append(taskLocalValues.getLocalProperties().getW5h_value());
                    text.append(": ");
                    text.append(taskLocalValues.getLocal_value());
                    text.append("\n");
                }
            }
            txtListHeaderBody.setText(text);
        }else if(childTask.getList_of_pids().size()>0){
            if(childTask.getList_of_pids().get(0) instanceof Place) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.gmaps));
                txtListHeader.setText(((Place) childTask.getList_of_pids().get(0)).getName());
                txtListHeaderBody.setText(((Place) childTask.getList_of_pids().get(0)).getStreet());
                StringBuilder text = new StringBuilder();
                for (TaskLocalValues taskLocalValues : childTask.getLocalValues()) {
                    if (taskLocalValues.getLocalProperties().getW5h_label().equalsIgnoreCase("when")) {
                        Date date = new Date(taskLocalValues.getLocal_value());
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                        text.append(taskLocalValues.getLocalProperties().getW5h_value());
                        text.append(": ");
                        text.append(sf.format(date));
                        text.append("\n");
                    } else {
                        text.append(taskLocalValues.getLocalProperties().getW5h_value());
                        text.append(": ");
                        text.append(taskLocalValues.getLocal_value());
                        text.append("\n");
                    }
                }

                LinearLayout horizontalButtons = new LinearLayout(getApplicationContext());
                horizontalButtons.setOrientation(LinearLayout.HORIZONTAL);

                Button yesButton = new Button(getApplicationContext());
                yesButton.setText("Yes");
                horizontalButtons.addView(yesButton);

                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            DatabaseHelper.getHelper(getApplicationContext()).confirmPlace(((Place) childTask.getList_of_pids().get(0)).get_id());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });


                Button noButton = new Button(getApplicationContext());
                noButton.setText("No");
                horizontalButtons.addView(noButton);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            DatabaseHelper.getHelper(getApplicationContext()).deletePlaces(((Place) childTask.getList_of_pids().get(0)).get_id());
                            ;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });


                Button other = new Button(getApplicationContext());
                other.setText("> Other");
                horizontalButtons.addView(other);

                other.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout horizontalButtons = new LinearLayout(getApplicationContext());
                        horizontalButtons.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.setMargins(20, 10, 20, 10);
                        horizontalButtons.setLayoutParams(params);

                        for (Object pid : childTask.getList_of_pids()) {
                            // LinearLayout optionButtons = new LinearLayout(getApplicationContext());
                            // optionButtons.setOrientation(LinearLayout.VERTICAL);

                            TextView tView = new TextView(getApplicationContext());
                            SpannableString styledString = new SpannableString(((Place) pid).getName() + "\n" + "New Brunswick, NJ");
                            styledString.setSpan(new ForegroundColorSpan(Color.GRAY), ((Place) pid).getName().length(), styledString.length(), 0);
                            tView.setGravity(Gravity.CENTER);
                            tView.setText(styledString);
                            tView.setLayoutParams(params);
                            tView.setBackgroundColor(Color.parseColor("#CCCCCC"));
                            tView.setClickable(true);

                            tView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        DatabaseHelper.getHelper(getApplicationContext()).confirmPlace(((Place) pid).get_id());
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            horizontalButtons.addView(tView);


                        }

                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setView(horizontalButtons);
                        alert.create();
                        alert.show();
                        alert.setCancelable(true);

                    }
                });


                list_item_layout.addView(horizontalButtons);


//            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//
//            LatLng latLng1 = new LatLng(-25.63356, -47.440722);
//
////
////            this.mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
////                @Override
////                public void onMapLoaded() {
////                    LatLng sydney = new LatLng(latLng1.latitude, latLng1.longitude);
////                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in here"));
////                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
////                }
////            });
//
//            mapFragment.getMapAsync(this);
//            this.mMap.addMarker(new MarkerOptions()
//                        .position(latLng1)
//                        .title("Testando")
//                        .snippet("Population: 776733"));
//            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));


            }
        } else if(childTask.getPid() instanceof Event){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.google_calendar));
            txtListHeader.setText(((Event) childTask.getPid()).getTitle());
            StringBuilder text = new StringBuilder();
            for(TaskLocalValues taskLocalValues : childTask.getLocalValues()){
                text.append(taskLocalValues.getLocalProperties().getW5h_value());
                text.append(": ");
                text.append(taskLocalValues.getLocal_value());
                text.append("\n");
            }
            txtListHeaderBody.setText(text);
            //txtListHeaderBody.setText(((Event) childTask.getPid()).getCreator().getName());
        }else if(childTask.getPid() instanceof Feed) {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fb_logo));
            txtListHeader.setText(((Feed) childTask.getPid()).getMessage());
            txtListHeaderBody.setText(((Feed) childTask.getPid()).getCreator().getName());
            StringBuilder text = new StringBuilder();
            for(TaskLocalValues taskLocalValues : childTask.getLocalValues()){
                if (taskLocalValues.getLocalProperties().getW5h_label().equalsIgnoreCase("when")){
                    Date date = new Date(Long.parseLong(taskLocalValues.getLocal_value()));
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    text.append(taskLocalValues.getLocalProperties().getW5h_value());
                    text.append(": ");
                    text.append(sf.format(date));
                    text.append("\n");
                }else{
                    text.append(taskLocalValues.getLocalProperties().getW5h_value());
                    text.append(": ");
                    text.append(taskLocalValues.getLocal_value());
                    text.append("\n");
                }
            }
//            StringBuilder sb = new StringBuilder();
//            sb.append("whenWasPosted: " + parsedDate + "\n");
//            sb.append("whereWasPosted: " + ((Feed) childTask.getPid()).getPlace().getName() + "\n" + "whatWasPosted: ");
            final SpannableString myString = new SpannableString(((Feed) childTask.getPid()).getLink());
            text.append(myString);
            String allTheString = text.toString();
            final int i1 = allTheString.indexOf(myString.toString());
            txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
            txtListHeaderBody.setText(allTheString, TextView.BufferType.SPANNABLE);

            Spannable mySpannable = (Spannable) txtListHeaderBody.getText();

            ClickableSpan myClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    WebView mWebview = new WebView(getApplicationContext());
                    mWebview.loadUrl(((TextView) view).getText().toString().substring(i1));
                    alert.setView(mWebview);
                    alert.create();
                    alert.show();


                }
            };
            mySpannable.setSpan(myClickableSpan, i1, i1 + myString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtListHeaderBody.setText(mySpannable);
            txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
        }else if(childTask.getPid() instanceof Photo) {
            if (((Photo) childTask.getPid()).getSource().equalsIgnoreCase("facebook")) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fb_logo));
            }else{
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.insta_logo));
            }
            txtListHeader.setText(((Photo) childTask.getPid()).getName());
            txtListHeaderBody.setText(((Photo) childTask.getPid()).getCreator().getName());

            StringBuilder text = new StringBuilder();
            for(TaskLocalValues taskLocalValues : childTask.getLocalValues()){
                if (taskLocalValues.getLocalProperties().getW5h_label().equalsIgnoreCase("when")){
                    Date date = new Date(Long.parseLong(taskLocalValues.getLocal_value()));
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    text.append(taskLocalValues.getLocalProperties().getW5h_value());
                    text.append(": ");
                    text.append(sf.format(date));
                    text.append("\n");

                    // Date extractedDate = new Date(taskLocalValues.getLocal_value());
                    //Format format = new SimpleDateFormat("yyyy-MM-dd");
                    //String parsedDate = format.format(extractedDate);

                }else{
                    text.append(taskLocalValues.getLocalProperties().getW5h_value());
                    text.append(": ");
                    text.append(taskLocalValues.getLocal_value());
                    text.append("\n");
                }

            }
//            StringBuilder sb = new StringBuilder();
//            sb.append("whenWasPosted: " + parsedDate + "\n");
//            sb.append("whereWasPosted: " + ((Feed) childTask.getPid()).getPlace().getName() + "\n" + "whatWasPosted: ");
            final SpannableString myString = new SpannableString(((Photo) childTask.getPid()).getLink());
            text.append(myString);
            String allTheString = text.toString();
            final int i1 = allTheString.indexOf(myString.toString());
            txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
            txtListHeaderBody.setText(allTheString, TextView.BufferType.SPANNABLE);

            Spannable mySpannable = (Spannable) txtListHeaderBody.getText();

            ClickableSpan myClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    WebView mWebview = new WebView(getApplicationContext());
                    mWebview.setWebChromeClient(new WebChromeClient());
                    mWebview.getSettings().setAllowContentAccess(true);
                    mWebview.getSettings().setJavaScriptEnabled(true);
                    mWebview.getSettings().setDomStorageEnabled(true);
                    mWebview.getSettings().setAllowFileAccess(true);
                    mWebview.getSettings().setUseWideViewPort(true);
                    mWebview.getSettings().setAppCacheEnabled(true);
                    mWebview.loadUrl(((TextView) view).getText().toString().substring(i1));
                    alert.setView(mWebview);
                    alert.create();
                    alert.show();


                }
            };
            mySpannable.setSpan(myClickableSpan, i1, i1 + myString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtListHeaderBody.setText(mySpannable);
            txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return view;
    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.setOnCameraIdleListener(this);
//        mMap.setOnCameraMoveStartedListener(this);
//        mMap.setOnCameraMoveListener(this);
//        mMap.setOnCameraMoveCanceledListener(this);
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//    }
//
//    @Override
//    public void onCameraIdle() {
//
//    }
//
//    @Override
//    public void onCameraMoveCanceled() {
//
//    }
//
//    @Override
//    public void onCameraMove() {
//
//    }
//
//    @Override
//    public void onCameraMoveStarted(int reason) {
//        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
//            Toast.makeText(getApplicationContext(), "The user gestured on the map.",
//                    Toast.LENGTH_SHORT).show();
//        } else if (reason == GoogleMap.OnCameraMoveStartedListener
//                .REASON_API_ANIMATION) {
//            Toast.makeText(getApplicationContext(), "The user tapped something on the map.",
//                    Toast.LENGTH_SHORT).show();
//        } else if (reason == GoogleMap.OnCameraMoveStartedListener
//                .REASON_DEVELOPER_ANIMATION) {
//            Toast.makeText(getApplicationContext(), "The app moved the camera.",
//                    Toast.LENGTH_SHORT).show();
//        }
//
//    }

    class SomeObject {
        public String name;

        public SomeObject(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

}