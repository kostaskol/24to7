package gr.mapeu.a24to7_rebuild;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

class Callbacks {
    static class ButtonCallbacks {

        static View.OnClickListener nextListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animations.userDisappear(LoginScreen.userLayout, LoginScreen.next, LoginScreen.userOff);
                Animations.passAppear(LoginScreen.passLayout, LoginScreen.logIn, LoginScreen.passOn, LoginScreen.logOn);
            }
        };

        static View.OnClickListener backListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animations.passDisappear(LoginScreen.passLayout, LoginScreen.logIn, LoginScreen.back);
                Animations.userAppear(LoginScreen.userLayout, LoginScreen.next, LoginScreen.userOn);
            }
        };

        static View.OnClickListener linkToPageListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginScreen.loginScreenContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mapgr.eu")));
            }
        };

        static View.OnClickListener hideKeyboardListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)
                        LoginScreen.loginScreenContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.setVisibility(View.INVISIBLE);
            }
        };

        static View.OnClickListener loginListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user = LoginScreen.userEdit.getText().toString();
                final String pass = LoginScreen.passEdit.getText().toString();
                if (!user.equals("") &&
                        !pass.equals("")) {
                    SoapManager sManager = new SoapManager(new String[] {user, pass});
                    sManager.loginService();
                } else {
                    Toast.makeText(LoginScreen.loginContext, "Παρακαλώ συμπληρώστε και το όνομα χρήστη και τον κωδικό",
                            Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    static class AnimationCallbacks {
        static Animation.AnimationListener passOnAnimation = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoginScreen.passLayout.animate().translationX(0).start();
                LoginScreen.passLayout.setX(LoginScreen.userLayout.getX());
                LoginScreen.passLayout.setY(LoginScreen.userLayout.getY());
                LoginScreen.logIn.animate().translationX(0).start();
                LoginScreen.back.animate().translationX(0).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        static Animation.AnimationListener userOnAnimation = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoginScreen.userLayout.animate().translationX(0).start();
                LoginScreen.next.animate().translationX(0).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        static Animation.AnimationListener userOffAnimation = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoginScreen.userLayout.animate().translationX(5000).start();
                LoginScreen.next.animate().translationX(5000).start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }
}
