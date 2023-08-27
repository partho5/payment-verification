package com.wordmas.payment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.wordmas.payment.database.DatabaseClient;
import com.wordmas.payment.model.SMS;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "6b948c86-69cf-4bcd-ad43-e2f720b07db2";
    private static final int PERMISSION_ALL_REQUEST_CODE = 10;
    /* access modifiers changed from: private */
    public CommonLibrary Library;
    SmsBroadcastReceiver smsBroadcastReceiver = new SmsBroadcastReceiver();
    WebView webView;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        getSupportActionBar().hide();
        this.webView = (WebView) findViewById(R.id.mainWV);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.addJavascriptInterface(new JavaScriptInterface(this), "AndroidMainActivity");
        this.webView.loadUrl("file:///android_asset/html/index.html");
        this.Library = new CommonLibrary();
        try {
            if (Build.VERSION.SDK_INT < 11) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 2);
            } else if (!Environment.isExternalStorageEmulated()) {
                Intent intent = new Intent("android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS");
                intent.setData(Uri.fromParts("package", getPackageName(), (String) null));
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] PERMISSIONS = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.READ_SMS", "android.permission.RECEIVE_SMS", "android.permission.SEND_SMS", "android.permission.READ_PHONE_STATE"};
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 10);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(this.smsBroadcastReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.smsBroadcastReceiver);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context == null || permissions == null) {
            return true;
        }
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != 0) {
                return false;
            }
        }
        return true;
    }

    public class JavaScriptInterface {
        Context mContext;

        JavaScriptInterface(Context c) {
            this.mContext = c;
        }

        @JavascriptInterface
        public void fetchAllSMS() {
            MainActivity.this.fetchSmsFromDB(this.mContext);
        }

        @JavascriptInterface
        public void deleteAllSMS() {
            MainActivity.this.deleteAllSms(this.mContext);
            fetchAllSMS();
        }

        @JavascriptInterface
        public void sendBroadcast() {
            try {
                Intent intent = new Intent(this.mContext, SmsBroadcastReceiver.class);
                intent.putExtra("trigger", "manual");
                class SendBroadcastTask extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected Void doInBackground(Void... params) {
                        mContext.sendBroadcast(intent);
                        return null;
                    }
                }

                // To send the broadcast from the main thread, execute the AsyncTask:
                new SendBroadcastTask().execute();
                Toast.makeText(this.mContext, "Broadcast sent", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                CommonLibrary Library = MainActivity.this.Library;
                Context context = this.mContext;
                Library.appendToFile(context, "payment.txt", "sendBroadcast() error: " + e.toString(), false);
            }
        }

        @JavascriptInterface
        public void enableData() {
            try {
                new SmsBroadcastReceiver().setMobileNetwork(this.mContext, 1);
            } catch (Exception e) {
                e.printStackTrace();
                CommonLibrary Library = MainActivity.this.Library;
                Context context = this.mContext;
                Library.appendToFile(context, "payment.txt", "enableData() error: " + e.toString(), false);
            }
        }

        @JavascriptInterface
        public void disableData() {
            try {
                new SmsBroadcastReceiver().setMobileNetwork(this.mContext, 0);
            } catch (Exception e) {
                e.printStackTrace();
                CommonLibrary Library = MainActivity.this.Library;
                Context context = this.mContext;
                Library.appendToFile(context, "payment.txt", "disableData() error: " + e.toString(), false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void fetchSmsFromDB(final Context context) {
        new AsyncTask<String, Void, List<SMS>>() {
            /* access modifiers changed from: protected */
            public List<SMS> doInBackground(String... params) {
                List<SMS> allSms = new DatabaseClient(context).getAppDatabase().smSdao().getAllSms();
                for (SMS sms : allSms) {
                    Log.d("querySms", sms.getId() + "--" + sms.getSmsId() + "--" + sms.getMessage() + "---" + sms.isSentToServer());
                }
                return allSms;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(List<SMS> smsList) {
                Log.d("onPost", smsList.size() + " sms total");
                String dom = "";
                for (SMS sms : smsList) {
                    dom = dom + "<p>" + sms.getId() + "--" + sms.getSmsId() + "--" + sms.getMessage() + "---" + sms.isSentToServer() + "</p>";
                }
                if (smsList.size() == 0) {
                    dom = "No sms in Database";
                }
                MainActivity.this.webView.loadUrl("javascript:$('#log').html('" + dom + "')");
            }
        }.execute(new String[0]);
    }

    /* access modifiers changed from: package-private */
    public void deleteAllSms(final Context context) {
        new AsyncTask<String, Void, String>() {
            /* access modifiers changed from: protected */
            public String doInBackground(String... strings) {
                new DatabaseClient(context).getAppDatabase().smSdao().deleteAll();
                Log.d("deleteSms", "all deleted");
                return null;
            }
        }.execute(new String[0]);
    }
}
