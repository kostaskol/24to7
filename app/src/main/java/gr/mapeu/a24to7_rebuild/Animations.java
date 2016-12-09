package gr.mapeu.a24to7_rebuild;


import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class Animations {
    //Password Layout disappears to the right
    static public void passDisappear (LinearLayout passLayout, ImageButton logIn, ImageButton back) {
        passLayout.animate().translationX(5000).start();
        logIn.animate().translationX(5000).start();
        back.animate().translationX(5000).start();
    }

    //User Layout disappears to the right
    static public void userDisappear(LinearLayout userLayout, ImageButton next,Animation userOff) {
        userLayout.startAnimation(userOff);
        next.startAnimation(userOff);
    }

    //Pass layout appears from the right
    static public void passAppear(LinearLayout passLayout, ImageButton logIn, Animation passOn, Animation logOn) {
        passLayout.startAnimation(passOn);
        logIn.startAnimation(logOn);
    }

    //User layout appears from the right
    static public void userAppear(LinearLayout userLayout, ImageButton next, Animation userOn) {
        userLayout.startAnimation(userOn);
        next.startAnimation(userOn);
    }
}
