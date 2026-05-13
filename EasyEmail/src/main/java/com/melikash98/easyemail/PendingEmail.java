package com.melikash98.easyemail;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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