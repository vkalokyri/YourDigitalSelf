package com.rutgers.neemi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.Transaction;

import java.lang.reflect.Array;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;


public class ScriptFragment extends Fragment {

    private static final String TAG = "ScriptFragment";
    View myView;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, String> listDataHeaderLocals;
    HashMap<String, List<Object>> listDataChild;


    Integer[] imgid={
            R.drawable.restaurant
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.scriptview, container, false);
        expListView = (ExpandableListView) myView.findViewById(R.id.lvExp);

        ArrayList listOfProcesses = (ArrayList) getArguments().getSerializable("processes");
        int position = (int) getArguments().getSerializable("position");
        long id  = (long) getArguments().getSerializable("id");

        LinearLayout linearLayout = (LinearLayout) myView.findViewById(R.id.linearLayout);
        // TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) myView.findViewById(R.id.icon);


        Script script = (Script)listOfProcesses.get(position);//.getScriptDefinition();
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

        Place place = null;

        for (Task task: script.getTasks()) {
            if (task.getPid() instanceof Transaction) {
                place = ((Transaction) task.getPid()).getPlace();
                break;
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
                imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 40, 40, false));
            }
        }else{
            imageView.setImageResource(imgid[0]);

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

        // preparing list data
        prepareListData((Script)listOfProcesses.get(position));

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataHeaderLocals, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);


        return myView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

    }


    /*
     * Preparing the list data
     */
    private void prepareListData(Script script) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Object>>();
        listDataHeaderLocals = new HashMap<String, String>();

        for(Script subscript : script.getSubscripts()){
            StringBuilder header = new StringBuilder();
            header.append(subscript.getScriptDefinition().getName());

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

            listDataHeader.add(header.toString());
            listDataHeaderLocals.put(header.toString(),localsSb.toString());
        }


        // Adding child data
        List<Object> tasksInitiated = new ArrayList<Object>();

        tasksInitiated.addAll(script.getTasks());
        tasksInitiated.addAll(script.getSubscripts());

