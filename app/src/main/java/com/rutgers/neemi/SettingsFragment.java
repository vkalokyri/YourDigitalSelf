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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsFragment extends Fragment {

    View myView;

    String[] itemname ={
            "Facebook",
            "Google Calendar",
            "Gmail",
            "Bank data",
            "Location data",
    };

    Integer[] imgid={
            R.drawable.fb_logo,
            R.drawable.google_calendar,
            R.drawable.gmail_icon,
            R.drawable.bank,
            R.drawable.location,
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_settings, container, false);

        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        CustomListAdapter adapter=new CustomListAdapter(getActivity(), itemname, imgid);
        ListView list=(ListView) myView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String Slecteditem= itemname[+position];
                Toast.makeText(getActivity(), Slecteditem, Toast.LENGTH_SHORT).show();

            }
        });


//        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.Services, android.R.layout.simple_list_item_1);
//        setListAdapter(adapter);
//        getListView().setOnItemClickListener(this);
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
//        if (position==1){
//            Intent myIntent = new Intent(getActivity(), GcalActivity.class);
//            startActivity(myIntent);
////            GcalFragment gcalFragment = new GcalFragment();
////            android.support.v4.app.FragmentTransaction gcalfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
////            gcalfragmentTransaction.replace(R.id.frame,gcalFragment);
////            gcalfragmentTransaction.commit();
//        }
//        if (position==0){
//            Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
//            startActivity(myIntent);
////            FacebookActivity fbFragment = new FacebookActivity();
////            android.support.v4.app.FragmentTransaction fbfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
////            fbfragmentTransaction.replace(R.id.frame, fbFragment);
////            fbfragmentTransaction.commit();
//        }
//        if (position==2){
//            Intent myIntent = new Intent(getActivity(), GmailActivity.class);
//            startActivity(myIntent);
////            GmailActivity gmailFragment = new GmailActivity();
////            android.support.v4.app.FragmentTransaction gmailfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
////            gmailfragmentTransaction.replace(R.id.frame, gmailFragment);
////            gmailfragmentTransaction.commit();
//        }
//        if (position==3){
//            Intent myIntent = new Intent(getActivity(), PlaidActivity.class);
//            startActivity(myIntent);
////            PlaidActivity plaidFragment = new PlaidActivity();
////            android.support.v4.app.FragmentTransaction plaidfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
////            plaidfragmentTransaction.replace(R.id.frame, plaidFragment);
////            plaidfragmentTransaction.commit();
//        }
//
//
////
//
//    }


    public class CustomListAdapter extends ArrayAdapter<String> {

        private final Activity context;
        private final String[] itemname;
        private final Integer[] imgid;

        public CustomListAdapter(Activity context, String[] itemname, Integer[] imgid) {
            super(context, R.layout.listview, itemname);
            // TODO Auto-generated constructor stub

            this.context=context;
            this.itemname=itemname;
            this.imgid=imgid;
        }

        public View getView(int position,View view,ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.listview, null,true);

            TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

            txtTitle.setText(itemname[position]);
            imageView.setImageResource(imgid[position]);
            return rowView;

        };
    }



}

