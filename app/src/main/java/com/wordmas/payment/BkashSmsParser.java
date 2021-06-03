package com.wordmas.payment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BkashSmsParser {
    String sender = null, amount = null, trxId = null, receivedAt = null;

    BkashSmsParser(String sms){
        /*
        sample SMS pattern :
        You have received Tk 7,140.00 from 01732355358. Ref 1. Fee Tk 0.00. Balance Tk 7,284.73. TrxID 8DU7556HYF at 30/04/2021 18:15
        */
        Pattern pattern = Pattern.compile("You have received Tk .* from");
        Matcher matcher = pattern.matcher(sms);
        while (matcher.find()) {
            //System.out.println(matcher.group(0));
            String s = matcher.group(0);
            s = s.replaceAll("You have received Tk ", "");
            s = s.replaceAll(" from", "");
            s = s.replaceAll(",", "");
            amount = s;
        }

        pattern = Pattern.compile("from [0-9]{11}");
        matcher = pattern.matcher(sms);
        while (matcher.find()) {
            //System.out.println(matcher.group(0));
            String s = matcher.group(0);
            s = s.replace("from ", "");
            sender = s;
        }

        pattern = Pattern.compile("TrxID \\w* at");
        matcher = pattern.matcher(sms);
        while (matcher.find()) {
            //System.out.println(matcher.group(0));
            String s = matcher.group(0);
            s = s.replace("TrxID ", "");
            s = s.replace(" at", "");
            trxId = s;
        }

        String s = sms.substring(sms.indexOf("at") + 3);
        try {
            Date dateObj = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(s);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(dateObj);
            //System.out.println(dateString);
            receivedAt = dateString;
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        //System.out.println("amount=" + amount + "  sender=" + sender + "  TrxID=" + trxId + " received at=" + receivedAt);
    }

    public String getSender(){
        return sender;
    }
    public String getAmount(){
        return amount;
    }
    public String getTrxID(){
        return trxId;
    }
    public String getReceivedAt(){
        return receivedAt;
    }
}
