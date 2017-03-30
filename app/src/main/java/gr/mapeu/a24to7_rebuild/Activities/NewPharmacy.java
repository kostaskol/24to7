package gr.mapeu.a24to7_rebuild.Activities;

import android.app.Activity;
import android.content.Context;
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
import gr.mapeu.a24to7_rebuild.Fragments.FragmentScanItem;
import gr.mapeu.a24to7_rebuild.HelpfulClasses.AlertBuilder;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.Managers.ListManager;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapNotifyPharmCompleteManager;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapNotifyServiceManager;

public class NewPharmacy extends AppCompatActivity implements ListManagerCallback {

    SharedPreferences sharedPreferences;
    static public FragmentChoosePharm choosePharm;
    static public FragmentScanItem scanItem;



    // static FragmentChoosePharm fragmentChoosePharm = new FragmentChoosePharm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pharmacy);

        sharedPreferences = getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        choosePharm = new FragmentChoosePharm();
        scanItem = new FragmentScanItem();

        transaction.add(R.id.rel_lay_placeholder, choosePharm);
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d("Frag", "On Activity Result called");

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Ακυρώθηκε", Toast.LENGTH_LONG).show();
            } else {
                Log.d("NewPharm", "Code scanned");
                String scannedCode = result.getContents();
                String currCode = sharedPreferences.getString(Constants.PREF_CURR_PHARM_CODE, null);
                Log.d("NewPharm", "Code scanned: " + scannedCode);
                DatabaseManager dbManager = new DatabaseManager(this);
                if (dbManager.scanProd(currCode, scannedCode)) {

                    Toast.makeText(this, "Το προϊόν σαρώθηκε επιτυχώς",
                            Toast.LENGTH_LONG).show();

                    Log.d("NewPharm", "Remaining: " + dbManager.remainingOverall());
                    Log.d("NewPharm", "Remaining pharm: " + dbManager.remainingPharm(currCode));
                    int remainingPharm = dbManager.remainingPharm(currCode);
                    int remainingOverall = dbManager.remainingOverall();
                    scanItem.setTvText(dbManager.remainingPharm(currCode), currCode);

                    if (remainingPharm == 0) {
                        Log.d("NewPharm", "Pharm complete");
                        //TODO: Do the error handling (no key / user)
                        String key = sharedPreferences.getString(Constants.PREF_PKEY, null);
                        String user = sharedPreferences.getString(Constants.PREF_USER, null);
                        String pharmStatus = sharedPreferences.getString(Constants.PREF_STATUS, null);

                        Log.d("NewPhar", "Calling notify with code: " + currCode);
                        SoapNotifyPharmCompleteManager sManager =
                                new SoapNotifyPharmCompleteManager(user, key, currCode, pharmStatus, this);

                        sManager.call();
                        Toast.makeText(this, "Όλα τα προϊόντα για το συγκεκριμένο " +
                                "φαρμακείο σαρώθηκαν επιτυχώς", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    if (remainingOverall == 0) {
                        Log.d("NewPharm", "Overall complete");
                        // TODO: Error handling
                        String key = sharedPreferences.getString(Constants.PREF_PKEY, null);
                        String user = sharedPreferences.getString(Constants.PREF_USER, null);
                        SoapNotifyServiceManager sManager =
                                new SoapNotifyServiceManager(user, key, this);
                        sManager.call();
                        Toast.makeText(this, "Όλα τα προϊόντα σαρώθηκαν.",
                                Toast.LENGTH_LONG).show();
                    }

                } else if (dbManager.pharmExists(scannedCode)) {
                    Toast.makeText(this, "Pharm : " + scannedCode + " exists", Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.PREF_CURR_PHARM_CODE, scannedCode);
                    editor.putString(Constants.PREF_STATUS, "1");
                    editor.apply();

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    transaction.add(R.id.rel_lay_placeholder, scanItem);
                    transaction.remove(choosePharm);
                    transaction.commitAllowingStateLoss();;
                }
                else if(dbManager.pharmExists(scannedCode.substring(0,scannedCode.length()-1)) && scannedCode.substring(scannedCode.length() - 1).equals("0")){
                    scannedCode = scannedCode.substring(0,scannedCode.length()-1);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.PREF_CURR_PHARM_CODE, scannedCode);
                    editor.putString(Constants.PREF_STATUS, "0");
                    editor.apply();

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    transaction.add(R.id.rel_lay_placeholder, scanItem);
                    transaction.remove(choosePharm);
                    transaction.commitAllowingStateLoss();;

                }
                else {
                    Log.d("NewPharm", "Unknown code");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




    @Override
    public void onListAcquiredResponse(int returnCode) {
        // TODO: Notify user about whether the list was updated
    }
}
