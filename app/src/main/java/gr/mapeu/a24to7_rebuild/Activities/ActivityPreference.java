package gr.mapeu.a24to7_rebuild.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import gr.mapeu.a24to7_rebuild.R;

public class ActivityPreference extends android.preference.PreferenceActivity {

   @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       getFragmentManager()
               .beginTransaction()
               .replace(android.R.id.content, new FragmentPreference())
               .commit();
   }
}
