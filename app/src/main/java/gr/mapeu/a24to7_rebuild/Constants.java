package gr.mapeu.a24to7_rebuild;


public class Constants {
    static final String URL = "";
    static final String NAMESPACE = "http://tempuri.org/";

    static final String METHOD_LOGIN = "GPSLogin";
    static final String SOAP_ACTION_LOGIN = NAMESPACE + METHOD_LOGIN;

    static final String METHOD_SERVICE = "GPSService";
    static final String SOAP_ACTION_SERVICE = NAMESPACE + METHOD_SERVICE;

    static final String RE_LOGIN_CODE = "777";
    static final String CODE_SUCC = "567";
    static final String CODE_WRONG_CRED = "566";

    static final String KEY_EXTRA = "key_extra";

    static final int ERROR_RELOG = 1;
    static final int ERROR_UNKNOWN = 2;
    static final int ERROR_WRONG_CRED = 3;
    static final int ERROR_NO_ERROR = 0;

    static final String MYPREFS = "my_prefs";
    static final String USER = "username";
    static final String PASS = "password";
    static final String INTERVAL = "interval";

    static final int MILF = 1000;

}
