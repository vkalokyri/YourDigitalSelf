package com.rutgers.neemi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Report;
import com.rutgers.neemi.util.ApplicationManager;
import com.rutgers.neemi.util.NER;
import com.rutgers.neemi.util.OpenNLP;


import java.io.IOException;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper helper= DatabaseHelper.getHelper(this);
        setContentView(R.layout.activity_main);


        // Write a message to the database
//        mDatabase = FirebaseDatabase.getInstance().getReference();

       // mDatabase.child("users").child("12345").child("instance2").setValue(new Report(true,"correct","correct",true,"correct"));


//        ConnectionSource connectionSource = new AndroidConnectionSource(helper);
//        RuntimeExceptionDao<Subscript, String> subScriptDao = helper.getSubScriptDao();
//        subScriptDao.queryRaw("delete from  Subscript;");
//        subScriptDao.queryRaw("delete from LocalProperties;");
//        subScriptDao.queryRaw("delete from  ScriptDefHasTaskDef;");
//        subScriptDao.queryRaw("delete from  ScriptDefinition;");
//        subScriptDao.queryRaw("delete from  TaskDefinition;");
//
//                try {
//                    //TableUtils.clearTable(connectionSource, Email.class,false);
//                    TableUtils.dropTable(connectionSource, Subscript.class,true);
//                    TableUtils.createTable(connectionSource, Subscript.class);
//                    TableUtils.dropTable(connectionSource, ScriptDefHasTaskDef.class,true);
//                    TableUtils.createTable(connectionSource, ScriptDefHasTaskDef.class);
//                    TableUtils.dropTable(connectionSource, ScriptDefinition.class,true);
//                    TableUtils.createTable(connectionSource, ScriptDefinition.class);
//                    TableUtils.dropTable(connectionSource, TaskDefinition.class,true);
//                    TableUtils.createTable(connectionSource, TaskDefinition.class);
//
//
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {

            //DataSyncJob.schedulePeriodicJob();

            // <---- run your one time code here
            ApplicationManager appManager = new ApplicationManager();
            appManager.initScript(helper, getApplicationContext(),"restaurant");
            appManager.initScript(helper, getApplicationContext(),"trip");


            // mark first time has runned.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();

            Intent myIntent = new Intent(this, IntroActivity.class);
            startActivity(myIntent);

            getFragmentManager().beginTransaction()
                .replace(R.id.frame, new SettingsFragment2())
                .addToBackStack(null)
                .commit();


        }

