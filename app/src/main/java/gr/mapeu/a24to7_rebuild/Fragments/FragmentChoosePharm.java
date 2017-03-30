package gr.mapeu.a24to7_rebuild.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import gr.mapeu.a24to7_rebuild.Activities.NewPharmacy;
import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.HelpfulClasses.AlertBuilder;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.Managers.ListManager;
import gr.mapeu.a24to7_rebuild.R;

public class FragmentChoosePharm extends Fragment {

    SharedPreferences sharedPreferences;

    public FragmentChoosePharm() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_choose_pharm, container, false);
        ImageButton getCodeBtn = (ImageButton) view.findViewById(R.id.move_to_scan_btn);
        final EditText pharmCodeEdit = (EditText) view.findViewById(R.id.pharm_code_edit);
        sharedPreferences =
                getContext().getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);
        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String currCode = pharmCodeEdit.getText().toString();
                if ((currCode.replace(" ", "")).equals("")) {
                    Toast.makeText(getContext(), "Δεν συμπληρώσατε τον κωδικό φαρμακείου",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseManager manager = new DatabaseManager(getContext());


                if (manager.pharmExists(currCode)) {
                    editor.putString(Constants.PREF_CURR_PHARM_CODE, currCode);
                    editor.putString(Constants.PREF_STATUS, "1");
                    editor.apply();


                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.addToBackStack(null);
                    transaction.remove(FragmentChoosePharm.this);
                    transaction.add(R.id.rel_lay_placeholder, NewPharmacy.scanItem);
                    transaction.commit();
                    InputMethodManager inm = (InputMethodManager)
                            getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else if (manager.pharmExists(currCode.substring(0, currCode.length() - 1)) && currCode.substring(currCode.length() - 1).equals("0")) {
                    currCode = currCode.substring(0, currCode.length() - 1);
                    editor.putString(Constants.PREF_CURR_PHARM_CODE, currCode);
                    editor.putString(Constants.PREF_STATUS, "0");
                    editor.apply();


                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.addToBackStack(null);
                    transaction.remove(FragmentChoosePharm.this);
                    transaction.add(R.id.rel_lay_placeholder, NewPharmacy.scanItem);
                    transaction.commit();
                    InputMethodManager inm = (InputMethodManager)
                            getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else if (!manager.pharmExists(currCode)) {
                    String title = "Άγνωστος κωδικός";
                    String message = "Ο κωδικός φαρμακείου που εισάγατε δεν βρέθηκε." +
                            "Αν είστε σίγουρος πως τον πληκτρολογήσατε σωστά και δεν λειτουργεί, " +
                            "πατήστε \"Ok\" για να ανανεώσετε την λίστα.";
                    DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pKey = sharedPreferences.getString(Constants.PREF_PKEY, null);
                            String user = sharedPreferences.getString(Constants.PREF_USER, null);
                            if (pKey == null || user == null) {
                                // How did we not get a key?
                                Log.e("New PHARM", "No KEY exists");
                            }
                            ListManager lManager = new ListManager(getContext(), pKey, user);
                            lManager.getList();
                        }
                    };
                    DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    };
                    AlertBuilder alert = new AlertBuilder(getContext(), message, title,
                            positive, negative);
                    alert.showDialog();
                    return;
                }
            }
        });


        ImageButton scanPharmBtn = (ImageButton) view.findViewById(R.id.scanPharmButton);

        scanPharmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Choose pharm", "On Click listener");
                new IntentIntegrator(getActivity()).initiateScan();
            }
        });

        return view;
    }
}
