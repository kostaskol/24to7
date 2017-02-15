package gr.mapeu.a24to7_rebuild.Etc;


public class Constants {
    public static final String URL = "http://mapgr.eu/GPSService/GPSService.svc?wsdl";
    public static final String NAMESPACE = "http://tempuri.org/";

    public static final String METHOD_LOGIN = "GPSServiceLogin";
    public static final String SOAP_ACTION_LOGIN = "http://tempuri.org/IGPSService/GPSServiceLogin";

    public static final String METHOD_SERVICE = "GPSService";
    public static final String SOAP_ACTION_SERVICE = "http://tempuri.org/IGPSService/GPSService";

    public static final String METHOD_LIST = "GetList";
    public static final String SOAP_ACTION_LIST = "http://tempuri.org/IGPSService/GPSServiceList";

    public static final String RE_LOGIN_CODE = "777";
    public static final String CODE_SUCC = "567";
    public static final String CODE_WRONG_CRED = "566";

    public static final String KEY_EXTRA = "key_extra";

    public static final int ERROR_RELOG = 1;
    public static final int ERROR_UNKNOWN = 2;
    public static final int ERROR_WRONG_CRED = 3;
    public static final int NO_ERROR = 0;
    public static final int ERROR_INV_FORM = 4;
    public static final int ERROR_MALFORMED_ROUTE_NUMBER = 5;

    public static final String MYPREFS = "my_prefs";
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String INTERVAL = "interval";

    public static final int MIN_INTERVAL = 1000;

    public static final int SECONDS = 1000;


    //DataBase
    public static final String DB_NAME = "package_data.db";
    public static final String TABLE_NAME = "packages";
    public static final String COL_PHARM = "pharmacy_code";
    public static final String COL_PROD = "product_code";
}
