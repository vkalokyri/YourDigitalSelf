package com.rutgers.neemi;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.TransactionHasCategory;
import com.rutgers.neemi.model.Place;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaidActivity extends AppCompatActivity {


    String client_id = "596e6db04e95b810ac887f56";
    String secret = "d1ba7f8c06c7d70da0cef6e28b6b2f";

    PlaidClient plaidClient = PlaidClient.newBuilder()
            .clientIdAndSecret(client_id, secret)
            //.publicKey("0ea8ed7c85e1c6d8aa4695cb156c97")
            //.sandboxBaseUrl()// optional. only needed to call endpoints that require a public key
            .developmentBaseUrl() // or equivalent, depending on which environment you're calling into
            .build();

    ProgressDialog mProgress;
    DatabaseHelper helper;
    String account_name;
    String accountId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plaid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
       // ab.setDisplayHomeAsUpEnabled(true);

        helper=DatabaseHelper.getHelper(this);



        Intent i = getIntent();
        String permissionType = i.getStringExtra("action");

        if(permissionType.equals("grant")) {
            grantPermissions();
            //getResultsFromApi();
        } else{
            revokePermissions();
            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.putExtra("key", "bank");
            myIntent.putExtra("items", 0);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

        }



        //mProgress = new ProgressDialog(this);
        //mProgress.setMessage("Authorizing Plaid. Please wait ...");


    }


    public void grantPermissions(){
        // Initialize Link
        HashMap<String, String> linkInitializeOptions = new HashMap<String,String>();
        linkInitializeOptions.put("key", "0ea8ed7c85e1c6d8aa4695cb156c97");
        linkInitializeOptions.put("client_id", client_id);
        linkInitializeOptions.put("secret", secret);
        linkInitializeOptions.put("apiVersion", "v2");
        //linkInitializeOptions.put("product", "auth");
        linkInitializeOptions.put("product", "transactions");
        linkInitializeOptions.put("selectAccount", "true");
        //linkInitializeOptions.put("env", "sandbox");
        linkInitializeOptions.put("env", "development");
        linkInitializeOptions.put("clientName", "YourDigitalSelf");
        linkInitializeOptions.put("webhook", "http://requestb.in");
        linkInitializeOptions.put("baseUrl", "https://cdn.plaid.com/link/v2/stable/link.html");
        // If initializing Link in PATCH / update mode, also provide the public_token
        // linkInitializeOptions.put("public_token", "PUBLIC_TOKEN")

        // Generate the Link initialization URL based off of the configuration options.
        final Uri linkInitializationUrl = generateLinkInitializationUrl(linkInitializeOptions);

        // Modify Webview settings - all of these settings may not be applicable
        // or necesscary for your integration.
        final WebView plaidLinkWebview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = plaidLinkWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        //webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        WebView.setWebContentsDebuggingEnabled(true);


        // Initialize Link by loading the Link initiaization URL in the Webview
        plaidLinkWebview.loadUrl(linkInitializationUrl.toString());

        // Override the Webview's handler for redirects
        // Link communicates success and failure (analogous to the web's onSuccess and onExit
        // callbacks) via redirects.
        plaidLinkWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Parse the URL to determine if it's a special Plaid Link redirect or a request
                // for a standard URL (typically a forgotten password or account not setup link).
                // Handle Plaid Link redirects and open traditional pages directly in the  user's
                // preferred browser.
                Uri parsedUri = Uri.parse(url);
                if (parsedUri.getScheme().equals("plaidlink")) {
                    String action = parsedUri.getHost();
                    final HashMap<String, String> linkData = parseLinkUriData(parsedUri);

                    if (action.equals("connected")) {

                        //final String accessToken;

                        Log.d("Public token: ", linkData.get("public_token"));
                        Log.d("Account ID: ", linkData.get("account_id"));
                        Log.d("Account name: ", linkData.get("account_name"));
                        account_name = linkData.get("account_name");


                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(PlaidActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(PlaidActivity.this);
                        }


                        Toast.makeText(getApplicationContext(), "Plaid was successfully authorized!", Toast.LENGTH_SHORT).show();
                        new RetrieveTransactionsTask(getApplicationContext()).execute(linkData.get("public_token"));


                    } else if (action.equals("exit")) {
                        // User exited
                        // linkData may contain information about the user's status in the Link flow,
                        // the institution selected, information about any error encountered,
                        // and relevant API request IDs.
                        //  Log.d("User status in flow: ", linkData.get("status"));
                        // The requet ID keys may or may not exist depending on when the user exited
                        // the Link flow.
                        // Log.d("Link request ID: ", linkData.get("link_request_id"));
                        // Log.d("API request ID: ", linkData.get("plaid_api_request_id"));

                        // Reload Link in the Webview
                        // You will likely want to transition the view at this point.
                        plaidLinkWebview.loadUrl(linkInitializationUrl.toString());
                    } else {
                        Log.d("Link action detected: ", action);
                    }
                    // Override URL loading
                    return true;
                } else if (parsedUri.getScheme().equals("https") ||
                        parsedUri.getScheme().equals("http")) {
                    // Open in browser - this is most  typically for 'account locked' or
                    // 'forgotten password' redirects
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    // Override URL loading
                    return true;
                } else {
                    // Unknown case - do not override URL loading
                    return false;
                }
            }
        });

    }


    public void revokePermissions(){

    }


    // Generate a Link initialization URL based on a set of configuration options
    public Uri generateLinkInitializationUrl(HashMap<String,String>linkOptions) {
        Uri.Builder builder = Uri.parse(linkOptions.get("baseUrl"))
                .buildUpon()
                .appendQueryParameter("isWebview", "true")
                .appendQueryParameter("isMobile", "true");
        for (String key : linkOptions.keySet()) {
            if (!key.equals("baseUrl")) {
                builder.appendQueryParameter(key, linkOptions.get(key));
            }
        }
        return builder.build();
    }

    // Parse a Link redirect URL querystring into a HashMap for easy manipulation and access
    public HashMap<String,String> parseLinkUriData(Uri linkUri) {
        HashMap<String,String> linkData = new HashMap<String,String>();
        for(String key : linkUri.getQueryParameterNames()) {
            linkData.put(key, linkUri.getQueryParameter(key));
        }
        return linkData;
    }


    private class RetrieveTransactionsTask extends AsyncTask<String, Void, Void> {

        private Exception exception;

        Context context;
        private RetrieveTransactionsTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected void onPreExecute() {
            //mProgress.show();
        }

        @Override
        protected Void doInBackground(String... publicToken) {
            try {
                getDataFromApi(publicToken[0]);
            } catch (Exception e) {
                cancel(true);
                return null;
            }
            return null;
        }

        private void getDataFromApi(String publicToken) throws IOException {


            plaidClient.service()
                    .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                    .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {
                        @Override
                        public void onResponse(Call<ItemPublicTokenExchangeResponse> call, Response<ItemPublicTokenExchangeResponse> response) {
                            if (response.isSuccessful()) {
                                final String accessToken = response.body().getAccessToken();
                                new AsyncGetAccountsTask().execute(accessToken);
                                //new AsyncGetTransactionsTask().execute(accessToken);
                            }

                        }

                        @Override
                        public void onFailure(Call<ItemPublicTokenExchangeResponse> call, Throwable t) {
                            // handle the failure as needed
                        }
                    });
        }

        @Override
        protected void onPostExecute(Void output) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "bank");
            myIntent.putExtra("items", -1);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);


        }

    }


    private class AsyncGetAccountsTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String accessToken = params[0];
            try {
                Response<AccountsGetResponse> accountsResponse = plaidClient.service().accountsGet(
                        new AccountsGetRequest(
                                accessToken)).execute();

                List<Account> accounts = accountsResponse.body().getAccounts();
                for (Account account:accounts) {
                    String last4digits = account.getMask();
                    String accountName = account.getName();
                    accountId = account.getAccountId();
                    String full_account = accountName+"_@#_"+last4digits+"_@#_"+accountId;

                    try {
                        FileOutputStream fos = openFileOutput(full_account, Context.MODE_PRIVATE);
                        fos.write(accessToken.getBytes());
                        fos.close();
                        FileOutputStream fos2 = openFileOutput("BankAccounts", Context.MODE_APPEND);
                        fos2.write((full_account+System.getProperty("line.separator")).getBytes());
                        fos2.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

