package gr.mapeu.a24to7_rebuild.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;
import gr.mapeu.a24to7_rebuild.Callbacks.ListManagerCallback;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Fragments.FragmentChoosePharm;
import gr.mapeu.a24to7_rebuild.HelpfulClasses.AlertBuilder;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.Managers.ListManager;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapNotifyPharmCompleteManager;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapNotifyServiceManager;

public class NewPharmacy extends AppCompatActivity implements ListManagerCallback {

    EditText pharmCodeEdit;
    SharedPreferences prefs;
    String currCode;

    // static FragmentChoosePharm fragmentChoosePharm = new FragmentChoosePharm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pharmacy);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.rel_lay_placeholder, new FragmentChoosePharm());
        transaction.commit();
    }




    @Override
    public void onListAcquiredResponse(int returnCode) {
        // TODO: Notify user about whether the list was updated
    }
}
