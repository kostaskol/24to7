package gr.mapeu.a24to7_rebuild.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import gr.mapeu.a24to7_rebuild.Callbacks.GpsManagerCallback;
import gr.mapeu.a24to7_rebuild.Callbacks.ListResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.HelpfulClasses.AlertBuilder;
import gr.mapeu.a24to7_rebuild.Managers.ListManager;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.Services.GpsSender;

public class MainDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ListResponseHandler {

    private String[] credentials = new String[2];
    private String key;

    PowerManager mPowerManager;
    PowerManager.WakeLock mWakeLock;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initialize();
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(relog,
                new IntentFilter("relog"));
    }

    public void initialize() {
        sharedPreferences = getSharedPreferences(Constants.MYPREFS, MODE_PRIVATE);

        // Keep the CPU running (screen will still turn off)
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myWakeLock");
        mWakeLock.acquire();

        //Get credentials from prefs
        credentials[0] = LoginScreen.sharedPreferences.getString(Constants.USER, null);
        credentials[1] = LoginScreen.sharedPreferences.getString(Constants.PASS, null);

        // Check if user has set a custom interval
        final int interval = sharedPreferences.getInt(Constants.INTERVAL, 60);

        key = sharedPreferences.getString(Constants.PKEY, null);
        if (key == null) {
            // How did we not get a key from login screen?
            Log.e("Main screen", "No key exists!");
            return;
        }

        Intent serviceIntent = new Intent(this, GpsSender.class);
        serviceIntent
                .putExtra(Constants.KEY_EXTRA, key)
                .putExtra(Constants.CREDENTIALS_EXTRA, credentials)
                .putExtra(Constants.SERVICE_INTERVAL_EXTRA, interval)
                .putExtra(Constants.ALL_EXTRAS, "");

        startService(serviceIntent);



        final Button getListBtn = (Button) findViewById(R.id.get_list_btn);

        getListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListBtn.setEnabled(false);
                final String user = sharedPreferences.getString(Constants.USER, null);
                if (user == null) {
                    // huh?
                    Log.e("MAIN SCREEN", "Could not find username (???)");
                    return;
                }
                DatabaseManager dbManager = new DatabaseManager(MainDrawerActivity.this);
                int rem = dbManager.remainingOverall();
                if (rem != 0) {
                    String title = "Οι παραγγελίες δεν έχουν ολοκληρωθεί";
                    String message = "Απομένουν " + rem + " παραγγελίες. Είσαστε σίγουρος πως " +
                            "θέλετε να ανανεώσετε την λίστα; " +
                            "(Οι προηγούμενες παραγγελίες θα διαγραφούν)";
                    DialogInterface.OnClickListener positive =
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ListManager lManager =
                                    new ListManager(MainDrawerActivity.this, key, user);
                            lManager.deleteList();
                            lManager.getList();
                        }
                    };

                    DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    };
                    AlertBuilder alert = new AlertBuilder(MainDrawerActivity.this,
                            message, title, positive, negative);
                    alert.showDialog();
                }
            getListBtn.setEnabled(true);
            }
        });

        final Button newPharm = (Button) findViewById(R.id.new_scan_btn);

        newPharm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPharm.setEnabled(false);
                startActivity(new Intent(MainDrawerActivity.this, NewPharmacy.class));
                newPharm.setEnabled(true);
            }
        });

        Button printListBtn = (Button) findViewById(R.id.print_list_btn);
        printListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbManager = new DatabaseManager(MainDrawerActivity.this);
                dbManager.printList();
            }
        });
    }

    public void logOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER, null);
        editor.putString(Constants.PASS, null);
        editor.apply();
        stopSending();
        finishAffinity();
    }

    //Navigation drawer stuff
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /*Intent intent = new Intent(this, settings.class);
            intent.putExtra("cred", credentials);
            startActivity(intent);*/
        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.credits) {
            /*Intent intent = new Intent(this, credits.class);
            intent.putExtra("cred", credentials);
            startActivity(intent);*/
        } else if (id == R.id.exit) {
            exit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void exit() {
        stopSending();
        finishAffinity();
    }

    private void stopSending() {
        Intent intent = new Intent("stop");
        intent.putExtra(Constants.STOP_SENDING_DATA, "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void checkForNetwork() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = connectivityManager.getActiveNetworkInfo();
        if (active == null) {
            showProblemBar(R.id.internet_bar);
        } else {
            hideProblemBar(R.id.internet_bar);
        }
    }

    protected  void showProblemBar(int ID) {
        LinearLayout internet = (LinearLayout) findViewById(ID);
        if (internet.getVisibility() == View.GONE) {
            Animation in = AnimationUtils.loadAnimation(this, R.anim.problem_alert_in);
            internet.startAnimation(in);
            internet.setVisibility(View.VISIBLE);
        }
    }

    protected void hideProblemBar(int ID) {
        LinearLayout internet = (LinearLayout) findViewById(ID);
        if (internet.getVisibility() == View.VISIBLE) {
            Animation out = AnimationUtils.loadAnimation(this, R.anim.problem_alert_out);
            internet.startAnimation(out);
            internet.setVisibility(View.GONE);
        }
    }

    @Override
    public void onListResponse(int returnCode) {
        switch (returnCode) {
            case Constants.ERROR_INV_FORM:
                // TODO: Do some stuff here
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainDrawerActivity.this, "Could not retrieve list", Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case Constants.ERROR_MALFORMED_ROUTE_NUMBER:
                // TODO: Do some other stuff here
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainDrawerActivity.this, "Could not retrieve list", Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case Constants.NO_ERROR:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainDrawerActivity.this, "List retrieved successfully",
                                Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case Constants.ERROR_NO_MORE_ROUTES:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainDrawerActivity.this, "There are no more routes at" +
                                " this moment. Try again later", Toast.LENGTH_LONG).show();
                    }
                });
            default:
                // Wut?
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                          int[] grantResults) {
        if (requestCode == Constants.ACCESS_FINE_LOCATION_RESULT) {
            if (grantResults.length == 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Αυτή η εφαρμογή πρέπει να γνωρίζει την τοποθεσία σας.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                Constants.ACCESS_FINE_LOCATION_RESULT);
    }


    private BroadcastReceiver relog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.RELOG_EXTRA)) {
                relogin();
            } else {
                requestPermission();
            }
        }
    };

    private void relogin() {
        // TODO: Handle this
    }


}
