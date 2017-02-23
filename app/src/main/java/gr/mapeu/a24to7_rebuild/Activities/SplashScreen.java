package gr.mapeu.a24to7_rebuild.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.R;

public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final ImageView iv = (ImageView) findViewById(R.id.circle);
        final Animation an =  AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        final Animation fade = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade);
        final Intent intent = new Intent(this, LoginScreen.class);

        final SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);
        String url = sharedPreferences.getString(Constants.PREF_URL, null);
        final boolean urlExists = url != null;


        iv.startAnimation(an);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /*if (urlExists) {
                    iv.startAnimation(fade);
                    finish();
                    Log.i("ERROR", "Starting activity");
                    startActivity(intent);
                } else {*/
                final EditText urlEdit = new EditText(SplashScreen.this);
                String ip = sharedPreferences.getString(Constants.PREF_IP, "79.130.22.30:8080");
                urlEdit.setText(ip);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                urlEdit.setLayoutParams(lp);
                TextView tv = new TextView(SplashScreen.this);
                tv.setText("Yo");
                tv.setLayoutParams(lp);
                AlertDialog.Builder alert = new AlertDialog.Builder(SplashScreen.this);
                String message = "Input company's URL: (without http:// and /GPS24-7_Service...";
                String title = "New Url";
                alert.setView(urlEdit);
                alert.setMessage(message);
                alert.setTitle(title);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = urlEdit.getText().toString();
                        String actualUrl = "http://" + url + "/GPS24-7_Service/GPSService.svc?wsdl";
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.PREF_URL, actualUrl);
                        editor.putString(Constants.PREF_IP, url);
                        editor.apply();

                        startActivity(intent);
                    }
                });
                alert.show();

                //}
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
