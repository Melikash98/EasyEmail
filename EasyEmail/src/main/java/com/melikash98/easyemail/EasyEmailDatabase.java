package com.melikash98.easyemail;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Local Room database used by EasyEmail to store pending emails.
 * <p>
 * This database acts as an offline-safe queue for emails that could not be sent immediately.
 * By keeping pending emails locally, the library can retry sending them later when network
 * or server conditions become available.
 * <p>
 * The database is implemented as a singleton to ensure that only one instance exists
 * across the entire application process.
 */

@Database(entities = {PendingEmail.class}, version = 1, exportSchema = false)
public abstract class EasyEmailDatabase extends RoomDatabase {


    /**
     * Singleton instance of the database.
     * <p>
     * Declared volatile to ensure visibility across threads and support safe double-checked locking.
     */

    private static volatile EasyEmailDatabase INSTANCE;

    /**
     * Returns the DAO used to access pending email records.
     *
     * @return PendingEmailDao for reading and writing pending email items
     */

    public abstract PendingEmailDao pendingEmailDao();


    /**
     * Returns the singleton instance of the database.
     * <p>
     * If the database has not been created yet, it will be initialized lazily using
     * Room's database builder.
     *
     * @param context application or activity context; the application context will be used internally
     * @return the singleton EasyEmailDatabase instance
     */

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