package com.wordmas.payment.api;

import android.media.MediaDrm;
import android.util.Base64;

import java.util.UUID;

public class ApiUtils {

    public String baseDomain(){
        return "https://jovoc.com";
    }

    public String deviceId(){
        UUID wideVineUuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
        try {
            MediaDrm wvDrm = new MediaDrm(wideVineUuid);
            byte[] wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            return Base64.encodeToString(wideVineId, Base64.DEFAULT);
        } catch (Exception e) {
            //MediaDrm not available
            return null;
        }
    }
}
