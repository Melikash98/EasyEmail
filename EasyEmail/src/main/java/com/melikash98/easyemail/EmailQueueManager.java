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


/**
 * Manages the local queue of emails that are waiting to be sent.
 * <p>
 * This class is responsible for:
 * <ul>
 *     <li>Creating pending email jobs</li>
 *     <li>Storing them in the local Room database</li>
 *     <li>Scheduling background work to send them when network is available</li>
 *     <li>Notifying the caller about queue and error states</li>
 * </ul>
 * <p>
 * It acts as the bridge between the payload builders, the local database,
 * and the background worker responsible for sending emails.
 */

public class EmailQueueManager {
    private static final String TAG = "EasyEmail_Queue";
    private static final String WORK_TAG = "easy_email_send";

    private final Context context;
    private final EmailJsConfig config;
    private final PendingEmailDao dao;

    /**
     * Creates a new queue manager instance.
     *
     * @param context application or activity context; application context will be stored internally
     * @param config  current EasyEmail configuration
     */

    public EmailQueueManager(Context context, EmailJsConfig config) {
        this.context = context.getApplicationContext();
        this.config = config;
        this.dao = EasyEmailDatabase.getInstance(this.context).pendingEmailDao();
    }

    /**
     * Creates and enqueues an inquiry email job.
     * <p>
     * The inquiry payload is built first, then stored in the local queue.
     * If any error occurs while creating the payload, the callback is notified immediately.
     *
     * @param ownerEmail    owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL
     * @param userName      name of the user sending the inquiry
     * @param userEmail     user's email address
     * @param userPhone     user's phone number
     * @param term          inquiry term or duration
     * @param time          inquiry time or availability time
     * @param message       inquiry message content
     * @param itemId        item identifier
     * @param categoriesId  category identifier
     * @param userUid       unique user ID
     * @param extraParams   optional extra template parameters
     * @param callback      callback for success or error reporting
     */

    public void enqueueInquiry(
            String ownerEmail, String ownerName, String ownerPhotoUrl,
            String userName, String userEmail, String userPhone,
            String term, String time, String message,
            String itemId, String categoriesId, String userUid,
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

    /**
     * Creates and enqueues a reply email job.
     * <p>
     * The reply payload is built first, then stored in the local queue.
     * If any error occurs while creating the payload, the callback is notified immediately.
     *
     * @param ownerEmail   replying owner's email address
     * @param ownerName    replying owner's display name
     * @param replyMessage reply message content
     * @param inquiryId    original inquiry ID
     * @param itemId       item identifier
     * @param userName     original user's display name
     * @param userEmail    original user's email address
     * @param userPhotoUrl original user's profile photo URL
     * @param userUid      unique user ID
     * @param extraParams  optional extra template parameters
     * @param callback     callback for success or error reporting
     */

    public void enqueueReply(
            String ownerEmail, String ownerName, String replyMessage,
            String inquiryId, String itemId,
            String userName, String userEmail, String userPhotoUrl,
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

    /**
     * Inserts a prepared email job into the database and schedules the send worker.
     * <p>
     * This method runs on a background thread so database operations do not block
     * the main thread.
     *
     * @param type     email job type, such as inquiry or reply
     * @param payload  prepared JSON payload for the email
     * @param callback callback used to report queue or failure status
     */

    private void enqueue(String type, JSONObject payload, EmailCallback callback) {
        new Thread(() -> {
            try {
                String jobId = UUID.randomUUID().toString();

                PendingEmail job = new PendingEmail();
                job.id = jobId;
                job.type = type;
                job.payload = payload.toString();
                job.createdAt = System.currentTimeMillis();
                job.status = "PENDING";

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

    /**
     * Schedules a WorkManager job to send queued emails when the device is online.
     * <p>
     * The worker is constrained to run only when network connectivity is available.
     * A retry policy with exponential backoff is also applied to handle transient failures.
     */

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

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * @return true if the device is online and connected; otherwise false
     */

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
