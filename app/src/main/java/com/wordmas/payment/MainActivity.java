package com.wordmas.payment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wordmas.payment.database.AppDatabase;
import com.wordmas.payment.database.DatabaseClient;
import com.wordmas.payment.model.SMS;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    WebView webView;
    private CommonLibrary Library;
    private static final String ONESIGNAL_APP_ID = "6b948c86-69cf-4bcd-ad43-e2f720b07db2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.splashScreenTheme);

        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        webView = findViewById(R.id.mainWV);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.addJavascriptInterface(new JavaScriptInterface(MainActivity.this),
                "AndroidMainActivity");

        webView.loadUrl("file:///android_asset/html/index.html");

        Library = new CommonLibrary();



    }//onCreate()



    public class JavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void  fetchAllSMS(){
            fetchSmsFromDB(mContext);
        }

        @JavascriptInterface
        public void  deleteAllSMS(){
            deleteAllSms(mContext);
            fetchAllSMS();
        }

        @JavascriptInterface
        public void  sendBroadcast(){
            Intent i = new Intent(mContext, SmsBroadcastReceiver.class);
            i.putExtra("trigger", "manual");
            mContext.sendBroadcast(i);
            Toast.makeText(mContext, "Broadcast sent", Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void enableData(){
            try {
                new SmsBroadcastReceiver().setMobileNetwork(mContext, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void disableData(){
            try {
                new SmsBroadcastReceiver().setMobileNetwork(mContext, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }






    void fetchSmsFromDB(Context context){
        class query extends AsyncTask<String, Void, List<SMS> > {
            @Override
            protected List<SMS> doInBackground(String... params) {
                final AppDatabase appDatabase = new DatabaseClient(context).getAppDatabase();
                List<SMS> allSms = appDatabase.smSdao().getAllSms();
                for(SMS sms : allSms){
                    Log.d("querySms", sms.getId()+"--"+sms.getSmsId()+"--"+sms.getMessage()+"---"+sms.isSentToServer());
                }
                return allSms;
            }

            @Override
            protected void onPostExecute(List<SMS> smsList) {
                Log.d("onPost", smsList.size()+" sms total");
                String dom = "";
                for(SMS sms : smsList){
                    dom+= "<p>"+sms.getId()+"--"+sms.getSmsId()+"--"+sms.getMessage()+"---"+sms.isSentToServer()+"</p>";
                }
                if(smsList.size() == 0){
                    dom = "No sms in Database";
                }
                //Log.d("dom", dom);
                webView.loadUrl("javascript:$('#log').html(\'"+dom+"\')");
            }
        }
        new query().execute();
    }


    void deleteAllSms(Context context){
        class query extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... strings) {
                final AppDatabase appDatabase = new DatabaseClient(context).getAppDatabase();
                appDatabase.smSdao().deleteAll();
                Log.d("deleteSms", "all deleted");
                return null;
            }
        }
        new query().execute();
    }


}




