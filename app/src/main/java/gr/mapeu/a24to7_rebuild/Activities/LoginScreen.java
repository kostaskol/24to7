package gr.mapeu.a24to7_rebuild.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import gr.mapeu.a24to7_rebuild.BroadcastReceivers.NetworkBroadcastReceiver;
import gr.mapeu.a24to7_rebuild.Callbacks.ButtonCallbacks;
import gr.mapeu.a24to7_rebuild.Callbacks.LoginResponseHandler;
import gr.mapeu.a24to7_rebuild.Callbacks.NetworkChangedListener;
import gr.mapeu.a24to7_rebuild.Etc.Animations;
import gr.mapeu.a24to7_rebuild.Callbacks.AnimationCallbacks;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.HelpfulClasses.AlertBuilder;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapLoginServiceManager;
import static gr.mapeu.a24to7_rebuild.HelpfulClasses.ConnectivityCheckers.checkForNetwork;

public class LoginScreen extends AppCompatActivity implements LoginResponseHandler,
        NetworkChangedListener {

    Context loginContext;
    static private int REQUEST_CODE_RECOVERY_PLAY_SERVICES = 200;


    public ImageButton logIn;
    public ImageButton next;
    public ImageButton back;
    public ImageButton linkToPage;

    public LinearLayout passLayout;
    public LinearLayout userLayout;

    public Animation passOff;
    public Animation passOn;
    public Animation userOff;
    public Animation userOn;
    public Animation logOn;

    public EditText userEdit;
    public EditText passEdit;

    public Button hideUserKeyboard;
    public Button hidePassKeyboard;

    public ProgressBar progressSpinner;

    public Context loginScreenContext;

    static SharedPreferences sharedPreferences;

    public ButtonCallbacks buttonCallbacks;
    public AnimationCallbacks animationCallbacks;

    private NetworkBroadcastReceiver networkReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        loginContext = this;

        initialise();
        createListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Create Network broadcast receiver
        networkReceiver = new NetworkBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void initialise() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Initialize all views

        //Image Buttons
        logIn = (ImageButton) findViewById(R.id.logIn);
        next = (ImageButton) findViewById(R.id.moveToPass);
        back = (ImageButton) findViewById(R.id.returnToUser);
        linkToPage = (ImageButton) findViewById(R.id.link);

        //Animations
        passOff = AnimationUtils.loadAnimation(this, R.anim.passoffscreen);
        passOn = AnimationUtils.loadAnimation(this, R.anim.passtoscreen);
        userOff = AnimationUtils.loadAnimation(this,R.anim.useroffscreen);
        logOn = AnimationUtils.loadAnimation(this, R.anim.logintoscreen);
        userOn = AnimationUtils.loadAnimation(this, R.anim.useronscreen);

        //Progress Bar
        progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        progressSpinner.setVisibility(View.GONE);

        //Layouts
        passLayout = (LinearLayout) findViewById(R.id.passWord);
        userLayout = (LinearLayout) findViewById(R.id.userName);

        //Edit text views
        userEdit = (EditText) findViewById(R.id.log_user);
        passEdit = (EditText) findViewById(R.id.log_pwd);

        //Buttons
        hideUserKeyboard = (Button) findViewById(R.id.hideUserKeyboard);
        hidePassKeyboard = (Button) findViewById(R.id.hidePassKeyboard);

        //Hide buttons
        hideUserKeyboard.setVisibility(View.GONE);
        hidePassKeyboard.setVisibility(View.GONE);

        //Initialise ActivityPreference
        sharedPreferences = getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);

        //Prompt user to install/update Google Services
        checkForServices();
        //Check if credentials exist -> Automatically sign user in
        checkCredentials();
    }

    void createListeners() {
        buttonCallbacks = new ButtonCallbacks(this);
        animationCallbacks = new AnimationCallbacks(this);

        //Initialize ALL Animation and Button listeners
        Animations.passDisappear(passLayout, logIn, back);

        hideUserKeyboard.setOnClickListener(buttonCallbacks.hideKeyboardListener);

        hidePassKeyboard.setOnClickListener(buttonCallbacks.hideKeyboardListener);

        next.setOnClickListener(buttonCallbacks.nextListener);

        passOn.setAnimationListener(animationCallbacks.passOnAnimation);

        userOn.setAnimationListener(animationCallbacks.userOnAnimation);

        userOff.setAnimationListener(animationCallbacks.userOffAnimation);

        back.setOnClickListener(buttonCallbacks.backListener);

        linkToPage.setOnClickListener(buttonCallbacks.linkToPageListener);

        logIn.setOnClickListener(buttonCallbacks.loginListener);
        //End Listeners
    }

    /*
     * Checks for Google play services availability
     */
    private void checkForServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int checkGoogleServices = availability.isGooglePlayServicesAvailable(this);
        if (checkGoogleServices != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(checkGoogleServices)) {
                availability.getErrorDialog(this, checkGoogleServices,
                        REQUEST_CODE_RECOVERY_PLAY_SERVICES).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_RECOVERY_PLAY_SERVICES) {
            Toast.makeText(LoginScreen.this,
                    "Η εγκατάσταση του Google Play Services είναι απαραίτητη για να συνεχίσετε",
                    Toast.LENGTH_LONG).show();
            android.os.Process.killProcess(android.os.Process.myPid());
            super.onDestroy();
            finish();
        }
    }

    /*
     * If user has signed in before, log them in automatically
     */
    private void checkCredentials() {
        if (checkForNetwork(this)) {
            String[] credentials = new String[2];
            credentials[0] = sharedPreferences.getString(Constants.PREF_USER, null);
            credentials[1] = sharedPreferences.getString(Constants.PREF_PASS, null);
            if (credentials[0] != null && credentials[1] != null) {
                Log.d("Login", "Credentials are not null!");
                SoapLoginServiceManager sManager = new SoapLoginServiceManager(credentials, this);
                sManager.setCallback(this);
                sManager.call();
            } else {
                Toast.makeText(this, "Παρακαλώ συνδεθείτε για να συνεχίσετε",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            String title = "Πρόβλημα σύνδεσης";
            String message = "Δεν ήταν δυνατή η σύνδεση στο δίκτυο. Βεβαιωθείτε πως τα δεδομένα " +
                    "κινητής τηλεφωνίας είναι ανοιχτά και προσπαθήστε ξανά";
            AlertBuilder alert = new AlertBuilder(this, message, title);
            alert.showDialog();
        }
    }

    /*
     * Called by SoapManagerLogin. Supplies the code and key returned by the web service
     * and the username and password that was used for the login
     */
    @Override
    public void onLoginResponse(int code, String key, String user, String pass) {
        switch (code) {
            case Constants.NO_ERROR:

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(Constants.PREF_USER, user);
                editor.putString(Constants.PREF_PASS, pass);
                editor.putString(Constants.PREF_PKEY, key);
                editor.apply();
                Log.d("Login", "Got key: " + key);
                startActivity(new Intent(LoginScreen.this, MainDrawerActivity.class));
                break;
            case Constants.ERROR_UNKNOWN:
                String title = "Άγνωστο σφάλμα";
                String message = "Υπήρξε κάποιο λάθος κατά την σύνδεση." +
                        "Εάν είσαστε βέβαιοι πως έχετε σύνδεση στο ιντερνετ, επικοινωνήστε " +
                        "με τον διαχειριστή δικτύου της αποθήκης σας για την επίλυση του " +
                        "προβλήματος.";
                final AlertBuilder alert = new AlertBuilder(this, message, title);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert.showDialog();
                    }
                });
                break;
            case Constants.ERROR_WRONG_CRED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginScreen.this, "Τα στοιχεία που δώσατε είναι λανθασμένα. " +
                                "Παρακαλώ προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    }
                });

                break;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressSpinner.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onNetworkChanged(boolean status) {
        if (status) {
            checkCredentials();
        } else {
            Log.d("Login", "Network disabled");
        }
    }
}
