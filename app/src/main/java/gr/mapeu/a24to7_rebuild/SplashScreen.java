package gr.mapeu.a24to7_rebuild;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    ImageView iv;
    Animation an;
    Animation fade;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        iv = (ImageView) findViewById(R.id.circle);
        an =  AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        fade = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade);
        intent = new Intent(this, LoginScreen.class);


        iv.startAnimation(an);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv.startAnimation(fade);
                finish();
                Log.i ("ERROR", "Starting activity");
                startActivity(intent);
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