//        boolean gmailPermission = prefs.getBoolean("gmail", true);
//
//        PersistableBundle pb = new PersistableBundle();
//        pb.putBoolean("gmailPermission" , gmailPermission);

       //


            Intent i = getIntent();
            String key = i.getStringExtra("key");
            int items = i.getIntExtra("items", 0);

            if (key != null) {
                if (key.equalsIgnoreCase("facebook")) {
                    if (items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No facebook data fetched.", Snackbar.LENGTH_LONG).show();

                    } else if(items>0){
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " facebook objects fetched.", Snackbar.LENGTH_LONG).show();
                    }


                }
                if (key.equalsIgnoreCase("instagram")) {

                    if (items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No instagram photos fetched.", Snackbar.LENGTH_LONG).show();

                    } else if (items>0){
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " instagram photos fetched.", Snackbar.LENGTH_LONG).show();

                    }

                }
                if (key.equalsIgnoreCase("gcal")) {

                    if (items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No calendar events fetched.", Snackbar.LENGTH_LONG ).show();
                    } else if (items>0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" calendar events fetched.", Snackbar.LENGTH_LONG ).show();
                    }

                }
                if (key.equalsIgnoreCase("gmail")) {

                    if(items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No emails fetched.", Snackbar.LENGTH_LONG ).show();
                    } else if(items>0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" emails fetched.", Snackbar.LENGTH_LONG ).show();
                    }
                }
                if (key.equalsIgnoreCase("bank")) {

                    if (items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No financial transactions fetched.", Snackbar.LENGTH_LONG).show();
                    } else if(items>0){
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout),  items+" financial transactions fetched.", Snackbar.LENGTH_LONG).show();
                    }

                }
                if (key.equalsIgnoreCase("gdrive")) {


                    if (items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No financial transactions fetched.", Snackbar.LENGTH_LONG).show();
                    } else {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout),  items+" financial transactions fetched.", Snackbar.LENGTH_LONG).show();
                    }
                }
                if (key.equalsIgnoreCase("sync_data")) {
                    SettingsFragment settingsfragment = new SettingsFragment();
                    android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                    setfragmentTransaction.add(R.id.frame, settingsfragment);
                    setfragmentTransaction.addToBackStack(null);
                    setfragmentTransaction.commit();
                }

                if (key.equalsIgnoreCase("gmaps")) {
                    if(items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No locations fetched.", Snackbar.LENGTH_LONG ).show();
                    } else if(items>0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" staying points fetched.", Snackbar.LENGTH_LONG ).show();
                    }
                }

                if (key.equalsIgnoreCase("gmaps")) {
                    if(items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No locations fetched.", Snackbar.LENGTH_LONG ).show();
                    } else if(items>0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" staying points fetched.", Snackbar.LENGTH_LONG ).show();
                    }
                }
                if (key.equalsIgnoreCase("gphotos")) {
                    if(items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No google photos fetched.", Snackbar.LENGTH_LONG ).show();
                    } else if(items>0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" google photos fetched.", Snackbar.LENGTH_LONG ).show();
                    }
                }
                if (key.equalsIgnoreCase("sms")) {
                    if(items == 0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No text messages fetched.", Snackbar.LENGTH_LONG ).show();
                    } else if(items>0) {
                        SettingsFragment settingsfragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction setfragmentTransaction = getSupportFragmentManager().beginTransaction();
                        setfragmentTransaction.add(R.id.frame, settingsfragment);
                        setfragmentTransaction.addToBackStack(null);
                        setfragmentTransaction.commit();
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" text messages fetched.", Snackbar.LENGTH_LONG ).show();
                    }
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame, new SettingsFragment2())
                        .commit();



            }

            // Initializing Toolbar and setting it as the actionbar
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Initializing NavigationView
            navigationView = (NavigationView) findViewById(R.id.navigation_view);
            navigationView.setItemIconTintList(null);


            //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

                // This method will trigger on item Click of navigation menu
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {


                    //Checking if the item is in checked state or not, if not make it in checked state
                    if (menuItem.isChecked())
                        menuItem.setChecked(false);
                    else menuItem.setChecked(true);


                    //Closing drawer on item click
                    drawerLayout.closeDrawers();

                    //Check to see which item was being clicked and perform appropriate action
                    switch (menuItem.getItemId()) {


                        //Replacing the main content with ContentFragment Which is our Inbox View;
                        case R.id.action_settings:
                            getFragmentManager().beginTransaction()
                            .replace(R.id.frame, new SettingsFragment2())
                            .addToBackStack(null)
                            .commit();
                            return true;
                        case R.id.trips:
                            ContentFragment fragment = new ContentFragment();
                            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.add(R.id.frame, fragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                            return true;
                        case R.id.restaurants:
                            RestaurantsFragment restFragment = new RestaurantsFragment();
                            android.support.v4.app.FragmentTransaction restaurantsfragmentTrans = getSupportFragmentManager().beginTransaction();
                            restaurantsfragmentTrans.add(R.id.frame, restFragment);
                            restaurantsfragmentTrans.addToBackStack(null);
                            restaurantsfragmentTrans.commit();
                            return true;
                        case R.id.data_sync:
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.frame, new DataSyncPreferenceFragment())
                                    .addToBackStack(null)
                                    .commit();
                            return true;

                        // For rest of the options we just show a toast on click
                        default:
                            Toast.makeText(getApplicationContext(), "Something is Wrong", Toast.LENGTH_SHORT).show();
                            return true;

                    }
                }
            });

            // Initializing Drawer Layout and ActionBarToggle
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

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

            //setFirstItemNavigationView();


    }

    private void setFirstItemNavigationView() {
        navigationView.setCheckedItem(R.id.restaurants);
        navigationView.getMenu().performIdentifierAction(R.id.restaurants, 0);
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