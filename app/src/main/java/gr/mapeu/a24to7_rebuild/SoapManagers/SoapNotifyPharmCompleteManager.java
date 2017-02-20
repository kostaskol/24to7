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

public class SoapNotifyPharmCompleteManager {
    private final String user;
    private final String key;
    private final String pharmCode;
    private final SharedPreferences sharedPreferences;

    public SoapNotifyPharmCompleteManager(String user, String key, String code, Context context) {
        this.user = user;
        this.key = key;
        this.pharmCode = code;
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
                    Log.e("SoapManagerNotifyPharm", "url is null");
                    return null;
                }

                int pCode = -1;
                try {
                    pCode = Integer.parseInt(pharmCode);
                } catch (Exception nfe) {
                    nfe.printStackTrace();
                }

                SoapObject request =
                        new SoapObject(Constants.NAMESPACE, Constants.METHOD_NOTIFY_PHARM);
                request.addProperty("UserName", user);
                request.addProperty("pKey", key);
                request.addProperty("PharmacyID", pCode);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);

                HttpTransportSE transportSE = new HttpTransportSE(url);
                try {
                    Log.d("NotifyPharm", "Calling with: UserName: " + user + " pKey: " + key +
                        " Pharm ID: " + pCode);
                    transportSE.call(Constants.SOAP_ACTION_NOTIFY_PHARM, envelope);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("NotifyPharm", "Sent data");
                return null;
            }
        }.execute();
    }
}
