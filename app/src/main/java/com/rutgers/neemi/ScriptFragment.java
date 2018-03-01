package com.rutgers.neemi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.LocalValues;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class ScriptFragment extends Fragment {


    private static final String TAG = "ScriptFragment";
    View myView;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, String> listDataHeaderLocals;
    HashMap<String, List<Task>> listDataChild;
    DatabaseHelper dbHelper;




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
        imageView.setImageResource(imgid[0]);
        ArrayList<LocalValues> localValues = script.getLocalValues();
        if (localValues != null) {
            for (LocalValues localValue : localValues) {
                if (localValue != null) {
                    LocalProperties lp = localValue.getLocalProperties();
                    if (lp != null) {
                        String w5h_value = lp.getW5h_value();
                        if (map.containsKey(w5h_value)) {
                            ArrayList<String> values = map.get(w5h_value);
                            values.add(localValue.getValue());
                            map.put(w5h_value, values);
                        } else {
                            if (w5h_value != null) {
                                ArrayList<String> values = new ArrayList<String>();
                                values.add(localValue.getValue());
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


//        CustomListAdapter adapter=new CustomListAdapter(getActivity(), listOfProcesses, imgid);
//        ListView list=(ListView) myView.findViewById(R.id.restaurant_list);
//        list.setAdapter(adapter);

        return myView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
//        expListView = (ExpandableListView) myView.findViewById(R.id.lvExp);
//
//        // preparing list data
//        prepareListData();
//
//        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
//
//        // setting list adapter
//        expListView.setAdapter(listAdapter);
//
//        Toast.makeText(getContext(), "Pressed 2!", Toast.LENGTH_LONG).show();

        // get the listview

        // CustomListAdapter adapter=new CustomListAdapter(getActivity(), listOfProcesses, imgid);
        // ListView list=(ListView) myView.findViewById(R.id.lvExp);
        // list.setAdapter(adapter);

    }


    /*
     * Preparing the list data
     */
    private void prepareListData(Script script) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Task>>();
        listDataHeaderLocals = new HashMap<String, String>();


        for(Script subscript : script.getSubscripts()){
            StringBuilder header = new StringBuilder();
            header.append(subscript.getScriptDefinition().getName());

            StringBuilder localsSb = new StringBuilder();
            HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

            for (LocalValues lv : subscript.getLocalValues()){
                String key=lv.getLocalProperties().getW5h_value();
                if(map.containsKey(key)){
                    map.get(key).add(lv.getValue());
                }else{
                    HashSet<String> valueSet = new HashSet<String>();
                    valueSet.add(lv.getValue());
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


//            for (LocalValues lv : subscript.getLocalValues()){
//                localsSb.append(lv.getLocalProperties().getW5h_value());
//                localsSb.append(": ");
//                localsSb.append(lv.getValue());
//                localsSb.append("\n");
//                localsSb.toString();
//            }
            listDataHeader.add(header.toString());
            listDataHeaderLocals.put(header.toString(),localsSb.toString());

        }

        // Adding child data
//        listDataHeader.add("Initiate Discussion");
//        listDataHeader.add("Make Reservation");
//        listDataHeader.add("Write in Calendar");

        // Adding child data
        List<Task> tasksInitiated = new ArrayList<Task>();

        for(Task task:script.getTasks()){
            tasksInitiated.add(task);
        }

        listDataChild.put(listDataHeader.get(0), tasksInitiated); // Header, Child data
//        listDataChild.put(listDataHeader.get(1), nowShowing);
//        listDataChild.put(listDataHeader.get(2), comingSoon);
    }


    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<Task>> _listDataChild;
        private HashMap<String, String> _listDataHeaderLocals;

        public ExpandableListAdapter(Activity context, List<String> listDataHeader,  HashMap<String, String> listDataHeaderLocals,
                                     HashMap<String, List<Task>> listChildData) {
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

            final Task childTask = (Task) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
            txtListChild.setText(childTask.getName());
            TextView txtListHeader = (TextView) convertView.findViewById(R.id.relItemHeader);
            TextView txtListHeaderBody = (TextView) convertView.findViewById(R.id.relItem);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
            if(childTask.getPid() instanceof Email){
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.gmail_icon));
                txtListHeader.setText(((Email) childTask.getPid()).getSubject());
                txtListHeaderBody.setText(((Email) childTask.getPid()).getFrom());
            }else if(childTask.getPid() instanceof Payment){
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.bank));
                txtListHeader.setText(((Payment) childTask.getPid()).getName());
                txtListHeaderBody.setText("");
            }else if(childTask.getPid() instanceof Calendar){
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.google_calendar));
                txtListHeader.setText(((Event) childTask.getPid()).getTitle());
                txtListHeaderBody.setText(((Event) childTask.getPid()).getCreator().getName());
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





