package com.wordmas.payment;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.wordmas.payment.api.ApiUtils;
import com.wordmas.payment.database.AppDatabase;
import com.wordmas.payment.database.DatabaseClient;
import com.wordmas.payment.model.SMS;

import org.json.JSONException;
import org.json.JSONObject;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    SmsMessage smsMessage;
    String payerNum = null, amount = null, trxId = null, receivedAt = null, paymentMethod = null, message = null, senderAddress;
    CommonLibrary Library = new CommonLibrary();
    ApiUtils apiUtils = new ApiUtils();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("startTime", System.currentTimeMillis() + "");

        try {
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            smsMessage = msgs[0];
            senderAddress = smsMessage.getDisplayOriginatingAddress();
            message = smsMessage.getDisplayMessageBody();

            displayNotification(context, senderAddress, message);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        //smsMessage.getTimestampMillis()
//
//        if(message.contains("TrxID")){
//            //bkash payment received by "send money" option
//            paymentMethod = "bKash";
//            BkashSmsParser bkashSmsParser = new BkashSmsParser(message);
//            Log.d("bKash", "sender="+bkashSmsParser.getSender()+" amount="+bkashSmsParser.getAmount()+" TrxId="+bkashSmsParser.getTrxID()+" time="+bkashSmsParser.getReceivedAt());
//            //Toast.makeText(context, "sender="+bkashSmsParser.getSender()+" amount="+bkashSmsParser.getAmount()+" TrxId="+bkashSmsParser.getTrxID()+" time="+bkashSmsParser.getReceivedAt(), Toast.LENGTH_LONG).show();
//            payerNum = bkashSmsParser.getSender();
//            amount = bkashSmsParser.getAmount();
//            trxId = bkashSmsParser.getTrxID();
//            receivedAt = bkashSmsParser.getReceivedAt();
//
//        }
//        displayNotification(context, senderAddress, message);
//        Log.d("mySms", senderAddress+"-----"+message);

        int limit = 100;
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(Telephony.Sms.Inbox.CONTENT_URI,
                new String[]{
                        Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.ADDRESS,
                        Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE_SENT
                },
                null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
//                Log.d("mySms", c.getInt(0)+" ---"
//                        +c.getString(1)+"----"+c.getString(2)+
//                        "----"+c.getString(3) );

                int smsId = c.getInt(0);
                String senderAddress = c.getString(1);
                String message = c.getString(2);
                String sentTimeMilis = c.getString(3);

                SMS sms = new SMS();
                sms.setSmsId(smsId);
                sms.setMessage(message);
                sms.setSenderAddress(senderAddress);
                sms.setSentToServer(false);

                if (message.contains("You have received Tk")) {
                    insertSms(context, sms);
                }

                c.moveToNext();
                if (i >= limit) break;
            }
        } else {
            //throw new RuntimeException("You have no SMS in Inbox");
            Log.d("noSms", "Inbox empty");
        }
        c.close();


        if (Library.isNetworkAvailable(context)) {
            syncPayments(context);
            try {
                Thread.sleep(2*60*1000);//2 min
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            try {
                setMobileNetwork(context, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
                Intent i = new Intent(context, SmsBroadcastReceiver.class);
                context.sendBroadcast(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    void insertSms(Context context, SMS sms) {
        class DoInsert extends AsyncTask<SMS, Void, String> {
            @Override
            protected String doInBackground(SMS... sms) {
                SMS mySms = sms[0];
                final AppDatabase appDatabase = new DatabaseClient(context).getAppDatabase();

                List<SMS> foundSms = appDatabase.smSdao().getBySmsId(mySms.getSmsId());
                if (foundSms.size() == 0) {
                    //i.e. not found
                    long id = appDatabase.smSdao().insert(mySms);
                    Log.d("inserted", id + " smsId=" + mySms.getSmsId());
                } else {
                    Log.d("smsExist", mySms.getId() + "  smsId=" + mySms.getSmsId() + " exists");
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

            }
        }
        new DoInsert().execute(sms);
    }

    void syncPayments(Context context) {
        class fetcher extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                final AppDatabase appDatabase = new DatabaseClient(context).getAppDatabase();
                List<SMS> smsList = appDatabase.smSdao().getSmsToSync();

                BkashSmsParser bkashSmsParser;
                UserPayment userPayment;
                List<UserPayment> paymentList = new ArrayList<>();
                List<Integer> syncedSmsIds = new ArrayList<>();

                for (SMS smsObj : smsList) {
                    syncedSmsIds.add(smsObj.getSmsId());
                    message = smsObj.getMessage();
                    senderAddress = smsObj.getSenderAddress();

                    Log.d("toSync", smsObj.getSmsId() + "---" + message + "----");

                    if (message.contains("You have received Tk") || senderAddress.equals("bKash")) {
                        paymentMethod = "bKash";
                    }
                    bkashSmsParser = new BkashSmsParser(message);
                    userPayment = new UserPayment();
                    userPayment.setMethod(paymentMethod);
                    userPayment.setPayerMobile(bkashSmsParser.getSender());
                    userPayment.setPaidAt(bkashSmsParser.getReceivedAt());
                    userPayment.setPaidAmount(bkashSmsParser.getAmount());
                    userPayment.setAuthToken(bkashSmsParser.getTrxID());
                    userPayment.setMeta("{\"bKashOption\":\"sendMoney\"}");

                    paymentList.add(userPayment);
                }

                String json = Library.paymentListToJson(paymentList);
                //Log.d("paymentJson", json);

                String response = "";
                try {
                    String urlStr = apiUtils.baseDomain() + "/api/payment/create";
                    try {
                        String urlParameters = "data=" + json + "&authKey=onlyMyDeviceAccepted623";
                        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                        int postDataLength = postData.length;
                        URL url = new URL(urlStr);

                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setDoInput(true);
                        con.setDoOutput(true);

                        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        con.setRequestProperty("charset", "utf-8");
                        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                        con.setInstanceFollowRedirects(false);
                        con.setUseCaches(false);

                        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                            wr.write(postData);
                        }

                        BufferedReader br = null;
                        if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        } else {
                            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                        }
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            response = response + line;
                            Log.d("serverResponse", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String msg = jsonObject.getString("msg");
                                if (msg.equals("success")) {
                                    appDatabase.smSdao().makeSynced(syncedSmsIds);
                                }
                                Log.d("endTime", System.currentTimeMillis() + "");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }
        new fetcher().execute();
    }


    public void setMobileNetwork(Context context, int state) throws Exception {
        String command = null;
        try {
            // Get the current state of the mobile network.
            //state = isMobileDataEnabledFromLollipop(context) ? 0 : 1;
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            String transactionCode = getTransactionCode(context);
            // Android 5.1+ (API 22) and later.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            // We do not have this permission. Let's ask the user
                            return;
                        }
                        int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                        // Execute the command via `su` to turn off mobile network for a subscription service.
                        command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                        Log.d("myCommand", command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0 (API 21) only.
                if (transactionCode != null && transactionCode.length() > 0) {
                    // Execute the command via `su` to turn off mobile network.
                    command = "service call phone " + transactionCode + " i32 " + state;
                    executeCommandViaSu(context, "-c", command);
                }
            }
        } catch(Exception e) {
            // Oops! Something went wrong, so we throw the exception here.
            throw e;
        }
    }




    private static boolean isMobileDataEnabledFromLollipop(Context context) {
        boolean state = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        return state;
    }

    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

    private static void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
                e.printStackTrace();
            } finally {
                success = true;
            }
        }
    }



    void displayNotification(Context context, String title, String details) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("simplifiedcoding", "simplifiedcoding", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "simplifiedcoding")
                .setContentTitle(title)
                .setContentText(details)
                .setSmallIcon(R.mipmap.ic_launcher);
;
        notificationManager.notify((int) System.currentTimeMillis(), notification.build());
    }

}
