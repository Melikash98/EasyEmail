package com.melikash98.easyemail;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PendingEmailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PendingEmail email);

    @Query("SELECT * FROM pending_emails WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<PendingEmail> getPending();

    @Query("UPDATE pending_emails SET status = :status, retryCount = retryCount + 1 WHERE id = :id")
    void updateStatus(String id, String status);

    @Query("DELETE FROM pending_emails WHERE id = :id")
    void delete(String id);

    @Query("SELECT COUNT(*) FROM pending_emails WHERE status = 'PENDING'")
    LiveData<Integer> getPendingCount();
}