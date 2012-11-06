package net.translatewiki;

import java.io.IOException;

import org.apache.http.HttpVersion;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.mediawiki.api.MWApi;
import org.mediawiki.auth.MWApiApplication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;

public class TranslateWikiApp extends Application implements MWApiApplication {
    private MWApi api;
    private Account currentAccount = null; // Unlike a savings account...
    public static final String API_URL = "http://translatewiki.net/w/api.php";
   
    public static MWApi createMWApi() {
        DefaultHttpClient client = new DefaultHttpClient();
        // Because WMF servers support only HTTP/1.0. Biggest difference that
        // this makes is support for Chunked Transfer Encoding. 
        // I have this here so if any 1.1 features start being used, it 
        // throws up. 
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, 
                HttpVersion.HTTP_1_0);
        return new MWApi(API_URL, client);
    }
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        api = createMWApi();
    }
    
    @Override
    public MWApi getApi() {
        return api;
    }
    
    @Override
    public Account getCurrentAccount() {
        if(currentAccount == null) {
            AccountManager accountManager = AccountManager.get(this);
            Account[] allAccounts = accountManager.getAccountsByType(getString(R.string.account_type_identifier));
            if(allAccounts.length != 0) {
                currentAccount = allAccounts[0];
            }
        }
        return currentAccount;
    }
    
    public Boolean revalidateAuthToken() {
        AccountManager accountManager = AccountManager.get(this);
        Account curAccount = getCurrentAccount();
       
        if(curAccount == null) {
            return false; // This should never happen
        }
        
        accountManager.invalidateAuthToken(getString(R.string.account_type_identifier), api.getAuthCookie());
        try {
            String authCookie = accountManager.blockingGetAuthToken(curAccount, "", false);
            api.setAuthCookie(authCookie);
            return true;
        } catch (OperationCanceledException e) {
            e.printStackTrace();
            return false;
        } catch (AuthenticatorException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void setCurrentAccount(Account account) {
       currentAccount = account; 
    }

}
