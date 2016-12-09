package gr.mapeu.a24to7_rebuild;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

//TODO! create disconnect()
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

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.lat_tb);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        cancelSend = schedulerSend.scheduleAtFixedRate(sendData,gps.getInterval(),gps.getInterval(), TimeUnit.SECONDS);
    }

    public void initialize() {
        sharedPreferences = getSharedPreferences(Constants.MYPREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mWakeLock.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My tag");

        //Get credentials from prefs
        credentials[0] = LoginScreen.sharedPreferences.getString(Constants.USER, null);
        credentials[1] = LoginScreen.sharedPreferences.getString(Constants.PASS, null);

        int interval = sharedPreferences.getInt(Constants.INTERVAL, 60);
        if (!(interval > 0)) {
            interval = 30;
        }
        Intent intent = getIntent();
        key = intent.getStringExtra(Constants.KEY_EXTRA);
        gps = new GpsManager(interval);
        gps.startLocationUpdates();
        sendData = new Runnable() {
            @Override
            public void run() {
                double[] location = gps.getLocation();
                SoapManager sManager = new SoapManager(credentials,location,key);
                sManager.gpsService();
            }
        };
    }


    public static void errorRetriever(int code){
        if (code == Constants.ERROR_RELOG){

            logOut();
        }
        else if(code == Constants.ERROR_UNKNOWN){
            Toast.makeText(MainActivity.mContext, "Υπήρξε πρόβλημα στην αποστολή δεδομένων " +
                    "(έχετε ενεργή σύνδεση στο internet;", Toast.LENGTH_LONG).show();
        }
    }

    public static void logOut(){

    }

    //Google stuff
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lat_long_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, settings.class);
            intent.putExtra("cred", credentials);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.credits) {
            Intent intent = new Intent(this, credits.class);
            intent.putExtra("cred", credentials);
            startActivity(intent);
        } else if (id == R.id.exit) {
            disconnect();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private  void checkForNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
}