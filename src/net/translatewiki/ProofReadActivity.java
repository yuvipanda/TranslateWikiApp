package net.translatewiki;

import org.mediawiki.auth.AuthenticatedActivity;
import org.mediawiki.auth.MWApiApplication;

import android.os.Bundle;
import android.util.Log;

public class ProofReadActivity extends AuthenticatedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestAuthToken();
    }

    @Override
    protected void onAuthCookieAcquired(String authCookie) {
        super.onAuthCookieAcquired(authCookie);
        Log.d("TWN", "Cookie is" + authCookie);
    }

    @Override
    protected void onAuthFailure() {
        super.onAuthFailure();
        Log.d("TWN", "Auth failed :(");
    }
    
}
