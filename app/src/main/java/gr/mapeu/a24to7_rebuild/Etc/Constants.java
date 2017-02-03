package gr.mapeu.a24to7_rebuild.Etc;


public class Constants {
    public static final String URL = "http://mapgr.eu/GPSService/GPSService.svc?wsdl";
    public static final String NAMESPACE = "http://tempuri.org/";

    public static final String METHOD_LOGIN = "GPSServiceLogin";
    public static final String SOAP_ACTION_LOGIN = "http://tempuri.org/IGPSService/GPSServiceLogin";

    public static final String METHOD_SERVICE = "GPSService";
    public static final String SOAP_ACTION_SERVICE = "http://tempuri.org/IGPSService/GPSServiceLogin";

    public static final String RE_LOGIN_CODE = "777";
    public static final String CODE_SUCC = "567";
    public static final String CODE_WRONG_CRED = "566";

    public static final String KEY_EXTRA = "key_extra";

    public static final int ERROR_RELOG = 1;
    public static final int ERROR_UNKNOWN = 2;
    public static final int ERROR_WRONG_CRED = 3;
    public static final int ERROR_NO_ERROR = 0;

    public static final String MYPREFS = "my_prefs";
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String INTERVAL = "interval";

    public static final int MIN_INTERVAL = 1000;

    public static final int SECONDS = 1000;
}
