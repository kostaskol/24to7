package gr.mapeu.a24to7_rebuild.SoapManagers;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;
import gr.mapeu.a24to7_rebuild.Callbacks.ListResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class SoapListServiceManager {
    private final String user;
    private final String key;
    private ListResponseHandler callback;
    final private SharedPreferences sharedPreferences;
    final private boolean debug;

    public SoapListServiceManager(String user, String key, Context context) {
        this.user = user;
        this.key = key;
        this.sharedPreferences =
                context.getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);
        this.debug = sharedPreferences.getBoolean(Constants.PREF_DEBUG, false);
    }

    public void setCallback(ListResponseHandler callback) {
        this.callback = callback;
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
                    Log.e("SoapListService", "URL is null (why?)");
                    return null;
                }
                if (!debug) {
                    SoapObject response;
                    try {
                        SoapObject request = new SoapObject(Constants.NAMESPACE,
                                Constants.METHOD_LIST);
                        request.addProperty("UserName", user);
                        request.addProperty("pKey", key);


                        SoapSerializationEnvelope envelope =
                                new SoapSerializationEnvelope(SoapEnvelope.VER11);

                        envelope.dotNet = true;
                        envelope.setOutputSoapObject(request);

                        HttpTransportSE transportSE = new HttpTransportSE(url);
                        transportSE.call(Constants.SOAP_ACTION_LIST, envelope);

                        // Get response and split it appropriately
                        response = (SoapObject) envelope.getResponse();
                        String routeNumber = response.getPropertyAsString("Shift");
                        // TODO: if route number is -1, there is no route
                        SoapObject list = (SoapObject) response.getProperty("PharmacyList");

                        if (list == null) {
                            Log.d("SoapManagerList", "Got null list. Returning");
                            return null;
                        }

                        String[] stringList = new String[list.getPropertyCount()];
                        List<ProductBundle> productList = new ArrayList<>();
                        Log.d("SoapManager", "Got response");
                        for (int i = 0; i < stringList.length; i++) {
                            Log.d("Soapmanager", "Got item: " + list.getPropertyAsString(i));
                            stringList[i] = list.getPropertyAsString(i);
                        }

                        // Assumes format of type: pharmacyCode/productCode
                        for (String tmp : stringList) {
                            String[] splitString = tmp.split("/");

                            if (splitString.length != 2) {
                                Log.d("SOAP MANAGER", "Invalid formatting");
                                callback.onListResponse(0, null, Constants.ERROR_INV_FORM);
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
                            callback.onListResponse(0, null, Constants.ERROR_MALFORMED_ROUTE_NUMBER);
                            return null;
                        }
                        callback.onListResponse(routeNum, productList, Constants.NO_ERROR);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                } else {
                    String[] list = {"1024/123456", "1024/123457"};
                    List<ProductBundle> tmpList = new ArrayList<>();
                    for (String tmp : list) {
                        String[] splitList = tmp.split("/");
                        if (splitList.length != 2) {
                            callback.onListResponse(0, null, Constants.ERROR_INV_FORM);
                            return null;
                        }

                        ProductBundle bundle = new ProductBundle(splitList[0], splitList[1]);
                        tmpList.add(bundle);
                    }

                    callback.onListResponse(1, tmpList, Constants.NO_ERROR);
                    return null;
                }
            }
        }.execute();
    }
}
