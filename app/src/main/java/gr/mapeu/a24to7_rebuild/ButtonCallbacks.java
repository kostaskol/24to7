package gr.mapeu.a24to7_rebuild;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

class ButtonCallbacks {

    private LoginScreen context;
    ButtonCallbacks(LoginScreen context) {
        this.context = context;
    }

    View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Animations.userDisappear(context.userLayout, context.next, context.userOff);
            Animations.passAppear(context.passLayout, context.logIn, context.passOn, context.logOn);
        }
    };

    View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Animations.passDisappear(context.passLayout, context.logIn, context.back);
            Animations.userAppear(context.userLayout, context.next, context.userOn);
        }
    };

    View.OnClickListener linkToPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            context.loginScreenContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://mapgr.eu")));
        }
    };

    View.OnClickListener hideKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InputMethodManager imm = (InputMethodManager)
                    context.loginScreenContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.setVisibility(View.INVISIBLE);
        }
    };

    View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String user = context.userEdit.getText().toString();
            final String pass = context.passEdit.getText().toString();
            if (!user.equals("") &&
                    !pass.equals("")) {
                SoapManager soapManager = new SoapManager(new String[] {user, pass}, context);
                soapManager.loginService();
            } else {
                Toast.makeText(context, "Παρακαλώ συμπληρώστε και το όνομα χρήστη και τον κωδικό",
                        Toast.LENGTH_LONG).show();
            }
        }
    };
}