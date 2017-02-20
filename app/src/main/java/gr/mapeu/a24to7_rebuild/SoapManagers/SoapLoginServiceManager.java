package gr.mapeu.a24to7_rebuild.SoapManagers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import gr.mapeu.a24to7_rebuild.Callbacks.LoginResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class SoapLoginServiceManager {
    final private String[] credentials;
    private LoginResponseHandler callback;
    final private SharedPreferences sharedPreferences;
    final private boolean debug;

    public SoapLoginServiceManager(String[] credentials, Context context) {
        this.credentials = credentials;
        this.sharedPreferences =
                context.getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);
        this.debug = sharedPreferences.getBoolean(Constants.PREF_DEBUG, false);
    }

    public void setCallback(LoginResponseHandler callback) {
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
                String url = sharedPreferences.getString(Constants.PREF_URL, null);
                if (url == null) {
                    Log.e("SoapManagerLogin", "url is null");
                    return null;
                }
                if (!debug) {
                    String[] cred;
                    cred = params[0];


                    SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_LOGIN);

                    request.addProperty("UserName", cred[0]);
                    request.addProperty("PassWord", cred[1]);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);

                    HttpTransportSE transportSE = new HttpTransportSE(url);
                    SoapObject response = null;
                    try {
                        transportSE.call(Constants.SOAP_ACTION_LOGIN, envelope);

                        response = (SoapObject) envelope.getResponse();
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onLoginResponse(Constants.ERROR_UNKNOWN, null, null, null);
                    }
                    assert response != null;
                    String returnCode = response.getPropertyAsString("returnCode");
                    Log.d("ASYNC", "Got return code: " + returnCode);
                    String key = response.getPropertyAsString("pKey");
                    if (returnCode.equals(Constants.CODE_WRONG_CRED)) {
                        callback.onLoginResponse(Constants.ERROR_WRONG_CRED, null, cred[0], cred[1]);

                    } else {
                        callback.onLoginResponse(Constants.NO_ERROR, key, cred[0], cred[1]);
                    }
                } else {
                    callback.onLoginResponse(Constants.NO_ERROR, "0123", "Kostas", "123");
                }
                return null;
            }
        }.execute(this.credentials);
    }
}
