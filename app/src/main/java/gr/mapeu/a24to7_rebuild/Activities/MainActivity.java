package gr.mapeu.a24to7_rebuild.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Managers.GpsManager;
import gr.mapeu.a24to7_rebuild.Callbacks.LoginCallback;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.Managers.SoapManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoginCallback {

    private String[] location = new String[2];
    private String[] credentials = new String[2];
    private String key;
    GpsManager gps;

    private ScheduledFuture cancelSend;
    private final ScheduledExecutorService schedulerSend = Executors.newScheduledThreadPool(1);
    private Runnable sendData;

    PowerManager mPowerManager;
    PowerManager.WakeLock mWakeLock;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private boolean stopSendingData = false;

    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.lat_tb);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.act_main_drawer);
        System.out.println("Drawer is: " + drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initialize();
    }

    @Override
    public void onStart(){
        super.onStart();
        cancelSend = schedulerSend.scheduleAtFixedRate(sendData, gps.getInterval(),
                gps.getInterval(), TimeUnit.SECONDS);
    }

    public void initialize() {
        sharedPreferences = getSharedPreferences(Constants.MYPREFS, MODE_PRIVATE);

        // Keep the CPU running (screen will still turn off)
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "myWakeLock");
        mWakeLock.acquire();

        //Get credentials from prefs
        credentials[0] = LoginScreen.sharedPreferences.getString(Constants.USER, null);
        credentials[1] = LoginScreen.sharedPreferences.getString(Constants.PASS, null);

        // Check if user has set a custom interval
        final int interval = sharedPreferences.getInt(Constants.INTERVAL, 60);


        Intent intent = getIntent();

        if (intent.hasExtra(Constants.KEY_EXTRA)) {
            key = intent.getStringExtra(Constants.KEY_EXTRA);
            Log.d("DEBUG", "Got key " + key);
        } else {
            // LoginScreen did not provide a key.
            // FIXME: How do we handle that?
        }
        gps = new GpsManager(interval, this);
        gps.start();

        // Send data every <interval>
        final Handler handler = new Handler();
        sendData = new Runnable() {
            @Override
            public void run() {
                if (!stopSendingData) {
                    String lat = gps.getLatitude();
                    String lng = gps.getLongitude();
                    if (lat != null && lng != null) {
                        double[] location = new double[]{Double.valueOf(lat), Double.valueOf(lng)};
                        SoapManager sManager = new SoapManager(credentials, location,
                                key, MainActivity.this);
                        sManager.gpsService();
                    }
                    handler.postDelayed(this, interval * 1000);
                }
            }
        };
        sendData.run();
    }

    public static void logOut() {

    }

    //Navigation drawer stuff
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.act_main_drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lat_long_drawer, menu);
        return true;
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
            disconnect();
        } else if (id == R.id.barcode_scanner) {
            new IntentIntegrator(this).initiateScan();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.act_main_drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Ακυρώθηκε", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void disconnect() {
        stopSendingData = true;
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

    /*
     * NOT used
     */
    @Override
    public void loginHandler(int code, String key) {}

    @Override
    public void logoutHandler(int code) {
        if (code == Constants.ERROR_RELOG) {
            logOut();
        } else if (code == Constants.ERROR_UNKNOWN){
            Log.d("DEBUG", "Error Unknown");
        }
    }
}