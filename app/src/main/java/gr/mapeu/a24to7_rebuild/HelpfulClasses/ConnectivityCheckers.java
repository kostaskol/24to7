package gr.mapeu.a24to7_rebuild.HelpfulClasses;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityCheckers {

    public static boolean checkForGPS(Context context) {
        final LocationManager manager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean checkForNetwork(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo active = connectivityManager.getActiveNetworkInfo();
        return active != null;
    }
}