//        for(Task task:script.getTasks()){
//            tasksInitiated.add(task);
//        }
//
//        for (Script sub: script.getSubscripts()){
//            tasksInitiated.addAll(sub)
//        }
//

        listDataChild.put(listDataHeader.get(0), tasksInitiated); // Header, Child data
    }


    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<Object>> _listDataChild;
        private HashMap<String, String> _listDataHeaderLocals;

        public ExpandableListAdapter(Activity context, List<String> listDataHeader,  HashMap<String, String> listDataHeaderLocals,
                                     HashMap<String, List<Object>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
            this._listDataHeaderLocals = listDataHeaderLocals;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            //final Task childTask = (Task) getChild(groupPosition, childPosition);

            Object child = (Object) getChild(groupPosition, childPosition);

            if (child instanceof Task){
                Task childTask = (Task) child;

                if (convertView == null) {
                    LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.list_item, null);
                }

                TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
                txtListChild.setText(childTask.getName());
                TextView txtListHeader = (TextView) convertView.findViewById(R.id.relItemHeader);
                TextView txtListHeaderBody = (TextView) convertView.findViewById(R.id.relItem);
                final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
                if(childTask.getPid() instanceof Email){
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.gmail_icon));
                    txtListHeader.setText(((Email) childTask.getPid()).getSubject());
                    String text = "whoSent: "+((Email) childTask.getPid()).getFrom()+" \n whoReceived: "+ ((Email) childTask.getPid()).getTo()+" \n whenSent: "+ ((Email) childTask.getPid()).getDate();
                    txtListHeaderBody.setText(text);
                }else if(childTask.getPid() instanceof Transaction){
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.bank));
                    txtListHeader.setText(((Transaction) childTask.getPid()).getMerchant_name());
                    Date extractedDate = new Date(((Transaction) childTask.getPid()).getDate());
                    Format format = new SimpleDateFormat("yyyy-MM-dd");
                    String parsedDate = format.format(extractedDate);
                    String text = "whenPaid: "+parsedDate+" \n howMuchWasPaid: $"+((Transaction) childTask.getPid()).getAmount() ;
                    txtListHeaderBody.setText(text);
                }else if(childTask.getPid() instanceof Calendar){
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.google_calendar));
                    txtListHeader.setText(((Event) childTask.getPid()).getTitle());
                    txtListHeaderBody.setText(((Event) childTask.getPid()).getCreator().getName());
                }else if(childTask.getPid() instanceof Feed){
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.fb_logo));
                    txtListHeader.setText(((Feed) childTask.getPid()).getMessage());
                    txtListHeaderBody.setText(((Feed) childTask.getPid()).getCreator().getName());
                    Date extractedDate = new Date(((Feed) childTask.getPid()).getCreated_time());
                    Format format = new SimpleDateFormat("yyyy-MM-dd");
                    String parsedDate = format.format(extractedDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append("whenWasPosted: "+parsedDate+"\n");
                    sb.append("whereWasPosted: "+((Feed) childTask.getPid()).getPlace().getName()+"\n"+"whatWasPosted: ");
                    final SpannableString myString = new SpannableString(((Feed) childTask.getPid()).getLink());
                    sb.append(myString);
                    String allTheString = sb.toString();
                    final int i1 = allTheString.indexOf(myString.toString());
                    txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
                    txtListHeaderBody.setText(allTheString, TextView.BufferType.SPANNABLE);

                    Spannable mySpannable = (Spannable)txtListHeaderBody.getText();

                    ClickableSpan myClickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                            WebView mWebview  = new WebView(getApplicationContext());
                            mWebview .loadUrl(((TextView)view).getText().toString().substring(i1));
                            alert.setView(mWebview);
                            alert.create();
                            alert.show();


                        }
                    };
                    mySpannable.setSpan(myClickableSpan, i1, i1+myString.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtListHeaderBody.setText(mySpannable);
                    txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }else if (child instanceof Script){
                for(Script subscript : ((Script)child).getSubscripts()){
                    StringBuilder header = new StringBuilder();
                    header.append(subscript.getScriptDefinition().getName());

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

                    //listDataHeader.add(header.toString());
                   // listDataHeaderLocals.put(header.toString(),localsSb.toString());

                    if (convertView == null) {
                        LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = infalInflater.inflate(R.layout.list_group, null);
                        convertView.setBackgroundColor(Color.parseColor("#999999"));
                    }
                    TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
                    lblListHeader.setTextColor(Color.parseColor("#333333"));
                    lblListHeader.setTypeface(null, Typeface.BOLD);
                    lblListHeader.setText(header.toString());

                    TextView lblListHeaderLocals = (TextView) convertView.findViewById(R.id.lblListHeaderLocals);
                    lblListHeaderLocals.setTextColor(Color.parseColor("#333333"));
                    lblListHeaderLocals.setTypeface(null, Typeface.NORMAL);
                    lblListHeaderLocals.setText(localsSb.toString());
                }


                //String headerTitle = (String) getGroup(groupPosition);

            }

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            ArrayList<Object> header = new ArrayList<>();
            String headerTitle = this._listDataHeader.get(groupPosition);
            header.add(headerTitle);
            header.add(this._listDataHeaderLocals.get(headerTitle));
            return header;
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ArrayList<Object> header = (ArrayList<Object>) getGroup(groupPosition);
            String headerTitle = (String)header.get(0);
            String headerTitle2 = (String)header.get(1);

            //String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
                convertView.setBackgroundColor(Color.parseColor("#999999"));
            }
            TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setTextColor(Color.parseColor("#333333"));
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            TextView lblListHeaderLocals = (TextView) convertView.findViewById(R.id.lblListHeaderLocals);
            lblListHeaderLocals.setTextColor(Color.parseColor("#333333"));
            lblListHeaderLocals.setTypeface(null, Typeface.NORMAL);
            lblListHeaderLocals.setText(headerTitle2);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }






}





