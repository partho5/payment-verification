package com.wordmas.payment.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.wordmas.payment.dao.SMSdao;
import com.wordmas.payment.model.SMS;


@Database(
        entities = {
                SMS.class
        },
        version = 5, exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SMSdao smSdao();
}
