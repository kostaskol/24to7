package gr.mapeu.a24to7_rebuild.Managers;


import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;
import gr.mapeu.a24to7_rebuild.Callbacks.ListManagerCallback;
import gr.mapeu.a24to7_rebuild.Callbacks.LoginCallback;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class SoapManager {
    private final String[] credentials;
    private  final String[] position;
    private final String[] keyArr;
    private final String key;
    private LoginCallback callback;
    private ListManagerCallback lCallback;
    private boolean debug = true;

    /*
     * GPSService constructor
     */
    public SoapManager(String[] cred, double[] pos, String key, LoginCallback callback) {
        this.credentials = cred;
        this.position = new String[2];
        this.position[0] = String.valueOf(pos[0]);
        this.position[1] = String.valueOf(pos[1]);
        this.keyArr = new String[1];
        this.keyArr[0] = key;
        this.key = null;
        this.callback = callback;
    }


    /*
     * GPSServiceLogin constructor
     */
    public SoapManager(String[] cred, LoginCallback callback) {
        this.credentials = cred;
        this.callback = callback;
        this.position = null;
        this.key = null;
        this.keyArr = null;
    }

    /*
     * GPSServiceGetList constructor
     */
    public SoapManager(String key, String user, ListManagerCallback callback) {
        this.credentials = new String[1];
        this.credentials[0] = user;
        this.callback = null;
        this.keyArr = null;
        this.position = null;
        this.key = key;
        this.lCallback = callback;
    }

    /*
     * GPSServiceNotify constructor
     */
    public SoapManager(String key, String user) {
        this.credentials = new String[1];
        this.credentials[0] = user;
        this.key = key;
        this.callback = null;
        this.lCallback = null;
        this.keyArr = null;
        this.position = null;
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
                        callback.loginHandler(Constants.ERROR_WRONG_CRED, null, cred[0], cred[1]);
                    } else {
                        callback.loginHandler(Constants.NO_ERROR, key, cred[0], cred[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.loginHandler(Constants.ERROR_UNKNOWN, null, null, null);
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
                Log.d("SOAP", "Sending Data: " + cred[0] + ", " + cred[1] + ", " +
                        pos[0] + ", " + pos[1] + "\nwith key: " + key[0]);

                String response;
                try {
                    SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_SERVICE);

                    request.addProperty("UserName", cred[0]);
                    request.addProperty("Lat", pos[0]);
                    request.addProperty("Lng", pos[1]);
                    request.addProperty("pKey", key[0]);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);

                    HttpTransportSE transportSE = new HttpTransportSE(Constants.URL);
                    transportSE.call(Constants.SOAP_ACTION_SERVICE, envelope);

                    SoapObject soapResponse = (SoapObject) envelope.getResponse();
                    response = soapResponse.toString();
                    Log.d("RESPONSE", "Got response " + response);
                    if (response.equals(Constants.RE_LOGIN_CODE)) {
                        callback.logoutHandler(Constants.ERROR_RELOG);
                    } else {
                        callback.logoutHandler(Constants.NO_ERROR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.logoutHandler(Constants.ERROR_UNKNOWN);
                }
                return null;
            }
        }.execute(this.credentials, this.position, this.keyArr);
    }

    void getProductList() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void ... params) {
                if (!debug) {
                    SoapObject response;
                    try {
                        SoapObject request = new SoapObject(Constants.NAMESPACE,
                                Constants.METHOD_LIST);
                        request.addProperty("pKey", key);
                        request.addProperty("UserName", credentials[0]);

                        SoapSerializationEnvelope envelope =
                                new SoapSerializationEnvelope(SoapEnvelope.VER11);

                        envelope.dotNet = true;
                        envelope.setOutputSoapObject(request);

                        HttpTransportSE transportSE = new HttpTransportSE(Constants.URL);
                        transportSE.call(Constants.SOAP_ACTION_LIST, envelope);

                        // Get response and split it appropriately
                        response = (SoapObject) envelope.getResponse();
                        String routeNumber = response.getPropertyAsString("Shift");
                        // TODO: if route number is -1, there is no route
                        SoapObject list = (SoapObject) response.getProperty("PharmacyList");

                        String[] stringList = new String[list.getPropertyCount()];
                        List<ProductBundle> productList = new ArrayList<>();

                        for (int i = 0; i < stringList.length; i++) {
                            stringList[i] = list.getPropertyAsString(i);
                        }

                        // Assumes format of type: pharmacyCode/productCode
                        for (String tmp : stringList) {
                            String[] splitString = tmp.split("/");

                            if (splitString.length != 2) {
                                Log.d("SOAP MANAGER", "Invalid formatting");
                                lCallback.handleList(0, null, Constants.ERROR_INV_FORM);
                                return null;
                            }

                            ProductBundle tmpBundle = new ProductBundle(splitString[0], splitString[1]);
                            productList.add(tmpBundle);
                        }

                        int routeNum;
                        try {
                            routeNum = Integer.parseInt(routeNumber);
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                            lCallback.handleList(0, null, Constants.ERROR_MALFORMED_ROUTE_NUMBER);
                            return null;
                        }
                        lCallback.handleList(routeNum, productList, Constants.NO_ERROR);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                } else {
                    String[] list = {"1024/90311017", "1024/013314"};
                    List<ProductBundle> tmpList = new ArrayList<>();
                    for (String tmp : list) {
                        String[] splitList = tmp.split("/");
                        if (splitList.length != 2) {
                            lCallback.handleList(0, null, Constants.ERROR_INV_FORM);
                            return null;
                        }

                        ProductBundle bundle = new ProductBundle(splitList[0], splitList[1]);
                        tmpList.add(bundle);
                    }

                    lCallback.handleList(1, tmpList, Constants.NO_ERROR);
                    return null;
                }
            }
        }.execute();
    }

    public void notifyCompletion() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void ... params) {
                SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_NOTIFY);
                request.addProperty("pKey", key);
                request.addProperty("UserName", credentials[0]);

                SoapSerializationEnvelope envelope =
                        new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);

                HttpTransportSE transportSE = new HttpTransportSE(Constants.URL);
                try {
                    transportSE.call(Constants.SOAP_ACTION_NOTIFY, envelope);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    // TODO: notify after each pharmacy has been completed
    // NAME: GPSServiceBaskets
    // UserName, pKey, PharmacyID
}
