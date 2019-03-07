package com.rutgers.neemi;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.rutgers.neemi.model.Photo;

import static com.facebook.FacebookSdk.getApplicationContext;

public class GmapsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        WebView mWebview = new WebView(getApplicationContext()){
            @Override
            public boolean onCheckIsTextEditor() {
                return true;
            }
        };
        mWebview.setWebViewClient(new WebViewClient());
        mWebview.getSettings().setAllowContentAccess(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setDomStorageEnabled(true);
        mWebview.getSettings().setAllowFileAccess(true);
        mWebview.getSettings().setAppCacheEnabled(true);
        mWebview.setFocusable(true);
        mWebview.setFocusableInTouchMode(true);
        mWebview.setHapticFeedbackEnabled(true);
        mWebview.setClickable(true);
        mWebview.loadUrl("https://takeout.google.com/settings/takeout/custom/location_history");
        alert.setView(mWebview);
        alert.setNegativeButton("Download data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent myIntent = new Intent(getApplicationContext(), GMapsDriveActivity.class);
                myIntent.putExtra("action", "sync");
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
            }
        });
        alert.show();
        alert.setCancelable(true);







//        mWebview.setDownloadListener(new DownloadListener() {
//            public void onDownloadStart(String url, String userAgent,
//                                        String contentDisposition, String mimetype,
//                                        long contentLength) {
////                Intent i = new Intent(Intent.ACTION_VIEW);
////                i.setData(Uri.parse(url));
////                startActivity(i);
//
//                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//                request.setNotificationVisibility(
//                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            }
//
//        });

//
//        saveDataButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//            }
//        });
//



    }




}
