package gr.mapeu.a24to7_rebuild.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import gr.mapeu.a24to7_rebuild.Etc.Constants;
import gr.mapeu.a24to7_rebuild.R;

public class FragmentPreference extends PreferenceFragment {

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        sharedPreferences =
                getActivity().getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);

        Preference urlPref = findPreference(getString(R.string.url));
        urlPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = newValue.toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String actualUrl = "http://" + value + "/GPS24-7_Service/GPSService.svc?wsdl";
                editor.putString(Constants.PREF_URL, actualUrl);
                editor.apply();
                return true;
            }
        });

        Preference listPref = findPreference(getString(R.string.send_freq));
        listPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(Constants.PREF_INTERVAL, Integer.parseInt((String) newValue));
                editor.apply();
                return true;
            }
        });

        Preference debugPref = findPreference("debug_key");
        debugPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean debugOn = (boolean) newValue;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.PREF_DEBUG, debugOn);
                editor.apply();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
