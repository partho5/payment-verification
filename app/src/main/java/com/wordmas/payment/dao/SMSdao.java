package com.wordmas.payment.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.wordmas.payment.model.SMS;

import java.util.List;

@Dao
public interface SMSdao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SMS sms);

    @Query("SELECT * FROM sms")
    List<SMS> getAllSms();

    @Query("SELECT * FROM sms where smsId=:smsId")
    List<SMS> getBySmsId(int smsId);

    @Query("SELECT * FROM sms where sentToServer=0")
    List<SMS> getSmsToSync();

    @Query("delete from sms where 1")
    void deleteAll();

    @Query("delete from sms where smsId IN(:smsIds)")
    void deleteBySmsIds(List<Integer> smsIds);

    @Query("update sms set sentToServer=1 where smsId IN(:smsIds)")
    void makeSynced(List<Integer> smsIds);
}

