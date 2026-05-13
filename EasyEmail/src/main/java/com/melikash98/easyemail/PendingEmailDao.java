package com.melikash98.easyemail;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data access object for pending email jobs stored in the local Room database.
 * <p>
 * This DAO provides the operations required to:
 * <ul>
 *     <li>Insert new queued emails</li>
 *     <li>Read pending jobs in sending order</li>
 *     <li>Update retry and status information</li>
 *     <li>Delete successfully processed jobs</li>
 *     <li>Observe the number of queued emails</li>
 * </ul>
 */

@Dao
public interface PendingEmailDao {

    /**
     * Inserts a new pending email into the database.
     * <p>
     * If a record with the same primary key already exists, it will be replaced.
     *
     * @param email pending email entity to store
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PendingEmail email);

    /**
     * Returns all emails that are still waiting to be sent.
     * <p>
     * Results are ordered by creation time so older jobs are processed first.
     *
     * @return list of pending email jobs
     */

    @Query("SELECT * FROM pending_emails WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<PendingEmail> getPending();

    /**
     * Updates the status of a pending email and increases its retry count.
     *
     * @param id     unique job identifier
     * @param status new status value
     */

    @Query("UPDATE pending_emails SET status = :status, retryCount = retryCount + 1 WHERE id = :id")
    void updateStatus(String id, String status);

    /**
     * Deletes a queued email job from the database.
     *
     * @param id unique job identifier
     */

    @Query("DELETE FROM pending_emails WHERE id = :id")
    void delete(String id);

    /**
     * Returns the number of emails that are still waiting to be sent.
     * <p>
     * This value is exposed as LiveData so the UI can observe queue changes.
     *
     * @return observable count of pending jobs
     */

    @Query("SELECT COUNT(*) FROM pending_emails WHERE status = 'PENDING'")
    LiveData<Integer> getPendingCount();
}