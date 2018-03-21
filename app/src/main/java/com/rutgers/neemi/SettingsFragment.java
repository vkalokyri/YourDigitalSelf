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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    View myView;

    String[] itemname ={
            "Facebook",
            "Instagram",
            "Google Calendar",
            "Gmail",
            "Bank data",
            "Location data",
    };

    Integer[] imgid={
            R.drawable.fb_logo,
            R.drawable.insta_logo,
            R.drawable.google_calendar,
            R.drawable.gmail_icon,
            R.drawable.bank,
            R.drawable.location
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
                String Selecteditem= itemname[+position];

                if (Selecteditem.equalsIgnoreCase("Google Calendar")){
                    Intent myIntent = new Intent(getActivity(), GcalActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                 if (Selecteditem.equalsIgnoreCase("Facebook")) {
                     Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
                     myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(myIntent);
                 }
                if (Selecteditem.equalsIgnoreCase("Instagram")) {
                    Intent myIntent = new Intent(getActivity(), InstagramActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Gmail")){
                    Intent myIntent = new Intent(getActivity(), GmailActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                if (Selecteditem.equalsIgnoreCase("Bank data")){
                    Intent myIntent = new Intent(getActivity(), BankActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
//                    ArrayList accountNames = new ArrayList();
//                    String line;
//                    try {
//                        FileInputStream fis = getActivity().openFileInput("BankAccounts");
//                        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
//                        BufferedReader br = new BufferedReader(isr);
//                        while ((line = br.readLine()) != null) {
//                            accountNames.add(line);
//                        }
//                        fis.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (accountNames.size()==0) {
//                        Intent myIntent = new Intent(getActivity(), BankActivity.class);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(myIntent);
//                    }else{
//                        BankFragment bankfragment = new BankFragment();
//                        Bundle args = new Bundle();
//                        args.putStringArrayList("Accounts", accountNames);
//                        bankfragment.setArguments(args);
//                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//                        setfragmentTransaction.replace(R.id.frame,bankfragment);
//                        setfragmentTransaction.addToBackStack(null);
//                        setfragmentTransaction.commit();
//                    }

                }

                if (Selecteditem.equalsIgnoreCase("Location data")){
                    Intent myIntent = new Intent(getActivity(), LocationActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
                Toast.makeText(getActivity(), Selecteditem, Toast.LENGTH_SHORT).show();

            }
        });


    }


    private class CustomListAdapter extends ArrayAdapter<String> {

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

