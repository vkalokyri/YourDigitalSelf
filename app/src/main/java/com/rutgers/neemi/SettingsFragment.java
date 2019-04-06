package com.rutgers.neemi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rutgers.neemi.LocationActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    View myView;

    private ExpandListAdapter ExpAdapter;
    private ArrayList<ExpandListGroup> ExpListItems;
    private ExpandableListView ExpandList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_settings, container, false);

        return myView;
    }


    public ArrayList<ExpandListGroup> SetStandardGroups() {
        ArrayList<ExpandListGroup> list = new ArrayList<ExpandListGroup>();
        ArrayList<ExpandListChild> list2 = new ArrayList<ExpandListChild>();
        ExpandListGroup gru1 = new ExpandListGroup();
        gru1.setName("Social Media");
        ExpandListChild ch1_1 = new ExpandListChild();
        ch1_1.setName("Facebook");
        ch1_1.setImg(R.drawable.fb_logo);
        list2.add(ch1_1);
        ExpandListChild ch1_2 = new ExpandListChild();
        ch1_2.setName("Instagram");
        ch1_2.setImg(R.drawable.insta_logo);
        list2.add(ch1_2);
        ExpandListChild ch1_3 = new ExpandListChild();
        ch1_3.setName("Messenger");
        ch1_3.setImg(R.drawable.messenger);
        list2.add(ch1_3);
        gru1.setItems(list2);

        list2 = new ArrayList<ExpandListChild>();
        ExpandListGroup gru2 = new ExpandListGroup();
        gru2.setName("Calendar");
        ExpandListChild ch2_1 = new ExpandListChild();
        ch2_1.setName("Google Calendar");
        ch2_1.setImg(R.drawable.google_calendar);
        list2.add(ch2_1);
        ExpandListChild ch2_2 = new ExpandListChild();
        ch2_2.setName("Office 365 Calendar");
        ch2_2.setImg(R.drawable.officecalendar);
        list2.add(ch2_2);
        gru2.setItems(list2);

        list2 = new ArrayList<ExpandListChild>();
        ExpandListGroup gru3 = new ExpandListGroup();
        gru3.setName("Email");
        ExpandListChild ch3_1 = new ExpandListChild();
        ch3_1.setName("Gmail");
        ch3_1.setImg(R.drawable.gmail_icon);
        list2.add(ch3_1);
        gru3.setItems(list2);


        list2 = new ArrayList<ExpandListChild>();
        ExpandListGroup gru4 = new ExpandListGroup();
        gru4.setName("Bank transactions");
        ExpandListChild ch4_1 = new ExpandListChild();
        ch4_1.setName("Plaid");
        ch4_1.setImg(R.drawable.plaid);
        list2.add(ch4_1);
        ExpandListChild ch4_2 = new ExpandListChild();
        ch4_2.setName("Google Drive");
        ch4_2.setImg(R.drawable.gdrive_icon);
        list2.add(ch4_2);
        gru4.setItems(list2);

        list2 = new ArrayList<ExpandListChild>();
        ExpandListGroup gru5 = new ExpandListGroup();
        gru5.setName("Location services");
        ExpandListChild ch5_1 = new ExpandListChild();
        ch5_1.setName("GPS data");
        ch5_1.setImg(R.drawable.location);
        list2.add(ch5_1);
        ExpandListChild ch5_2 = new ExpandListChild();
        ch5_2.setName("Foursquare");
        ch5_2.setImg(R.drawable.foursquare);
        list2.add(ch5_2);
        ExpandListChild ch5_3 = new ExpandListChild();
        ch5_3.setName("Google Maps");
        ch5_3.setImg(R.drawable.gmaps);
        list2.add(ch5_3);
        gru5.setItems(list2);


        list.add(gru1);
        list.add(gru2);
        list.add(gru3);
        list.add(gru4);
        list.add(gru5);

        return list;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        ExpandList = (ExpandableListView) myView.findViewById(R.id.sourcesExpList);

        ExpListItems = SetStandardGroups();
        ExpAdapter = new ExpandListAdapter(getActivity(), ExpListItems);
        ExpandList.setAdapter(ExpAdapter);

        ExpandList.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){

            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                String Selecteditem= ((ExpandListChild)ExpAdapter.getChild(i,i1)).getName();
                if (Selecteditem.equalsIgnoreCase("Google Calendar")){
                    Intent myIntent = new Intent(getActivity(), GcalActivity.class);
                    myIntent.putExtra("action", "sync");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Facebook")) {
                    Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
                    myIntent.putExtra("action", "sync");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Instagram")) {
                    Intent myIntent = new Intent(getActivity(), InstagramActivity.class);
                    myIntent.putExtra("action", "sync");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Messenger")) {
                    Intent myIntent = new Intent(getActivity(), MessengerDriveActivity.class);
                    myIntent.putExtra("action", "sync");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Gmail")){
                    Intent myIntent = new Intent(getActivity(), GmailActivity.class);
                    myIntent.putExtra("action", "sync");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Plaid")){
                    Intent myIntent = new Intent(getActivity(), BankActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Google Drive")){
                    Intent myIntent = new Intent(getActivity(), GDriveActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }

                if (Selecteditem.equalsIgnoreCase("GPS data")){
                    System.err.println("clickedGPS");
                    Intent myIntent = new Intent(getActivity(), LocationActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Google Maps")){
                    Intent myIntent = new Intent(getActivity(), GmapsActivity.class);
                    myIntent.putExtra("action", "sync");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                Toast.makeText(getActivity(), Selecteditem, Toast.LENGTH_SHORT).show();
                return true;

            }


        });




    }




    public class ExpandListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private ArrayList<ExpandListGroup> groups;
        public ExpandListAdapter(Context context, ArrayList<ExpandListGroup> groups) {
            this.context = context;
            this.groups = groups;
        }

        public void addItem(ExpandListChild item, ExpandListGroup group) {
            if (!groups.contains(group)) {
                groups.add(group);
            }
            int index = groups.indexOf(group);
            ArrayList<ExpandListChild> ch = groups.get(index).getItems();
            ch.add(item);
            groups.get(index).setItems(ch);
        }
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            ArrayList<ExpandListChild> chList = groups.get(groupPosition).getItems();
            return chList.get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childPosition;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
                                 ViewGroup parent) {
            ExpandListChild child = (ExpandListChild) getChild(groupPosition, childPosition);
            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.listview, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.item);
            tv.setText(child.getName().toString());
            ImageView iv = (ImageView) view.findViewById(R.id.icon);
            iv.setImageResource(child.getImg());
            // TODO Auto-generated method stub
            return view;
        }

        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            ArrayList<ExpandListChild> chList = groups.get(groupPosition).getItems();

            return chList.size();

        }

        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return groups.get(groupPosition);
        }

        public int getGroupCount() {
            // TODO Auto-generated method stub
            return groups.size();
        }

        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isLastChild, View view,
                                 ViewGroup parent) {
            ExpandListGroup group = (ExpandListGroup) getGroup(groupPosition);
            if (view == null) {
                LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                view = inf.inflate(R.layout.list_sources, null);
            }
            TextView tv = (TextView) view.findViewById(R.id.sourcesHeader);
            tv.setText(group.getName());
            // TODO Auto-generated method stub
            return view;
        }

        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return true;
        }

        public boolean isChildSelectable(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return true;
        }

    }



    public class ExpandListGroup {

        private String Name;
        private ArrayList<ExpandListChild> Items;

        public String getName() {
            return Name;
        }
        public void setName(String name) {
            this.Name = name;
        }
        public ArrayList<ExpandListChild> getItems() {
            return Items;
        }
        public void setItems(ArrayList<ExpandListChild> Items) {
            this.Items = Items;
        }


    }


    public class ExpandListChild {

        private String name;
        private Integer img;

        public String getName() {
            return name;
        }
        public void setName(String Name) {
            this.name = Name;
        }
        public Integer getImg() {
            return img;
        }
        public void setImg(Integer img) {
            this.img = img;
        }
    }



}

