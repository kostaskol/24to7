package gr.mapeu.a24to7_rebuild.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.R;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapNotifyPharmCompleteManager;
import gr.mapeu.a24to7_rebuild.SoapManagers.SoapNotifyServiceManager;

public class FragmentScanItem extends Fragment {

    SharedPreferences sharedPreferences;
    TextView remTv1;
    TextView remTvActual;
    TextView remTv2;

    public FragmentScanItem() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_scan_item, container, false);

        sharedPreferences =
                getContext().getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);

        remTv1 = (TextView) view.findViewById(R.id.rem_one_tv);
        remTvActual = (TextView) view.findViewById(R.id.rem_actual_tv);
        remTv2 = (TextView) view.findViewById(R.id.rem_two_tv);


        String currPharmCode = sharedPreferences.getString(Constants.PREF_CURR_PHARM_CODE, null);

        DatabaseManager dbManager = new DatabaseManager(getContext());
        int rem = dbManager.remainingPharm(currPharmCode);

        setTvText(rem, currPharmCode);

        Button scanNewProdBtn = (Button) view.findViewById(R.id.scan_new_prod_btn);
        scanNewProdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(getActivity()).initiateScan();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getContext(), "Ακυρώθηκε", Toast.LENGTH_LONG).show();
            } else {
                Log.d("NewPharm", "Code scanned");
                String code = result.getContents();
                String currCode = sharedPreferences.getString(Constants.PREF_CURR_PHARM_CODE, null);
                if (currCode == null) {
                    Log.d("NewPharm", "Null code");
                    return;
                }
                System.out.println("Code: " + code + " pharm: " + currCode);
                DatabaseManager dbManager = new DatabaseManager(getContext());
                if (dbManager.scanProd(currCode, code)) {

                    Toast.makeText(getContext(), "Το προϊόν σαρώθηκε επιτυχώς",
                            Toast.LENGTH_LONG).show();

                    Log.d("NewPharm", "Remaining: " + dbManager.remainingOverall());
                    Log.d("NewPharm", "Remaining pharm: " + dbManager.remainingPharm(currCode));
                    int remainingPharm = dbManager.remainingPharm(currCode);
                    int remainingOverall = dbManager.remainingOverall();
                    setTvText(dbManager.remainingPharm(currCode), currCode);
                    if (remainingOverall == 0) {
                        Log.d("NewPharm", "Overall complete");
                        // TODO: Error handling
                        String key = sharedPreferences.getString(Constants.PREF_PKEY, null);
                        String user = sharedPreferences.getString(Constants.PREF_USER, null);
                        SoapNotifyServiceManager sManager =
                                new SoapNotifyServiceManager(user, key, getContext());
                        sManager.call();
                        Toast.makeText(getContext(), "Όλα τα προϊόντα σαρώθηκαν.",
                                Toast.LENGTH_LONG).show();
                    }
                    if (remainingPharm == 0) {
                        Log.d("NewPharm", "Pharm complete");
                        //TODO: Do the error handling (no key / user)
                        String key = sharedPreferences.getString(Constants.PREF_PKEY, null);
                        String user = sharedPreferences.getString(Constants.PREF_USER, null);

                        Log.d("NewPhar", "Calling notify with code: " + currCode);
                        SoapNotifyPharmCompleteManager sManager =
                                new SoapNotifyPharmCompleteManager(user, key, currCode, getContext());

                        sManager.call();
                        Toast.makeText(getContext(), "Όλα τα προϊόντα για το συγκεκριμένο " +
                                "φαρμακείο σαρώθηκαν επιτυχώς", Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setTvText(int rem, String currPharmCode) {
        remTv1.setText("Σας απομένουν");
        remTvActual.setText(String.valueOf(rem));
        remTv2.setText("προϊόντα για το φαρμακείο με κωδικό " + currPharmCode);
    }

}
