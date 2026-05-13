package com.melikash98.easyemail;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PendingEmail.class}, version = 1, exportSchema = false)
public abstract class EasyEmailDatabase extends RoomDatabase {

    private static volatile EasyEmailDatabase INSTANCE;

    public abstract PendingEmailDao pendingEmailDao();

    public static EasyEmailDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (EasyEmailDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    EasyEmailDatabase.class,
                                    "easy_email_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}