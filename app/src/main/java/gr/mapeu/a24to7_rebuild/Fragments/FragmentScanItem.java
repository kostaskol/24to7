package gr.mapeu.a24to7_rebuild.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.Managers.DatabaseManager;
import gr.mapeu.a24to7_rebuild.R;

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


    public void setTvText(int rem, String currPharmCode) {
        remTv1.setText("Σας απομένουν");
        remTvActual.setText(String.valueOf(rem));
        remTv2.setText("προϊόντα για το φαρμακείο με κωδικό " + currPharmCode);
    }

}
