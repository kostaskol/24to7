package gr.mapeu.a24to7_rebuild;

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

public class LoginScreen extends AppCompatActivity implements LoginCallback {

    Context loginContext;
    static private int REQUEST_CODE_RECOVERY_PLAY_SERVICES = 200;


    ImageButton logIn;
    ImageButton next;
    ImageButton back;
    ImageButton linkToPage;

    ProgressBar pb;

    LinearLayout passLayout;
    LinearLayout userLayout;

    static Animation passOff;
    static Animation passOn;
    static Animation userOff;
    static Animation userOn;
    static Animation logOn;

    EditText userEdit;
    EditText passEdit;

    Button hideUserKeyboard;
    Button hidePassKeyboard;

    Context loginScreenContext;

    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    ButtonCallbacks buttonCallbacks;
    AnimationCallbacks animationCallbacks;

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
                availability.getErrorDialog(this, checkGoogleServices, REQUEST_CODE_RECOVERY_PLAY_SERVICES).show();
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
            SoapManager sManager = new SoapManager(credentials, this);
            sManager.loginService();
        } else {
            Toast.makeText(this, "Παρακαλώ συνδεθείτε για να συνεχίσετε", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loginHandler(int code, String key) {
        if (code == Constants.ERROR_NO_ERROR) {
            startActivity(new Intent(LoginScreen.this, MainActivity.class).putExtra(Constants.KEY_EXTRA, key));
        } else {
            Toast.makeText(this, "Wrong cred", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void logoutHandler(int code) {

    }
}
