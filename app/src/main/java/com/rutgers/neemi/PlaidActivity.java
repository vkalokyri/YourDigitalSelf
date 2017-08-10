package com.rutgers.neemi;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.api.client.util.DateTime;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;
import com.rutgers.neemi.model.PaymentCategory;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.PaymentHasCategory;
import com.rutgers.neemi.model.Place;


import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaidActivity extends AppCompatActivity {


    PlaidClient plaidClient = PlaidClient.newBuilder()
            .clientIdAndSecret("596e6db04e95b810ac887f56", "d1ba7f8c06c7d70da0cef6e28b6b2f")
            .publicKey("0ea8ed7c85e1c6d8aa4695cb156c97") // optional. only needed to call endpoints that require a public key
            .sandboxBaseUrl() // or equivalent, depending on which environment you're calling into
            .build();

    ProgressDialog mProgress;
    DatabaseHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plaid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        helper=new DatabaseHelper(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your financial transactions. Please wait ...");

        // Initialize Link
        HashMap<String, String> linkInitializeOptions = new HashMap<String,String>();
        linkInitializeOptions.put("key", "0ea8ed7c85e1c6d8aa4695cb156c97");
        linkInitializeOptions.put("client_id", "596e6db04e95b810ac887f56");
        linkInitializeOptions.put("secret", "d1ba7f8c06c7d70da0cef6e28b6b2f");
        linkInitializeOptions.put("apiVersion", "v2");
        //linkInitializeOptions.put("product", "auth");
        linkInitializeOptions.put("product", "transactions");
        linkInitializeOptions.put("selectAccount", "true");
        linkInitializeOptions.put("env", "sandbox");
        linkInitializeOptions.put("clientName", "Neemi");
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
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
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

                       new RetrieveTransactionsTask(getApplicationContext()).execute(linkData.get("public_token"));

//
//
//
//                        Response<AuthGetResponse> response = null;
//                        try {
//                            response = plaidClient.service().authGet(new AuthGetRequest("ACCESS_TOKEN")).execute();
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                        System.out.println(response);

                        // User successfully linked





//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                        startDate = simpleDateFormat.parse("2017-01-01");
//                        endDate = simpleDateFormat.parse("2017-02-01");
//
//                        Response<TransactionsGetResponse> response = client().service().transactionsGet(
//                                new TransactionsGetRequest(
//                                        "ACCESS_TOKEN",
//                                        startDate,
//                                        endDate))
//                                .execute();
//
//                        Response<TransactionsGetResponse> response = client().service().transactionsGet(
//                                new TransactionsGetRequest(
//                                        accessToken,
//                                        startDate,
//                                        endDate)
//                                        .withAccountIds(Arrays.asList(someAccountId))
//                                        .withCount(numTxns)
//                                        .withOffset(1)).execute();
//
//                        for (TransactionsGetResponse.Transaction txn : response.body().getTransactions()) { ... }



                        // You will likely want to transition the view at this point.
                        //plaidLinkWebview.loadUrl(linkInitializationUrl.toString());
                    } else if (action.equals("exit")) {
                        // User exited
                        // linkData may contain information about the user's status in the Link flow,
                        // the institution selected, information about any error encountered,
                        // and relevant API request IDs.
                        Log.d("User status in flow: ", linkData.get("status"));
                        // The requet ID keys may or may not exist depending on when the user exited
                        // the Link flow.
                        Log.d("Link request ID: ", linkData.get("link_request_id"));
                        Log.d("API request ID: ", linkData.get("plaid_api_request_id"));

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
            mProgress.show();
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
                                new AsyncGetTransactionsTask().execute(accessToken);
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


        }

    }

    private class AsyncGetTransactionsTask extends AsyncTask<String, Void, Integer> {

        final RuntimeExceptionDao<PaymentCategory, String> categoryDao = helper.getCategoryDao();
        final RuntimeExceptionDao<Payment, String> paymentDao = helper.getPaymentDao();
        final RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
        final RuntimeExceptionDao<Place, String> placeDao = helper.getPlaceDao();
        final RuntimeExceptionDao<PaymentHasCategory, String> transactionHasCategoriesDao = helper.getTransactionCategoriesDao();

        int transactionsRetrieved = 0;


        @Override
        protected Integer doInBackground(String... params) {
            String accessToken = params[0];
            try {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, -6);
                    Date startDate = null;
                    Date endDate = null;



                    try {

                        startDate = simpleDateFormat.parse(simpleDateFormat.format(cal.getTime()));
                        endDate = simpleDateFormat.parse(simpleDateFormat.format(Calendar.getInstance().getTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    String timestamp = null;

                    GenericRawResults<String[]> rawResults = paymentDao.queryRaw("select max(timestamp) from Payment;");
                    List<String[]> results = null;
                    try {
                        results = rawResults.getResults();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (results!=null){
                        String[] resultArray = results.get(0);
                        System.out.println("timestamp= " + resultArray[0]);
                        timestamp=resultArray[0];
                    }

                    if (timestamp!=null) {
                        Calendar caltimestamp = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                        caltimestamp.setTimeInMillis(Long.parseLong(timestamp)*1000);
                        startDate = simpleDateFormat.parse(simpleDateFormat.format(caltimestamp.getTime()));
                    }



                    //startDate = simpleDateFormat.parse("2017-01-01");
                    //endDate = simpleDateFormat.parse("2017-02-01");
                    int totalTransactions=0;
                    do{
                        Response<TransactionsGetResponse> transactionsResponse = plaidClient.service().transactionsGet(
                                new TransactionsGetRequest(
                                        accessToken,
                                        startDate,
                                        endDate)
                                        .withOffset(transactionsRetrieved)).execute();


                    if (transactionsResponse!=null) {
                        totalTransactions = transactionsResponse.body().getTotalTransactions();

                        for (TransactionsGetResponse.Transaction txn : transactionsResponse.body().getTransactions()) {
                            Payment transaction = new Payment();

                            if (txn.getAccountId() != null) {
                                transaction.setAccount_id(txn.getAccountId());
                            }
                            if (txn.getAccountOwner() != null) {
                                Person personExists = helper.personExistsByName(txn.getAccountOwner());
                                if (personExists == null) {
                                    Person newPerson = new Person();
                                    newPerson.setName(txn.getAccountOwner());
                                    personDao.create(newPerson);
                                    transaction.setAccount_owner(newPerson);
                                } else {
                                    transaction.setAccount_owner(personExists);
                                }
                            }
                            if (txn.getAmount() != null) {
                                transaction.setAmount(txn.getAmount());
                            }
                            if (txn.getDate() != null) {
                                String date = txn.getDate();
                                transaction.setDate(simpleDateFormat.parse(txn.getDate()).getTime());
                            }
                            if (txn.getName() != null) {
                                transaction.setName(txn.getName());
                            }
                            if (txn.getOriginalDescription() != null) {
                                transaction.setDescription(txn.getOriginalDescription());
                            }
                            if (txn.getPending() != null) {
                                transaction.setPending(txn.getPending());
                                System.out.println();
                            }

                            if (txn.getTransactionId() != null) {
                                transaction.setId(txn.getTransactionId());
                            }
                            if (txn.getTransactionType() != null) {
                                transaction.setTransaction_type(txn.getTransactionType());
                            }
                            if (txn.getAccountId() != null) {
                                transaction.setAccount_id(txn.getAccountId());
                            }
                            if (txn.getPaymentMeta() != null) {
                                TransactionsGetResponse.Transaction.PaymentMeta meta = txn.getPaymentMeta();
                                if (meta.getPayee() != null) {
                                    Person personExists = helper.personExistsByName(meta.getPayee());
                                    if (personExists == null) {
                                        Person newPerson = new Person();
                                        newPerson.setName(meta.getPayee());
                                        personDao.create(newPerson);
                                        transaction.setPayee(newPerson);
                                    } else {
                                        transaction.setPayee(personExists);
                                    }
                                }
                                if (meta.getPayer() != null) {
                                    Person personExists = helper.personExistsByName(meta.getPayer());
                                    if (personExists == null) {
                                        Person newPerson = new Person();
                                        newPerson.setName(meta.getPayer());
                                        personDao.create(newPerson);
                                        transaction.setPayer(newPerson);
                                    } else {
                                        transaction.setPayer(personExists);
                                    }

                                }
                                if (meta.getPaymentMethod() != null) {
                                    transaction.setPayment_method(meta.getPaymentMethod());
                                }
                            }
                            if (txn.getLocation() != null) {
                                TransactionsGetResponse.Transaction.Location location = txn.getLocation();
                                if (location.getLat() != null && location.getLon() != null) {
                                    Place placeExists = helper.placeExistsByLatLong(location.getLat(), location.getLon());
                                    if (placeExists == null) {
                                        Place newPlace = new Place();
                                        if (location.getAddress() != null) {
                                            newPlace.setStreet(location.getAddress());
                                        }
                                        if (location.getCity() != null) {
                                            newPlace.setCity(location.getCity());
                                        }
                                        if (location.getLat() != null) {
                                            newPlace.setLatitude(location.getLat());
                                        }
                                        if (location.getLon() != null) {
                                            newPlace.setLongitude(location.getLon());
                                        }
                                        if (location.getState() != null) {
                                            newPlace.setState(location.getState());
                                        }
                                        if (location.getZip() != null) {
                                            newPlace.setZip(location.getZip());
                                        }
                                        placeDao.create(newPlace);
                                        transaction.setPlace(newPlace);

                                    } else {
                                        transaction.setPlace(placeExists);
                                    }
                                }
                            }

                            transaction.setTimestamp(System.currentTimeMillis() / 1000);
                            paymentDao.create(transaction);

                            if (txn.getCategory() != null) {
                                List<PaymentCategory> categoryList = new ArrayList<>();
                                for (String category : txn.getCategory()) {
                                    PaymentCategory categoryExists = categoryExists(category);
                                    if (categoryExists == null) {
                                        PaymentCategory newCategory = new PaymentCategory();
                                        newCategory.setCategoryName(category);
                                        categoryDao.create(newCategory);
                                        categoryList.add(newCategory);
                                    } else {
                                        PaymentHasCategory trans_categories = new PaymentHasCategory(transaction, categoryExists);
                                        transactionHasCategoriesDao.create(trans_categories);
                                    }
                                }
                                for (PaymentCategory eachCategory : categoryList) {
                                    PaymentHasCategory trans_categories = new PaymentHasCategory(transaction, eachCategory);
                                    transactionHasCategoriesDao.create(trans_categories);
                                }


                            }
                            transactionsRetrieved++;
                        }
                    }
                    }while(transactionsRetrieved<totalTransactions);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return transactionsRetrieved;
        }

        protected void onPostExecute(Integer output) {
            mProgress.hide();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "bank");
            myIntent.putExtra("items", output);
            startActivity(myIntent);

            //Snackbar.make(findViewById(R.id.myCoordinatorLayout),  output+" financial transactions fetched.", Snackbar.LENGTH_LONG).show();

        }

        public PaymentCategory categoryExists(String name) {

            RuntimeExceptionDao<PaymentCategory, String> categoryDao = helper.getCategoryDao();

            QueryBuilder<PaymentCategory, String> queryBuilder =
                    categoryDao.queryBuilder();
            Where<PaymentCategory, String> where = queryBuilder.where();
            try {
                where.eq(PaymentCategory.CATEGORY, name);
                List<PaymentCategory> results = queryBuilder.query();
                if (results.size() != 0) {
                    return results.get(0);
                } else
                    return null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

