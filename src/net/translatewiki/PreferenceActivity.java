package net.translatewiki;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferenceActivity extends SherlockPreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
