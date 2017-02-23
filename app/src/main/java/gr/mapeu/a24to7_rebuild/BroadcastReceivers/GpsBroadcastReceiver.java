package gr.mapeu.a24to7_rebuild.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static gr.mapeu.a24to7_rebuild.HelpfulClasses.ConnectivityCheckers.checkForGPS;
import gr.mapeu.a24to7_rebuild.Callbacks.GpsChangedListener;

public class GpsBroadcastReceiver extends BroadcastReceiver {

    final private GpsChangedListener callback;
    final private Context context;

    public GpsBroadcastReceiver(Context context) {
        this.callback = (GpsChangedListener) context;
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            callback.onGpsChanged(checkForGPS(this.context));
        }
    }


}
