package gr.mapeu.a24to7_rebuild.Etc;


public class Constants {
    /*
    ___________________________________SOAP___________________________________
     */
    // General
    // TODO: URL is different for each pharm -_-
    public static final String URL = "http://85.73.231.120:8080/GPS24-7_Service/GPSService.svc?wsdl";
    public static final String NAMESPACE = "http://tempuri.org/";

    // Login
    public static final String METHOD_LOGIN = "GPSServiceLogin";
    public static final String SOAP_ACTION_LOGIN = "http://tempuri.org/IGPSService/GPSServiceLogin";

    // Gps Service
    public static final String METHOD_SERVICE = "GPSService";
    public static final String SOAP_ACTION_SERVICE = "http://tempuri.org/IGPSService/GPSService";

    // Get list
    public static final String METHOD_LIST = "GPSServiceList";
    public static final String SOAP_ACTION_LIST = "http://tempuri.org/IGPSService/GPSServiceList";

    // Notify completion
    public static final String METHOD_NOTIFY = "GPSServiceNotify";
    public static final String SOAP_ACTION_NOTIFY = "http://tempuri.org/IGPSService/GPSServiceNotify";

    // Returned codes
    public static final String RE_LOGIN_CODE = "777";
    public static final String CODE_SUCC = "567";
    public static final String CODE_WRONG_CRED = "566";

    /*
    ___________________________________Inter Activity Communication_________________________________
     */

    // Error codes
    public static final int ERROR_RELOG = 1;
    public static final int ERROR_UNKNOWN = 2;
    public static final int ERROR_WRONG_CRED = 3;
    public static final int NO_ERROR = 0;
    public static final int ERROR_INV_FORM = 4;
    public static final int ERROR_MALFORMED_ROUTE_NUMBER = 5;
    public static final int ERROR_NO_MORE_ROUTES = 6;

    /*
    ___________________________________Preferences___________________________________
     */
    public static final String MYPREFS = "my_prefs";
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String CURR_PHARM_CODE = "current pharm code";
    public static final String INTERVAL = "interval";
    public static final String PKEY = "pKey";

    public static final int MIN_INTERVAL = 1000;

    public static final int SECONDS = 1000;


    /*
    ___________________________________Data Base___________________________________
     */
    public static final String DB_NAME = "package_data.db";
    public static final String TABLE_NAME = "packages";
    public static final String COL_PHARM = "pharmacy_code";
    public static final String COL_PROD = "product_code";


    /*
    ___________________________________Permissions___________________________________
     */
    public static final int ACCESS_FINE_LOCATION_RESULT = 1002;


    /*
    ___________________________________Service___________________________________
     */
    // Extras
    public static final String KEY_EXTRA = "key extra";
    public static final String SERVICE_INTERVAL_EXTRA = "interval";
    public static final String CREDENTIALS_EXTRA = "creds";
    public static final String ALL_EXTRAS = "all extras";
    public static final String RELOG_EXTRA = "relog";
    public static final String STOP_SENDING_DATA = "stop";
    public static final String PERMISSIONS_EXTRA = "ask for permissions";
}
