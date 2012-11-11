package net.translatewiki;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferenceActivity extends SherlockPreferenceActivity {
	final Context context = this;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Intent intent = new Intent(this, MessageGroupGetterActivity.class);
        PreferenceScreen msgGrpPref = (PreferenceScreen)findPreference("msgGroupPref");
        msgGrpPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference p) {
                // TODO stuff 
            	Toast.makeText(context, "Getting Message Groups fo ya..", Toast.LENGTH_SHORT).show();
            	
            	startActivity(intent);
                return true;
            }
        });
        
    }
}
