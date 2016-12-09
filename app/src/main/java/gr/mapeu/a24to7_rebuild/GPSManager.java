package gr.mapeu.a24to7_rebuild;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GpsManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    LocationRequest mLocationRequest;
    static GoogleApiClient mGoogleApiClient;
    protected double latitude;
    protected double longitude;
    protected int interval;

    public GpsManager(int interval) {
        this.interval = interval;
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval*Constants.MILF)
                .setFastestInterval((interval-20)*Constants.MILF)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates(){
        LocationServices.FusedLocationApi
                .removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void stopLocationUpdates(){
        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.mContext, "Η σύνδεση με το Google Location Services ήταν ανεπιτυχής. " +
                "Παρακαλώ προσπαθήστε αργότερα.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public int getInterval() {
        return interval;
    }

    public double[] getLocation() {

        return new double[]{latitude,longitude};
    }
}
