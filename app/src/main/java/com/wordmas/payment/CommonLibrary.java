package com.wordmas.payment;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CommonLibrary {
    private String appFolder = "aFolder";

    public String prependEscapingCharacter(String text) {
        return text.replace("'", "\\'").replace("\"", "\\\"").replace("’", "\\'").replace("<", "\\<").replace(">", "\\>").replace("(", "\\(").replace(")", "\\)");
    }

    public boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @SuppressLint({"NewApi"})
    public List<String> getSmsFromInbox(Context context, int limit) {
        List<String> lstSms = new ArrayList<>();
        Cursor c = context.getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI, new String[]{"_id", "address", "body", "date_sent"}, (String) null, (String[]) null, "date DESC");
        int totalSMS = c.getCount();
        Log.d("totalSms", totalSMS + "");
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                lstSms.add(c.getString(0));
                Log.d("mySms", c.getString(0) + " ---" + c.getString(1) + "----" + c.getString(2) + "----" + c.getString(3));
                c.moveToNext();
                if (i >= limit) {
                    break;
                }
            }
        } else {
            Log.d("noSms", "Inbox empty");
        }
        c.close();
        return lstSms;
    }

    public String paymentListToJson(List<UserPayment> paymentList) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectMapper().writeValue((OutputStream) out, (Object) paymentList);
            return new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /* access modifiers changed from: package-private */
    public String readFileToStr(String fileName) throws IOException {
        File dir = new File("/storage/emulated/0/" + this.appFolder);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dir.getPath() + "/" + fileName)));
        while (true) {
            String readLine = br.readLine();
            String strLine = readLine;
            if (readLine == null) {
                return stringBuilder.toString();
            }
            stringBuilder.append(strLine + "\n");
        }
    }

    public void writeFileOnInternalStorage(Context context, String fileName, String data) {
        File dir = new File("/storage/emulated/0/" + this.appFolder);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String path = dir.getPath() + "/" + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(path));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e2) {
            Log.e("Exception", "File write failed: " + e2.toString());
            Toast.makeText(context, e2.toString(), 1).show();
        }
    }

    public void appendToFile(Context context, String fileName, String dataToAppend, boolean flushBeforeAppend) {
        String fileData = "";
        try {
            fileData = readFileToStr(fileName);
            if (flushBeforeAppend) {
                fileData = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String curTime = milisToDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm:ss");
        writeFileOnInternalStorage(context, fileName, "\n>>" + curTime + "\n" + dataToAppend + "\n" + fileData);
    }

    /* access modifiers changed from: package-private */
    public String milisToDate(long milis, String pattern) {
        if (Build.VERSION.SDK_INT < 24) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milis);
        return new SimpleDateFormat(pattern).format(calendar.getTime());
    }

    /* access modifiers changed from: package-private */
    public void milis2DateTime() {
        Date currentDate = new Date(1622107972000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        PrintStream printStream = System.out;
        printStream.println("myDate" + sdf.format(currentDate));
    }


    public void displayNotification(Context context, String title, String details) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channel ID and name
        String channelId = "paymentVerification";
        String channelName = "paymentVerification";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create notification channel for Android Oreo and higher
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("bKash message parsing for Job Vocabulary payment");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(details)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true); // Remove notification when user taps on it


        Intent notificationIntent  = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("triggerFrom", "main");


        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 123, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 123, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        notificationBuilder.setContentIntent(pendingIntent);

        // Generate a unique notification ID using current timestamp
        int notificationId = (int) System.currentTimeMillis();

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }




}
