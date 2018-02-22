package com.rutgers.neemi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ScriptFragment extends Fragment {


    private static final String TAG = "ScriptFragment";
    View myView;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    DatabaseHelper dbHelper;

    Integer[] imgid={
            R.drawable.restaurant
    };




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.scriptview, container, false);
        expListView = (ExpandableListView) myView.findViewById(R.id.lvExp);

        ArrayList listOfProcesses = (ArrayList) getArguments().getSerializable("processes");

        // preparing list data
        prepareListData(listOfProcesses);

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

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
    private void prepareListData(ArrayList<Script> scripts) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        dbHelper=DatabaseHelper.getHelper(getActivity());
        dbHelper.getTopScriptByTask()

        // Adding child data
        listDataHeader.add("Initiate Discussion");
        listDataHeader.add("Make Reservation");
        listDataHeader.add("Write in Calendar");

        // Adding child data
        List<String> top250 = new ArrayList<String>();

        top250.add("whoInitiatedTheConversation: JohnSmith\nwhoWasIncludedInTheConversation: George Michael, Maria");
//        top250.add("whoWasIncludedInTheConversation: George Michael, Maria …");
//        top250.add("whenWasTheConversationInitiated: 2017/10/06");
//        top250.add("whenIsTheProposedEvent: 2017/10/07");
//        top250.add("whereIsThePlanToGo: Ippudo");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);
    }


    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;

        public ExpandableListAdapter(Activity context, List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
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

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
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
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
                convertView.setBackgroundColor(Color.parseColor("#999999"));
            }
            TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setTextColor(Color.parseColor("#333333"));
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

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



    private class CustomListAdapter extends ArrayAdapter<Script> {

        private final Activity context;
        private final List<Script> itemname;
        private final Integer[] imgid;

        public CustomListAdapter(Activity context, List<Script> tasks, Integer[] imgid) {
            super(context, R.layout.restaurantsview, tasks);
            // TODO Auto-generated constructor stub
            this.context=context;
            this.itemname=tasks;
            this.imgid=imgid;
        }

        public View getView(int position,View view,ViewGroup parent) {


            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.restaurantsview, null,true);
            LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayout);

            // TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);


            for (Task processTask : itemname.get(position).getTasks()) {
                System.out.println("SIZE OF TASKS = "+itemname.get(position).getTasks().size());
                if (processTask.getPid() instanceof Email){
                    //txtTitle.setText(itemname.get(position).getScore()+", "+String.valueOf(((Email)processTask.getPid()).get_id()));
                    imageView.setImageResource(imgid[0]);
//                    if (processTask.getLocals() != null) {
//                        for (LocalProperties local : processTask.getLocals()) {
//                            if (!local.getW5h_value().toString().equalsIgnoreCase("[null]") && !local.getW5h_value().toString().equalsIgnoreCase("[]") ) {
//
//                                LinearLayout textLayout = new LinearLayout(context);
//                                textLayout.setOrientation(LinearLayout.HORIZONTAL);
//                                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                                llp.setMargins(10, 5, 5, 0);
//                                textLayout.setLayoutParams(llp);
//
//                                TextView localTextView = new TextView(this.getContext());
//                                localTextView.setTextColor(Color.parseColor("#99CCFF"));
//                                localTextView.setText(getString(R.string.local, local.getW5h_label() + " : "));
//
//                                TextView localValueTextView = new TextView(this.getContext());
//                                localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
//                                localValueTextView.setText(getString(R.string.local, local.getW5h_value().toString()));
//
//                                textLayout.addView(localTextView);
//                                textLayout.addView(localValueTextView);
//                                linearLayout.addView(textLayout);
//                            }
//                        }
//                    }

                }else if (processTask.getPid() instanceof Payment) {
                    //txtTitle.setText(itemname.get(position).getScore()+", "+((Payment) processTask.getPid()).getName());
                    imageView.setImageResource(imgid[0]);
//                    if (processTask.getLocals() != null) {
//                        for (LocalProperties local : processTask.getLocals()) {
//                            LinearLayout textLayout = new LinearLayout(context);
//                            textLayout.setOrientation(LinearLayout.HORIZONTAL);
//                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                            llp.setMargins(10, 5, 5, 0);
//                            textLayout.setLayoutParams(llp);
//
//                            TextView localTextView = new TextView(this.getContext());
//                            localTextView.setTextColor(Color.parseColor("#99CCFF"));
//                            localTextView.setText(getString(R.string.local, local.getW5h_label() + " : "));
//
//                            TextView localValueTextView = new TextView(this.getContext());
//                            localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
//                            localValueTextView.setText(getString(R.string.local, local.getW5h_value().toString()));
//
//                            textLayout.addView(localTextView);
//                            textLayout.addView(localValueTextView);
//                            linearLayout.addView(textLayout);

    //                            TextView localTextView = new TextView(this.getContext());
    //                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    //                            llp.setMargins(5, 5, 5, 5);
    //                            localTextView.setLayoutParams(llp);
    //                            localTextView.setText(getString(R.string.local, local.getW5h_label() + " : " + local.getValue().toString()));
    //                            linearLayout.addView(localTextView);
//                        }
 //                   }
                }
            }


            return rowView;

        };
    }


}





