package gr.mapeu.a24to7_rebuild.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import gr.mapeu.a24to7_rebuild.BroadcastReceivers.GpsBroadcastReceiver;
import gr.mapeu.a24to7_rebuild.BroadcastReceivers.NetworkBroadcastReceiver;
import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;
import gr.mapeu.a24to7_rebuild.Callbacks.GpsChangedListener;
import gr.mapeu.a24to7_rebuild.Callbacks.ListManagerCallback;
import gr.mapeu.a24to7_rebuild.Callbacks.NetworkChangedListener;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Managers.ListManager;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.Services.GpsSender;

public class MainDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ListManagerCallback, NetworkChangedListener, GpsChangedListener {

    private String[] credentials = new String[2];
    private String key;

    private SharedPreferences sharedPreferences;

    private boolean shiftStarted;
    private boolean serviceStarted;

    private GpsBroadcastReceiver gpsReceiver;
    private NetworkBroadcastReceiver networkReceiver;

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
                new IntentFilter("service"));

        createListeners();

    }

    @Override
    public void onStart() {
        super.onStart();
        networkReceiver = new NetworkBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);

        gpsReceiver = new GpsBroadcastReceiver(this);
        filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(networkReceiver);
        unregisterReceiver(gpsReceiver);
    }


    public void initialize() {
        sharedPreferences = getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);

        // Keep the CPU running (screen will still turn off)
        PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myWakeLock");
        mWakeLock.acquire();

        //Get credentials from prefs
        credentials[0] = LoginScreen.sharedPreferences.getString(Constants.PREF_USER, null);
        credentials[1] = LoginScreen.sharedPreferences.getString(Constants.PREF_PASS, null);

        key = sharedPreferences.getString(Constants.PREF_PKEY, null);
        if (key == null) {
            // How did we not get a key from login screen?
            Log.e("Main screen", "No key exists!");
            return;
        }

        shiftStarted = sharedPreferences.getBoolean(Constants.PREF_SHIFT_STARTED, false);
        serviceStarted = sharedPreferences.getBoolean(Constants.PREF_SERVICE_STARTED, false);


    }

    private void createListeners() {

        // Check if user has set a custom interval
        final int interval = sharedPreferences.getInt(Constants.PREF_INTERVAL, 60);

        final ImageButton getListBtn = (ImageButton) findViewById(R.id.new_route_btn);

        getListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListBtn.setEnabled(false);
                final String user = sharedPreferences.getString(Constants.PREF_USER, null);
                if (user == null) {
                    // huh?
                    Log.e("MAIN SCREEN", "Could not find username (???)");
                    return;
                }

                ListManager lManager =
                        new ListManager(MainDrawerActivity.this, key, user);
                lManager.getList();
                getListBtn.setEnabled(true);
            }
        });

        final ImageButton newPharm = (ImageButton) findViewById(R.id.new_pharm_btn);

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

        final ImageButton startShift = (ImageButton) findViewById(R.id.start_shift_btn);

        if (shiftStarted) {
            startShift.setImageResource(R.drawable.end_shift_icon);
            if (!serviceStarted) {
                Intent serviceIntent = new Intent(MainDrawerActivity.this, GpsSender.class);
                serviceIntent
                        .putExtra(Constants.KEY_EXTRA, key)
                        .putExtra(Constants.CREDENTIALS_EXTRA, credentials)
                        .putExtra(Constants.SERVICE_INTERVAL_EXTRA, interval)
                        .putExtra(Constants.ALL_EXTRAS, "");

                startService(serviceIntent);

            }
        } else {
            startShift.setImageResource(R.drawable.start_shift_icon);
        }

        startShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startShift.setEnabled(false);
                if (!shiftStarted) {
                    shiftStarted = true;
                    serviceStarted = true;
                    Intent serviceIntent = new Intent(MainDrawerActivity.this, GpsSender.class);
                    serviceIntent
                            .putExtra(Constants.KEY_EXTRA, key)
                            .putExtra(Constants.CREDENTIALS_EXTRA, credentials)
                            .putExtra(Constants.SERVICE_INTERVAL_EXTRA, interval)
                            .putExtra(Constants.ALL_EXTRAS, "");

                    startService(serviceIntent);



                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.PREF_SHIFT_STARTED, true);
                    editor.putBoolean(Constants.PREF_SERVICE_STARTED, true);
                    editor.apply();

                    startShift.setImageResource(R.drawable.end_shift_icon);
                } else {
                    shiftStarted = false;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.PREF_SHIFT_STARTED, false);
                    editor.putBoolean(Constants.PREF_SERVICE_STARTED, false);
                    editor.apply();
                    startShift.setImageResource(R.drawable.start_shift_icon);
                    stopSending();
                }
                startShift.setEnabled(true);
            }
        });

        Button deleteDbBtn = (Button) findViewById(R.id.delete_db_btn);
        deleteDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListManager listManager = new ListManager(MainDrawerActivity.this);
                listManager.deleteList();
            }
        });
    }

    private void logOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_USER, null);
        editor.putString(Constants.PREF_PASS, null);
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

        if (id == R.id.settings) {
            startActivity(new Intent(this, ActivityPreference.class));
        } else if (id == R.id.exit) {
            exit();
        } else if (id == R.id.log_out) {
            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void exit() {
        if (shiftStarted) {
            String message = "Η τοποθεσία της συσκευής συνεχίζει να στέλνεται ακόμη και " +
                    "όταν η εφαρμογή είναι κλειστή. Θέλετε να σταματήσετε τώρα την αποστολή τοπθεσίας;" +
                    "(Μπορείτε να σταματήσετε την αποστολή " +
                    "μόνοι σας πατώντας το κουμπί \"ΤΕΛΟΣ ΒΑΡΔΙΑΣ\")";
            String title = "Προσοχή";
            DialogInterface.OnClickListener stop = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopSending();
                    stopSending();
                    finishAffinity();
                }
            };

            DialogInterface.OnClickListener dontStop = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            };

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(title);
            alert.setMessage(message);
            alert.setPositiveButton("Τέλος", stop);
            alert.setNeutralButton("Ακύρωση", null);
            alert.setNegativeButton("Κλείσιμο", dontStop);
            alert.show();
        } else {
            finishAffinity();
        }

    }

    private void stopSending() {
        stopService(new Intent(this, GpsSender.class));
    }

    @Override
    public void onListAcquiredResponse(int returnCode) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                          @NonNull int[] grantResults) {
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
            Log.d("MainDrawer", "Received");
            if (intent.hasExtra(Constants.RELOG_EXTRA)) {
                relogin();
            } else {
                requestPermission();
            }
        }
    };

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

    private void relogin() {
        Log.d("MainDrawer", "Reloging in");
        // TODO: Handle this
    }

    @Override
    public void onGpsChanged(boolean status) {
        Log.d("MainDrawer", "Gps Changed");
        if (!status) {
            showProblemBar(R.id.gps_bar);
        } else {
            hideProblemBar(R.id.gps_bar);
        }
    }

    @Override
    public void onNetworkChanged(boolean status) {
        if (!status) {
            showProblemBar(R.id.internet_bar);
        } else {
            hideProblemBar(R.id.internet_bar);
        }
    }
}
