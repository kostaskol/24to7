package gr.mapeu.a24to7_rebuild.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import gr.mapeu.a24to7_rebuild.Callbacks.ButtonCallbacks;
import gr.mapeu.a24to7_rebuild.Etc.Animations;
import gr.mapeu.a24to7_rebuild.Callbacks.AnimationCallbacks;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Callbacks.LoginCallback;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.Managers.SoapManager;

public class LoginScreen extends AppCompatActivity implements LoginCallback {

    Context loginContext;
    static private int REQUEST_CODE_RECOVERY_PLAY_SERVICES = 200;


    public ImageButton logIn;
    public ImageButton next;
    public ImageButton back;
    public ImageButton linkToPage;

    public ProgressBar pb;

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

    public Context loginScreenContext;

    static SharedPreferences sharedPreferences;

    public ButtonCallbacks buttonCallbacks;
    public AnimationCallbacks animationCallbacks;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        Log.i ("ERROR", "Started");
        loginContext = this;

        initialise();
        createListeners();
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
        pb = (ProgressBar) findViewById(R.id.loading);

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

        //Initialise Preferences
        sharedPreferences = getSharedPreferences(Constants.MYPREFS, MODE_PRIVATE);

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

    //If user has signed in before, log them in automatically
    private void checkCredentials() {
        String[] credentials = new String[2];
        credentials[0] = sharedPreferences.getString(Constants.USER, null);
        credentials[1] = sharedPreferences.getString(Constants.PASS, null);
        if (credentials[0] != null && credentials[1] != null) {
            Log.d("Login", "Credentials are not null!");
            SoapManager sManager = new SoapManager(credentials, this);
            sManager.loginService();
        } else {
            Toast.makeText(this, "Παρακαλώ συνδεθείτε για να συνεχίσετε",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loginHandler(int code, String key, String user, String pass) {
        if (code == Constants.NO_ERROR) {

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(Constants.USER, user);
            editor.putString(Constants.PASS, pass);
            editor.putString(Constants.PKEY, key);
            editor.apply();
            Log.d("Login", "Got key: " + key);
            startActivity(new Intent(LoginScreen.this, MainDrawerActivity.class));
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginScreen.this, "Τα στοιχεία που δώσατε είναι λανθασμένα. " +
                            "Παρακαλώ προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    @Override
    public void logoutHandler(int code) {

    }
}
