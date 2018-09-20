package com.rutgers.neemi;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.interfaces.AuthenticationListener;
import com.rutgers.neemi.model.Data;
import com.rutgers.neemi.model.InstagramResponse;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.rest.RestClient;
import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import java.sql.SQLException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by suitcase on 3/15/18.
 */

public class AuthenticationDialog extends Dialog {

    private final AuthenticationListener listener;
    private Context context;
    private WebView web_view;


    private final String auth_url;
    String TAG = "InstagramWebView";
    ConfigReader config;

    public AuthenticationDialog(Context context, AuthenticationListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        config = new ConfigReader(context);

        auth_url = config.getStr(PROPERTIES.INSTAGRAM_BASE_URL)
                + "oauth/authorize/?client_id="
                + config.getStr(PROPERTIES.INSTAGRAM_CLIENT_ID)
                + "&redirect_uri="
                + config.getStr(PROPERTIES.INSTAGRAM_REDIRECT_URI)
                + "&response_type=token";

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setContentView(R.layout.auth_dialog);
        super.onCreate(savedInstanceState);
        initializeWebView();
    }

    private void initializeWebView() {
        web_view = (WebView) findViewById(R.id.web_view);
        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.getSettings().setDomStorageEnabled(true);
        web_view.setWebViewClient(new WebViewClient() {


            boolean authComplete = false;


            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
             Log.i(TAG, "shouldOverrideUrlLoading() URL : " + url);
                if (url.contains("#access_token=") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    access_token = uri.getEncodedFragment();
                    // get the whole token after the '=' sign
                    access_token = access_token.substring(access_token.lastIndexOf("=")+1);
                    Log.i("", "CODE : " + access_token);
                    authComplete = true;
                    listener.onCodeReceived(access_token);
                    dismiss();
                    return true;
                } else if (url.contains("?error")) {
                    shouldOverrideUrlLoading(webView, auth_url);
                    Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return true;
                }else if (url.equals("https://www.instagram.com/")){
                    dismiss();
                    Intent myIntent = new Intent(getContext(), InstagramActivity.class);
                    myIntent.putExtra("action", "grant");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(myIntent);
                    return true;
                }
			    return false; // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "onPageStarted URL : " + url);

                super.onPageStarted(view, url, favicon);
            }

            String access_token;

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished() URL : " + url);

                super.onPageFinished(view, url);

                if (url.contains("#access_token=") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    access_token = uri.getEncodedFragment();
                    // get the whole token after the '=' sign
                    access_token = access_token.substring(access_token.lastIndexOf("=")+1);
                    Log.i("", "CODE : " + access_token);
                    authComplete = true;
                    listener.onCodeReceived(access_token);
                    dismiss();

                } else if (url.contains("?error")) {
                    Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });
        web_view.loadUrl(auth_url);

    }


}