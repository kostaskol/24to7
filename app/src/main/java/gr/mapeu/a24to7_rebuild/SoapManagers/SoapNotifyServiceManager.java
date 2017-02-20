package gr.mapeu.a24to7_rebuild.SoapManagers;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class SoapNotifyServiceManager {
    private final String user;
    private final String key;
    private final SharedPreferences sharedPreferences;

    public SoapNotifyServiceManager(String user, String key, Context context) {
        this.user = user;
        this.key = key;
        this.sharedPreferences =
                context.getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);
    }

    public void call() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void ... params) {
                String url = sharedPreferences.getString(Constants.PREF_URL, null);

                if (url == null) {
                    Log.e("SoapManagerNotify", "url is null");
                    return null;
                }

                SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_NOTIFY);

                request.addProperty("UserName", user);
                request.addProperty("pKey", key);


                SoapSerializationEnvelope envelope =
                        new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);

                HttpTransportSE transportSE = new HttpTransportSE(url);
                try {
                    transportSE.call(Constants.SOAP_ACTION_NOTIFY, envelope);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
