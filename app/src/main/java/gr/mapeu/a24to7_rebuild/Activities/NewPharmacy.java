package gr.mapeu.a24to7_rebuild.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import gr.mapeu.a24to7_rebuild.Callbacks.ListResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.HelpfulClasses.AlertBuilder;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.Managers.ListManager;
import gr.mapeu.a24to7_rebuild.Managers.SoapManager;
import gr.mapeu.a24to7_rebuild.R;

public class NewPharmacy extends AppCompatActivity implements ListResponseHandler{

    EditText pharmCodeEdit;
    SharedPreferences prefs;
    String currCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pharmacy);

        prefs = getSharedPreferences(Constants.MYPREFS, MODE_PRIVATE);
        pharmCodeEdit = (EditText) findViewById(R.id.pharm_code_edit);

        Button pharmCode = (Button) findViewById(R.id.submit_pharm_code);
        pharmCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                currCode = pharmCodeEdit.getText().toString();
                if ((currCode.replace(" ", "")).equals("")) {
                    Toast.makeText(NewPharmacy.this, "Δεν συμπληρώσατε τον κωδικό φαρμακείου",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseManager manager = new DatabaseManager(NewPharmacy.this);

                if (!manager.pharmExists(currCode)) {
                    String title = "Άγνωστος κωδικός";
                    String message = "Ο κωδικός φαρμακείου που εισάγατε δεν βρέθηκε." +
                            "Αν είστε σίγουρος πως τον πληκτρολογήσατε σωστά και δεν λειτουργεί, " +
                            "πατήστε \"Ok\" για να ανανεώσετε την λίστα.";
                    DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pKey = prefs.getString(Constants.PKEY, null);
                            String user = prefs.getString(Constants.USER, null);
                            if (pKey == null || user == null) {
                                // How did we not get a key?
                                Log.e("New PHARM", "No KEY exists");
                            }
                            ListManager lManager = new ListManager(NewPharmacy.this, pKey, user);
                            lManager.getList();
                        }
                    };
                    DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    };
                    AlertBuilder alert = new AlertBuilder(NewPharmacy.this, message, title,
                            positive, negative);
                    alert.showDialog();
                    return;
                }

                editor.putString(Constants.CURR_PHARM_CODE, currCode);
            }
        });

        Button scanItemBtn = (Button) findViewById(R.id.pharm_scan);
        scanItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(NewPharmacy.this).initiateScan();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Ακυρώθηκε", Toast.LENGTH_LONG).show();
            } else {
                String code = result.getContents();
                System.out.println("Code: " + code + " pharm: " + currCode);
                DatabaseManager dbManager = new DatabaseManager(NewPharmacy.this);
                if (dbManager.scanProd(currCode, code)) {

                    Toast.makeText(NewPharmacy.this, "Το προϊόν σαρώθηκε επιτυχώς",
                            Toast.LENGTH_LONG).show();
                    if (dbManager.remainingOverall() == 0) {
                        // TODO: Error handling
                        String key = prefs.getString(Constants.PKEY, null);
                        String user = prefs.getString(Constants.USER, null);
                        SoapManager sManager = new SoapManager(key, user);
                        sManager.notifyCompletion();
                        Toast.makeText(NewPharmacy.this, "Όλα τα προϊόντα σαρώθηκαν.",
                                Toast.LENGTH_LONG).show();
                    }
                    if (dbManager.remainingPharm(currCode) == 0) {
                        Toast.makeText(NewPharmacy.this, "Όλα τα προϊόντα για το συγκεκριμένο " +
                                "φαρμακείο σαρώθηκαν επιτυχώς", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onListResponse(int returnCode) {
        // Do nothing
    }
}
