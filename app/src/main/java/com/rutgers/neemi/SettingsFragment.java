package com.rutgers.neemi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class SettingsFragment extends ListFragment implements AdapterView.OnItemClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.Services, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        if (position==1){
            Intent myIntent = new Intent(getActivity(), GcalActivity.class);
            startActivity(myIntent);
//            GcalFragment gcalFragment = new GcalFragment();
//            android.support.v4.app.FragmentTransaction gcalfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//            gcalfragmentTransaction.replace(R.id.frame,gcalFragment);
//            gcalfragmentTransaction.commit();
        }
        if (position==0){
            Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
            startActivity(myIntent);
//            FacebookActivity fbFragment = new FacebookActivity();
//            android.support.v4.app.FragmentTransaction fbfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//            fbfragmentTransaction.replace(R.id.frame, fbFragment);
//            fbfragmentTransaction.commit();
        }
        if (position==2){
            Intent myIntent = new Intent(getActivity(), GmailActivity.class);
            startActivity(myIntent);
//            GmailActivity gmailFragment = new GmailActivity();
//            android.support.v4.app.FragmentTransaction gmailfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//            gmailfragmentTransaction.replace(R.id.frame, gmailFragment);
//            gmailfragmentTransaction.commit();
        }
        if (position==3){
            Intent myIntent = new Intent(getActivity(), PlaidActivity.class);
            startActivity(myIntent);
//            PlaidActivity plaidFragment = new PlaidActivity();
//            android.support.v4.app.FragmentTransaction plaidfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//            plaidfragmentTransaction.replace(R.id.frame, plaidFragment);
//            plaidfragmentTransaction.commit();
        }


//

    }
}





//        Preference myPref = (Preference) findPreference("gcal");
//        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                GcalFragment gcalFragment = new GcalFragment();
//                android.support.v4.app.FragmentTransaction gcalfragmentTransaction = getSupportFragmentManager().beginTransaction();
//                gcalfragmentTransaction.replace(R.id.frame,gcalFragment);
//                gcalfragmentTransaction.commit();
//                return true;
//
//            }
//        });



        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
//}

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
////        if (id == android.R.id.home) {
////            startActivity(new Intent(getActivity(), SettingsActivity.class));
////            return true;
////        }
//
//
//
//
//        return super.onOptionsItemSelected(item);
//    }
//}