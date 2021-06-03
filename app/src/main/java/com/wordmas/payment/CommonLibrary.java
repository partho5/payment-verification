package com.wordmas.payment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Telephony;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordmas.payment.model.SMS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CommonLibrary {

    public String prependEscapingCharacter(String text){
        text = text.replace("'", "\\'");
        text = text.replace("\"", "\\\"");
        text = text.replace("’", "\\'");
        text = text.replace("<", "\\<");
        text = text.replace(">", "\\>");
        text = text.replace("(", "\\(");
        text = text.replace(")", "\\)");

        return text;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //https://gist.github.com/snooplsm/9d3972fcb730f8e2fe71
    @SuppressLint("NewApi")
    public List<String> getSmsFromInbox(Context context, int limit) {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(Telephony.Sms.Inbox.CONTENT_URI,
                new String[] {
                        Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.ADDRESS,
                        Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE_SENT
                },
                null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);
        int totalSMS = c.getCount();

        Log.d("totalSms", totalSMS+"");
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                lstSms.add(c.getString(0));

                Log.d("mySms", c.getString(0)+" ---"
                        +c.getString(1)+"----"+c.getString(2)+
                        "----"+c.getString(3) );
                c.moveToNext();
                if(i>=limit) break;
            }
        } else {
            //throw new RuntimeException("You have no SMS in Inbox");
            Log.d("noSms", "Inbox empty");
        }
        c.close();

        return lstSms;
    }


    public String paymentListToJson(List<UserPayment> paymentList){
        String json = "";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(out, paymentList);
            final byte[] data = out.toByteArray();
            json = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }


    void milis2DateTime(){
        long currentDateTime = 1622107972000L;
        Date currentDate = new Date(currentDateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        System.out.println("myDate"+sdf.format(currentDate));
    }

}
