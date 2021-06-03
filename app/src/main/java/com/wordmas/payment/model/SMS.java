package com.wordmas.payment.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SMS {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int smsId;
    public String message;
    public String senderAddress;
    public boolean sentToServer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSmsId() {
        return smsId;
    }

    public void setSmsId(int smsId) {
        this.smsId = smsId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public boolean isSentToServer() {
        return sentToServer;
    }

    public void setSentToServer(boolean sentToServer) {
        this.sentToServer = sentToServer;
    }
}
