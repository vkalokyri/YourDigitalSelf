package com.rutgers.neemi;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class BankActivity extends AppCompatActivity {

    public static BankActivity instance;
    private PlaidFragment plaidFragment;
    private BankFragment bankFragment;
    private TabLayout allTabs;
    private boolean plaidUsedBefore=false;

    private int[] tabIcons = {
            R.drawable.gdrive_icon,
            R.drawable.plaid
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        instance=this;
        getAllWidgets();
        bindWidgetsWithAnEvent();
        setupTabLayout();
    }
    public static BankActivity getInstance() {
        return instance;
    }

    private void getAllWidgets() {
        allTabs = (TabLayout) findViewById(R.id.tabs);
    }
    private void setupTabLayout() {

        plaidFragment = new PlaidFragment();
        bankFragment = new BankFragment();
        allTabs.addTab(allTabs.newTab().setText("Download your transactions (plaid api)"),true);
        allTabs.addTab(allTabs.newTab().setText("Upload your transactions (.csv)"));

        allTabs.getTabAt(0).setIcon(tabIcons[1]);
        allTabs.getTabAt(1).setIcon(tabIcons[0]);
    }

    private void bindWidgetsWithAnEvent()
    {
        allTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    private void setCurrentTabFragment(int tabPosition) {
        ArrayList accountNames = new ArrayList();
        String line;
        try {
            FileInputStream fis = openFileInput("BankAccounts");
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                accountNames.add(line);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (accountNames.size()!=0) {
            plaidUsedBefore=true;
        }

        switch (tabPosition)
        {
            case 0 :
                if(plaidUsedBefore){
                    Bundle args = new Bundle();
                    args.putStringArrayList("Accounts", accountNames);
                    bankFragment.setArguments(args);
                    replaceFragment(bankFragment);
                }else {
                    replaceFragment(plaidFragment);
                }
                break;
            case 1 :
                Intent myIntent = new Intent(this, GDriveActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
                break;
        }
    }
    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
}