package gr.mapeu.a24to7_rebuild.Managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import gr.mapeu.a24to7_rebuild.Callbacks.GpsManagerCallback;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

/**
 * Simple manager that manages the google services location api callbacks
 */
public class GpsManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private LocationRequest mLocationRequest;
    final private GoogleApiClient mGoogleApiClient;
    private String latitude;
    private String longitude;
    final private int interval;
    final private Context mContext;
    final private GpsManagerCallback callback;
    final private boolean debug;

    public GpsManager(int interval, Context context) {
        this.interval = interval;
        this.mContext = context;
        this.callback = (GpsManagerCallback) context;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.debug =
                context.getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE)
                .getBoolean(Constants.PREF_DEBUG, false);
    }

    public void start() {
        Log.d("Gps Manager", "Connecting...");
        mGoogleApiClient.connect();
    }


    private void createLocationRequest(){
        mLocationRequest = LocationRequest.create()
                .setInterval(interval)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setSmallestDisplacement(0f);
    }

    private boolean startLocationUpdates(){
        int permissionCheck = ContextCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest();

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            return true;
        } else {
            callback.onPermissionNotGranted();
            return false;
        }
    }

    public void stopLocationUpdates(){
        if(mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi
                        .removeLocationUpdates(mGoogleApiClient, this);
            } else {
                if (mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (debug) {
            Toast.makeText(mContext, "Location changed", Toast.LENGTH_SHORT).show();
        }
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

    }

    public int getInterval() {
        return interval;
    }

    public String[] getLocation() {

        return new String[]{latitude,longitude};
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    /*
     * (For both getLatitude() and getLongitude())
     * If, for any reason, the user's current location us unavailable,
     * we request their last known location and send that to the server
     */
    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this.mContext, "A connection with the Google Location Services " +
                "client could not be established. Please try again later.", Toast.LENGTH_LONG).show();
    }
}
