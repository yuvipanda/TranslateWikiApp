package net.translatewiki;

import java.io.IOException;
import java.util.ArrayList;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;
import org.mediawiki.auth.AuthenticatedActivity;
import org.mediawiki.auth.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class ProofReadActivity extends AuthenticatedActivity {
    
    private TextView ebTranslatedText;
    private TextView ebSourceText;
    
    private String translateToken = null;
    
    private Message curMessage;
    private ArrayList<Message> translations;
    private int curIndex;
    private class FetchTranslationsTask extends AsyncTask<Void, Void, Boolean> {

        private Activity context;
        private ProgressDialog dialog;
      
        public FetchTranslationsTask(Activity context) {
            this.context = context;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();
            showMessage(translations.get(0));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Loading!");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            MWApi api = app.getApi();
            ApiResult result;
            try {
                String userId = api.getUserID();
               result = api.action("query")
                           .param("list", "messagecollection")
                           .param("mcgroup", "core")
                           .param("mclanguage", "ml")
                           .param("mclimit", "100")
                           .param("mcprop", "definition|translation|revision")
                           .param("mcfilter", "!last-translator:" + userId + "|!reviewer:" + userId + "|!ignored|translated" )
                           .get();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            ArrayList<ApiResult> messages = result.getNodes("/api/query/messagecollection/message");
            Log.d("TWN", "Actual result is" + Utils.getStringFromDOM(result.getDocument()));
            for(ApiResult message: messages) {
                Message m = new Message(message.getString("@key"), "ta", message.getString("@definition"), message.getString("@translation"), message.getString("@revision"));
                if(!m.getRevision().equals("")) {
                    translations.add(m);
                }
            }
            return true;
        }
        
    }
    
    private class ReviewTranslationTask extends AsyncTask<Message, Void, Boolean> {

        private Activity context;
        private ProgressDialog dialog;
        
        public ReviewTranslationTask(Activity context) {
            this.context = context;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();
            showMessage(translations.get(++curIndex));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Loading!");
            dialog.show();
        }
        
        @Override
        protected Boolean doInBackground(Message... params) {
            try {
                if(!app.getApi().validateLogin()) {
                    if(((TranslateWikiApp)app).revalidateAuthToken()) {
                        // Validated!
                        Log.d("TWN", "VALIDATED!");
                    } else {
                        Log.d("TWN", "Invalid :(");
                        throw new RuntimeException();
                    }
                }
                Message message = params[0];
                if(translateToken == null) {
                    ApiResult tokenResult;
                    tokenResult = app.getApi().action("tokens").param("type", "translationreview").get();
                    Log.d("TWN", "First result is " + Utils.getStringFromDOM(tokenResult.getDocument()));
                    translateToken = tokenResult.getString("/api/tokens/@translationreviewtoken");
                    Log.d("TWN", "Token is " + translateToken);
                }
                ApiResult reviewResult = app.getApi().action("translationreview")
                        .param("revision", message.getRevision())
                        .param("token", translateToken).get();
                Log.d("TWN", Utils.getStringFromDOM(reviewResult.getDocument()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            
            return false;
        }
        
    }
    
    void showMessage(Message message) {
        ebSourceText.setText(message.getDefinition());
        ebTranslatedText.setText(message.getTranslation());
        curMessage = message;
    }
    
    
    public void accept_translation(View target) {
       ReviewTranslationTask task = new ReviewTranslationTask(this);
       Utils.executeAsyncTask(task, curMessage);
    }
    
    public void reject_translation(View target) {
       showMessage(translations.get(++curIndex)); 
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proofread);
        
        ebSourceText = (TextView)findViewById(R.id.ebSourceTExt);
        ebTranslatedText = (TextView)findViewById(R.id.ebTranslatedText);
        
        translations = new ArrayList<Message>();
        requestAuthToken();
    }

    @Override
    protected void onAuthCookieAcquired(String authCookie) {
        super.onAuthCookieAcquired(authCookie);
        app.getApi().setAuthCookie(authCookie);
        Log.d("TWN", "AUTH COOKIE ACQUIRED PEOPLE!" + app.getApi().getAuthCookie());
        FetchTranslationsTask fetchTranslations = new FetchTranslationsTask(this);
        Utils.executeAsyncTask(fetchTranslations);
    }

    @Override
    protected void onAuthFailure() {
        super.onAuthFailure();
        super.onAuthFailure();
        Toast failureToast = Toast.makeText(this, R.string.authentication_failed, Toast.LENGTH_LONG);
        failureToast.show();
        finish();
    }
    
}
