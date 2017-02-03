package gr.mapeu.a24to7_rebuild.Managers;


import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import gr.mapeu.a24to7_rebuild.Callbacks.LoginCallback;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class SoapManager {
    private String[] credentials;
    private  String[] position;
    private String[] key;
    LoginCallback callback;

    public SoapManager(String[] cred, double[] pos, String key, LoginCallback callback) {
        this.credentials = cred;
        this.position = new String[2];
        this.position[0] = String.valueOf(pos[0]);
        this.position[1] = String.valueOf(pos[1]);
        this.key = new String[1];
        this.key[0] = key;
        this.callback = callback;
    }

    public SoapManager(String[] cred, LoginCallback callback) {
        this.credentials = cred;
        this.callback = callback;
    }

    public void loginService() {
        new AsyncTask<String[], Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(String[] ... params) {
                String[] cred;
                cred = params[0];

                Log.d("ASYNC", "Requesting sign in with: " + cred[0] + " | " + cred[1]);
                try {
                    SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_LOGIN);

                    request.addProperty("UserName", cred[0]);
                    request.addProperty("PassWord", cred[1]);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);

                    HttpTransportSE transportSE = new HttpTransportSE(Constants.URL);
                    transportSE.call(Constants.SOAP_ACTION_LOGIN, envelope);

                    SoapObject response = (SoapObject) envelope.getResponse();
                    String returnCode = response.getPropertyAsString("returnCode");
                    Log.d("ASYNC", "Got return code: " + returnCode);
                    String key = response.getPropertyAsString("pKey");
                    if (returnCode.equals(Constants.CODE_WRONG_CRED)) {
                        callback.loginHandler(Constants.ERROR_WRONG_CRED, null);
                    } else {
                        callback.loginHandler(Constants.ERROR_NO_ERROR, key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.loginHandler(Constants.ERROR_UNKNOWN, null);
                }
                return null;
            }
        }.execute(this.credentials);
    }

    public void gpsService() {
        new AsyncTask<String[], Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(String[] ... params) {
                String[] cred, pos, key;
                cred = params[0];
                pos = params[1];
                key = params[2];
                Log.d("SOAP", "Sending Data: " + cred[0] + ", " + cred[1]);

                String response;
                try {
                    SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_SERVICE);

                    request.addProperty("UserName", cred[0]);
                    request.addProperty("PassWord", cred[1]);
                    request.addProperty("Lat", pos[0]);
                    request.addProperty("Lng", pos[1]);
                    request.addProperty("Key", key[0]);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);

                    HttpTransportSE transportSE = new HttpTransportSE(Constants.URL);
                    transportSE.call(Constants.SOAP_ACTION_SERVICE, envelope);

                    SoapPrimitive soapResponse = (SoapPrimitive) envelope.getResponse();
                    response = soapResponse.toString();
                    Log.d("RESPONSE", "Got response " + response);
                    if (response.equals(Constants.RE_LOGIN_CODE)) {
                        callback.logoutHandler(Constants.ERROR_RELOG);
                    } else {
                        callback.logoutHandler(Constants.ERROR_NO_ERROR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.logoutHandler(Constants.ERROR_UNKNOWN);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

            }
        }.execute(this.credentials, this.position, this.key);
    }
}
