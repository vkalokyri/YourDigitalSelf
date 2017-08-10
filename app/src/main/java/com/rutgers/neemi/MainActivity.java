package com.rutgers.neemi;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        String key = i.getStringExtra("key");
        int items = i.getIntExtra("items",0);

        if (key!=null) {
            if (key.equalsIgnoreCase("facebook")) {
                SettingsFragment settingsfragment = new SettingsFragment();
                android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                setfragmentTransaction.replace(R.id.frame, settingsfragment);
                setfragmentTransaction.commit();
                if (items == 0) {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No facebook photos fetched.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " facebook photos fetched.", Snackbar.LENGTH_LONG).show();
                }

            }
            if (key.equalsIgnoreCase("gcal")) {
                SettingsFragment settingsfragment = new SettingsFragment();
                android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                setfragmentTransaction.replace(R.id.frame, settingsfragment);
                setfragmentTransaction.commit();
                if (items == 0) {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No calendar events fetched.", Snackbar.LENGTH_LONG ).show();
                } else {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" calendar events fetched.", Snackbar.LENGTH_LONG ).show();
                }

            }
            if (key.equalsIgnoreCase("gmail")) {
                SettingsFragment settingsfragment = new SettingsFragment();
                android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                setfragmentTransaction.replace(R.id.frame, settingsfragment);
                setfragmentTransaction.commit();
                if (items == 0) {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No emails fetched.", Snackbar.LENGTH_LONG ).show();
                } else {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" emails fetched.", Snackbar.LENGTH_LONG ).show();
                }

            }
            if (key.equalsIgnoreCase("bank")) {
                SettingsFragment settingsfragment = new SettingsFragment();
                android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                setfragmentTransaction.replace(R.id.frame, settingsfragment);
                setfragmentTransaction.commit();
                if (items == 0) {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No facebook photos fetched.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout),  items+" financial transactions fetched.", Snackbar.LENGTH_LONG).show();
                }

            }

        }

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){


                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.action_settings:
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.replace(R.id.frame,settingsfragment);
                        setfragmentTransaction.commit();
                        return true;
                    case R.id.trips:
                        ContentFragment fragment = new ContentFragment();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame,fragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.restaurants:
                        GcalFragment gcalFragment = new GcalFragment();
                        android.support.v4.app.FragmentTransaction gcalfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        gcalfragmentTransaction.replace(R.id.frame,gcalFragment);
                        gcalfragmentTransaction.commit();
                        return true;

                    // For rest of the options we just show a toast on click
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }

    }

}