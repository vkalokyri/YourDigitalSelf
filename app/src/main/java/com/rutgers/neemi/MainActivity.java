package com.rutgers.neemi;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.rest.DownloadJobService;
import com.rutgers.neemi.util.ApplicationManager;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper helper= DatabaseHelper.getHelper(this);
        setContentView(R.layout.activity_main);

        ConnectionSource connectionSource = new AndroidConnectionSource(helper);
        RuntimeExceptionDao<PhotoTags, String> subScriptDao = helper.getPhotoTagsDao();
//        subScriptDao.queryRaw("delete from TransactionHasCategory;");
//
//        subScriptDao.queryRaw("delete from Transaction;");
//        subScriptDao.queryRaw("insert into Person values (170, null, 'Merve Yuksel' ,null, 0, null);");
//        subScriptDao.queryRaw("insert into Person values (171, null, 'Vilmoula Kala' ,null, 0, null);");
//        subScriptDao.queryRaw("insert into PhotoTags values (71, 170, 7);");
//        subScriptDao.queryRaw("insert into PhotoTags values (72, 171, 7);");






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
//                }




        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            // <---- run your one time code here
            ApplicationManager appManager = new ApplicationManager();
            appManager.initScript(helper, getApplicationContext());

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

        boolean gmailPermission = prefs.getBoolean("gmail", true);

        PersistableBundle pb = new PersistableBundle();
        pb.putBoolean("gmailPermission" , gmailPermission);

       // DataSyncJob.scheduleAdvancedJob();


//        JobRequest jobScheduler = (JobScheduler)getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
//        ComponentName componentName = new ComponentName(this,DownloadJobService.class);
//        int result;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            JobInfo jobInfoObj = new JobInfo.Builder(1, componentName)
//                    .setPeriodic(300000)
//                    .setRequiresBatteryNotLow(true)
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
//                    //.setRequiresDeviceIdle(true)
//                    .setRequiresCharging(true)
//                    .setRequiresStorageNotLow(true)
//                    //.setBackoffCriteria(3600000, JobInfo.BACKOFF_POLICY_LINEAR)
//                    .setExtras(pb).build();
//            result = jobScheduler.schedule(jobInfoObj);
//
//        }else{
//            JobInfo jobInfoObj = new JobInfo.Builder(1, componentName)
//                    .setPeriodic(300000)
//                    .setRequiresCharging(true)
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
//                    //.setRequiresDeviceIdle(true)
//                    .setBackoffCriteria(3600000, JobInfo.BACKOFF_POLICY_LINEAR)
//                    .setExtras(pb).build();
//            result = jobScheduler.schedule(jobInfoObj);
//        }
//
//        if (result == JobScheduler.RESULT_SUCCESS)
//            Log.d("Scheduler", "Job scheduled successfully!");
//        else
//            Log.d("Scheduler", "Job not scheduled successfully!");



        Intent i = getIntent();
            String key = i.getStringExtra("key");
            int items = i.getIntExtra("items", 0);

            if (key != null) {
                if (key.equalsIgnoreCase("facebook")) {
                if (items == 0) {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No facebook data fetched.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " facebook objects fetched.", Snackbar.LENGTH_LONG).show();
                }

                }
                if (key.equalsIgnoreCase("instagram")) {
                    if (items == 0) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No instagram photos fetched.", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " instagram photos fetched.", Snackbar.LENGTH_LONG).show();
                    }

                }
                if (key.equalsIgnoreCase("gcal")) {
                    if (items == 0) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No calendar events fetched.", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " calendar events fetched.", Snackbar.LENGTH_LONG).show();
                    }

                }
                if (key.equalsIgnoreCase("gmail")) {
                    if (items == 0) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No emails fetched.", Snackbar.LENGTH_LONG ).show();
                    } else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items+" emails fetched.", Snackbar.LENGTH_LONG ).show();
                    }

                }
                if (key.equalsIgnoreCase("bank")) {
//                    if (items == 0) {
//                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No financial transactions fetched.", Snackbar.LENGTH_LONG).show();
//                    } else {
//                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " financial transactions fetched.", Snackbar.LENGTH_LONG).show();
//                    }

                }
                if (key.equalsIgnoreCase("gdrive")) {
                    if (items == 0) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "No financial transactions fetched.", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), items + " financial transactions fetched.", Snackbar.LENGTH_LONG).show();
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