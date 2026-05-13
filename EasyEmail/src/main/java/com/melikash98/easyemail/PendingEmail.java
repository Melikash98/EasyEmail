package com.melikash98.easyemail;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a queued email job.
 * <p>
 * This model is used to persist emails locally before they are successfully sent.
 * It allows EasyEmail to support retries, offline queueing, and background delivery
 * using WorkManager.
 * <p>
 * Stored data includes:
 * <ul>
 *     <li>Job identifier</li>
 *     <li>Email type (inquiry or reply)</li>
 *     <li>Serialized JSON payload</li>
 *     <li>Retry count</li>
 *     <li>Creation timestamp</li>
 *     <li>Current processing status</li>
 * </ul>
 */

@Entity(tableName = "pending_emails")
public class PendingEmail {

    public static final String TYPE_INQUIRY = "INQUIRY";
    public static final String TYPE_REPLY   = "REPLY";

    @PrimaryKey
    @NonNull
    public String id = "";

    public String type = "";
    public String payload = "";
    public int    retryCount = 0;
    public long   createdAt = 0L;
    public String status = "PENDING";
}