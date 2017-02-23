package gr.mapeu.a24to7_rebuild.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static gr.mapeu.a24to7_rebuild.HelpfulClasses.ConnectivityCheckers.checkForNetwork;

import gr.mapeu.a24to7_rebuild.Callbacks.NetworkChangedListener;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

    final private Context context;
    final private NetworkChangedListener callback;

    public NetworkBroadcastReceiver(Context context) {
        this.context = context;
        this.callback = (NetworkChangedListener) context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.callback.onNetworkChanged(checkForNetwork(this.context));
    }



}
