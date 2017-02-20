package gr.mapeu.a24to7_rebuild.Callbacks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import gr.mapeu.a24to7_rebuild.Activities.LoginScreen;
import gr.mapeu.a24to7_rebuild.Etc.Animations;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapLoginServiceManager;

public class ButtonCallbacks {

    private LoginScreen context;
    public ButtonCallbacks(LoginScreen context) {
        this.context = context;
    }

    public View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Animations.userDisappear(context.userLayout, context.next, context.userOff);
            Animations.passAppear(context.passLayout, context.logIn, context.passOn, context.logOn);
        }
    };

    public View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Animations.passDisappear(context.passLayout, context.logIn, context.back);
            Animations.userAppear(context.userLayout, context.next, context.userOn);
        }
    };

    public View.OnClickListener linkToPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            context.loginScreenContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mapgr.eu")));
        }
    };

    public View.OnClickListener hideKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InputMethodManager imm = (InputMethodManager)
                    context.loginScreenContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.setVisibility(View.INVISIBLE);
        }
    };

    public View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String user = context.userEdit.getText().toString();
            final String pass = context.passEdit.getText().toString();
            if (!user.equals("") &&
                    !pass.equals("")) {
                SoapLoginServiceManager soapManager =
                        new SoapLoginServiceManager(new String[] {user, pass}, context);
                soapManager.setCallback(context);
                soapManager.call();
            } else {
                Toast.makeText(context, "Παρακαλώ συμπληρώστε και το όνομα χρήστη και τον κωδικό",
                        Toast.LENGTH_LONG).show();
            }
        }
    };
}