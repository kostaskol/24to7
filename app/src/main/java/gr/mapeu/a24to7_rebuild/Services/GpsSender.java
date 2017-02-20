package gr.mapeu.a24to7_rebuild.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import gr.mapeu.a24to7_rebuild.Callbacks.GpsManagerCallback;
import gr.mapeu.a24to7_rebuild.Callbacks.GpsSenderResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Managers.GpsManager;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapGpsServiceManager;

public class GpsSender extends Service
        implements GpsSenderResponseHandler, GpsManagerCallback {

    private boolean stopSendingData = false;
    private GpsManager gpsManager;
    private String[] credentials;
    private String key;
    private int interval;

    @Override
    public void onCreate() {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.hasExtra(Constants.ALL_EXTRAS)) {
            this.credentials = intent.getStringArrayExtra(Constants.CREDENTIALS_EXTRA);
            this.key = intent.getStringExtra(Constants.KEY_EXTRA);
            this.interval = intent.getIntExtra(Constants.SERVICE_INTERVAL_EXTRA, 30);
        } else {
            // Why do I have no extras?
            return START_NOT_STICKY;
        }

        this.gpsManager = new GpsManager(this.interval * 1000, this);
        this.gpsManager.start();

        // Send data every <interval>
        final Handler handler = new Handler();
        Runnable sendData = new Runnable() {
            @Override
            public void run() {
                if (!stopSendingData) {
                    Log.d("GpsSender", "Still alive bitches!!");
                    String lat = gpsManager.getLatitude();
                    String lng = gpsManager.getLongitude();
                    if (lat != null && lng != null) {
                        double[] location = new double[]{Double.valueOf(lat), Double.valueOf(lng)};
                        SoapGpsServiceManager sManager =
                                new SoapGpsServiceManager(
                                        credentials,
                                        location,
                                        key,
                                        GpsSender.this);

                        sManager.setCallback(GpsSender.this);
                        sManager.gpsService();
                    }
                    handler.postDelayed(this, interval * 1000);
                } else {
                    stopSelf();
                }
            }
        };

        sendData.run();

        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        this.stopSendingData = true;
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d("GpsSender", "On destroy called");
        super.onDestroy();
        this.stopSendingData = true;
    }


    @Override
    public void onGpsServiceResponse(int code) {
        if (code == Constants.ERROR_RELOG) {
            Intent intent = new Intent("service");
            intent.putExtra(Constants.RELOG_EXTRA, "relog");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onPermissionNotGranted() {
        Intent intent = new Intent("service");
        intent.putExtra(Constants.PERMISSIONS_EXTRA, "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
