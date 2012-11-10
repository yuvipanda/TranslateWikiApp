package net.translatewiki;

import java.io.IOException;
import java.util.ArrayList;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;
import org.mediawiki.auth.AuthenticatedActivity;
import org.mediawiki.auth.Utils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

public class ProofReadActivity extends AuthenticatedActivity implements OnSharedPreferenceChangeListener {
    
    private String translateToken = null;
    
    private Message curMessage;
    private MessageListAdaptor translations;
    private int curIndex;
    final Context context = this;
    private int offsetCounter = 0;
    Button moreButton;
    private class FetchTranslationsTask extends AsyncTask<Void, Void, ArrayList<Message>> {

        private Activity context;
        private String lang;
        private ProgressDialog dialog;
        
      
        public FetchTranslationsTask(Activity context, String lang) {
            this.context = context;
            this.lang = lang;
        }
        
        @Override
        protected void onPostExecute(ArrayList<Message> result) {
            super.onPostExecute(result);
            //translations.clear(); 
            for(Message m : result) {
                translations.add(m);
            }
            dialog.dismiss();
            moreButton.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            moreButton.setVisibility(View.INVISIBLE);
            dialog = new ProgressDialog(context);
            dialog.setTitle("Loading!");
            dialog.show();
        }

        @Override
        protected ArrayList<Message> doInBackground(Void... params) {
            MWApi api = app.getApi();
            ArrayList<Message> messagesList = new ArrayList<Message>();
            ApiResult result;
            try {
                String userId = api.getUserID();
               result = api.action("query")
                           .param("list", "messagecollection")
                           .param("mcgroup", "core")
                           .param("mclanguage", lang)
                           .param("mclimit", "10")
                           .param("mcoffset",Integer.valueOf(offsetCounter).toString())
                           .param("mcprop", "definition|translation|revision")
                           .param("mcfilter", "!last-translator:" + userId + "|!reviewer:" + userId + "|!ignored|translated" )
                           .get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            ArrayList<ApiResult> messages = result.getNodes("/api/query/messagecollection/message");
            Log.d("TWN", "Actual result is" + Utils.getStringFromDOM(result.getDocument()));
            for(ApiResult message: messages) {
                Message m = new Message(message.getString("@key"), lang, message.getString("@definition"), message.getString("@translation"), message.getString("@revision"));
                messagesList.add(m);
            }
            return messagesList;
        }
        
    }
    
    private class ReviewTranslationTask extends AsyncTask<Void, Void, Boolean> {

        private Activity context;
        private ProgressDialog dialog;
        private Message message;
        
        public ReviewTranslationTask(Activity context, Message message) {
            this.context = context;
            this.message = message;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();
            translations.remove(message);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Submitting!");
            dialog.setMessage(message.getKey());
            dialog.show();
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
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
    
    private class MessageListAdaptor extends ArrayAdapter<Message> {

        public MessageListAdaptor(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) {
                v = getLayoutInflater().inflate(R.layout.listitem_translation, null);
            }
            
            final Message m = this.getItem(position);
            
            TextView lblSourceText = (TextView) v.findViewById(R.id.lblSourceText);
            TextView lblTranslatedText = (TextView) v.findViewById(R.id.lblTranslatedText);
            Button btnReject = (Button) v.findViewById(R.id.btnReject); 
            Button btnAccept = (Button) v.findViewById(R.id.btnAccept); 
            
            btnReject.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    translations.remove(m);
                }
            });
           
            btnAccept.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    ReviewTranslationTask task = new ReviewTranslationTask(ProofReadActivity.this, m);
                    Utils.executeAsyncTask(task);
                }
            });
            
            lblSourceText.setText(m.getDefinition());
            lblTranslatedText.setText(m.getTranslation());
            
            return v;
        }

        
    }
    
    void showMessage(Message message) {
        curMessage = message;
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proofread);
        
        translations = new MessageListAdaptor(this, 0);
        ListView listView = (ListView) findViewById(R.id.listTranslations);
        View footerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.more_button_footer, null, false);
        listView.addFooterView(footerView);

        moreButton = (Button)findViewById(R.id.buttonMore);
        moreButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	offsetCounter += 10;
            	Toast.makeText(context, "Getting mo for ya...", Toast.LENGTH_LONG).show();
            	refreshTranslations();
            	
            }
        });
        moreButton.setVisibility(View.INVISIBLE);
        listView.setAdapter(translations);
        
        requestAuthToken();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.proofread_menu, (com.actionbarsherlock.view.Menu) menu);
       return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            Intent i = new Intent(this, PreferenceActivity.class);
            startActivity(i);
            return true;
        }
        return false;
    }

    void onGetMore()
    {
    	Toast.makeText(context, "Getting more..", Toast.LENGTH_LONG).show();
    }
    private void refreshTranslations() {
        //translations.clear();
        String lang = PreferenceManager.getDefaultSharedPreferences(this).getString("language", "en");
        FetchTranslationsTask fetchTranslations = new FetchTranslationsTask(this, lang);
        fetchTranslations.execute();
    }
    
    @Override
    protected void onAuthCookieAcquired(String authCookie) {
        super.onAuthCookieAcquired(authCookie);
        app.getApi().setAuthCookie(authCookie);
        refreshTranslations();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onAuthFailure() {
        super.onAuthFailure();
        Toast failureToast = Toast.makeText(this, R.string.authentication_failed, Toast.LENGTH_LONG);
        failureToast.show();
        finish();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        refreshTranslations();
    }
    
}
