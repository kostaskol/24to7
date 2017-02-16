package gr.mapeu.a24to7_rebuild.Services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import gr.mapeu.a24to7_rebuild.Callbacks.GpsManagerCallback;
import gr.mapeu.a24to7_rebuild.Callbacks.LoginCallback;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Managers.GpsManager;
import gr.mapeu.a24to7_rebuild.Managers.SoapManager;

public class GpsSender extends Service implements LoginCallback, GpsManagerCallback {

    private boolean stopSendingData = false;
    private GpsManager gpsManager;
    private String[] credentials;
    private String key;
    private int interval;


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

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("stop"));

        // Send data every <interval>
        final Handler handler = new Handler();
        Runnable sendData = new Runnable() {
            @Override
            public void run() {
                if (!stopSendingData) {
                    String lat = gpsManager.getLatitude();
                    String lng = gpsManager.getLongitude();
                    if (lat != null && lng != null) {
                        double[] location = new double[]{Double.valueOf(lat), Double.valueOf(lng)};
                        SoapManager sManager = new SoapManager(credentials, location,
                                key, GpsSender.this);

                        sManager.gpsService();
                    }
                    handler.postDelayed(this, interval * 1000);
                }
            }
        };
        sendData.run();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void loginHandler(int code, String key, String user, String pass) {}

    @Override
    public void logoutHandler(int code) {
        Intent intent = new Intent("service");
        intent.putExtra(Constants.RELOG_EXTRA, "relog");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Service", "Got stop message. Stopping");
            if (intent.hasExtra(Constants.STOP_SENDING_DATA)) {
                stopSendingData = true;
            }
        }
    };

    @Override
    public void onPermissionNotGranted() {
        Intent intent = new Intent("service");
        intent.putExtra(Constants.PERMISSIONS_EXTRA, "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
