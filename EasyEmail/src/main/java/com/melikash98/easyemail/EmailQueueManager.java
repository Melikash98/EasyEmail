package com.melikash98.easyemail;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.BackoffPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EmailQueueManager {
    private static final String TAG      = "EasyEmail_Queue";
    private static final String WORK_TAG = "easy_email_send";

    private final Context        context;
    private final EmailJsConfig  config;
    private final PendingEmailDao dao;

    public EmailQueueManager(Context context, EmailJsConfig config) {
        this.context = context.getApplicationContext();
        this.config  = config;
        this.dao     = EasyEmailDatabase.getInstance(this.context).pendingEmailDao();
    }
    public void enqueueInquiry(
            String ownerEmail, String ownerName, String ownerPhotoUrl,
            String userName,   String userEmail,  String userPhone,
            String term,       String time,       String message,
            String itemId,     String categoriesId, String userUid,
            Map<String, String> extraParams,
            EmailCallback callback
    ) {
        try {
            JSONObject payload = EmailPayloadBuilder.buildInquiry(
                    config, ownerEmail, ownerName, ownerPhotoUrl,
                    userName, userEmail, userPhone,
                    term, time, message, itemId, categoriesId, userUid,
                    extraParams
            );
            enqueue(PendingEmail.TYPE_INQUIRY, payload, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError("Queue error: " + e.getMessage());
        }
    }

    public void enqueueReply(
            String ownerEmail,   String ownerName,   String replyMessage,
            String inquiryId,    String itemId,
            String userName,     String userEmail,   String userPhotoUrl,
            String userUid,
            Map<String, String> extraParams,
            EmailCallback callback
    ) {
        try {
            JSONObject payload = EmailPayloadBuilder.buildReply(
                    config, ownerEmail, ownerName, replyMessage,
                    inquiryId, itemId, userName, userEmail,
                    userPhotoUrl, userUid, extraParams
            );
            enqueue(PendingEmail.TYPE_REPLY, payload, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError("Queue error: " + e.getMessage());
        }
    }

    private void enqueue(String type, JSONObject payload, EmailCallback callback) {
        new Thread(() -> {
            try {
                String jobId = UUID.randomUUID().toString();

                PendingEmail job = new PendingEmail();
                job.id        = jobId;
                job.type      = type;
                job.payload   = payload.toString();
                job.createdAt = System.currentTimeMillis();
                job.status    = "PENDING";

                EmailStateLiveData.getInstance().post(EmailState.loading());

                dao.insert(job);
                scheduleWorker();

                Log.d(TAG, "Enqueued job: " + jobId + " type=" + type
                        + " online=" + isOnline());

                if (!isOnline()) {
                    List<PendingEmail> pending = dao.getPending();
                    int count = pending.size();
                    EmailStateLiveData.getInstance().post(EmailState.queued(count));
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onError("QUEUED: ایمیل در صف قرار گرفت"));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Enqueue failed", e);
                EmailStateLiveData.getInstance().post(EmailState.failed(e.getMessage()));
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError("Enqueue failed: " + e.getMessage()));
                }
            }
        }).start();
    }

    private void scheduleWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(EmailSendWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(WORK_TAG)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.KEEP, request);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
