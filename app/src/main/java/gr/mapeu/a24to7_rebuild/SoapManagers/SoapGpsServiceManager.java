package gr.mapeu.a24to7_rebuild.SoapManagers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import gr.mapeu.a24to7_rebuild.Callbacks.GpsSenderResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class SoapGpsServiceManager {

    private final String[] credentials;
    private final String[] position;
    private final String[] keyArr;
    private final SharedPreferences sharedPreferences;
    private GpsSenderResponseHandler callback;

    public SoapGpsServiceManager(String[] credentials,
                          double[] position, String key, Context context) {
        this.credentials = credentials;
        this.position = new String[2];
        this.position[0] = String.valueOf(position[0]);
        this.position[1] = String.valueOf(position[1]);
        this.keyArr = new String[1];
        this.keyArr[0] = key;
        this.sharedPreferences =
                context.getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);
        Log.d("GpsService", "Created");
    }

    public void setCallback(GpsSenderResponseHandler callback) {
        Log.d("GpsService", "Callback set");
        this.callback = callback;
    }

    public void call() {
        new AsyncTask<String[], Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(String[] ... params) {
                Log.d("GpsService", "Async task called");
                String[] cred, pos, key;
                cred = params[0];
                pos = params[1];
                key = params[2];
                Log.d("SOAP", "Sending Data: " + cred[0] + ", " + cred[1] + ", " +
                        pos[0] + ", " + pos[1] + "\nwith key: " + key[0]);

                String url = sharedPreferences.getString(Constants.PREF_URL, null);

                if (url == null) {
                    Log.e("SoapGpsService", "url is null");
                    return null;
                }

                String response;
                try {
                    SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_SERVICE);

                    request.addProperty("UserName", cred[0]);
                    request.addProperty("pKey", key[0]);
                    request.addProperty("Lat", pos[0]);
                    request.addProperty("Lng", pos[1]);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);

                    // HttpTransportSE transportSE = new HttpTransportSE("http://87.203.78.43:8080/GPS24-7_Service/GPSService.svc?wsdl");
                    HttpTransportSE transportSE = new HttpTransportSE(url);

                    transportSE.call(Constants.SOAP_ACTION_SERVICE, envelope);

                    SoapObject soapResponse = (SoapObject) envelope.getResponse();
                    response = soapResponse.toString();
                    Log.d("RESPONSE", "Got response " + response);
                    if (response.equals(Constants.RE_LOGIN_CODE)) {
                        callback.onGpsServiceResponse(Constants.ERROR_RELOG);
                    } else {
                        callback.onGpsServiceResponse(Constants.NO_ERROR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onGpsServiceResponse(Constants.ERROR_UNKNOWN);
                }
                return null;
            }
        }.execute(this.credentials, this.position, this.keyArr);
    }
}
