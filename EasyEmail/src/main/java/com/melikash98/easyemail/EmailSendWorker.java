package com.melikash98.easyemail;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.util.List;

/**
 * Background worker responsible for sending queued emails.
 * <p>
 * This worker is triggered by WorkManager when network connectivity is available.
 * It reads pending email jobs from the local Room database, restores the payload,
 * and sends each job using the appropriate sender implementation.
 * <p>
 * If sending fails, the job is retried until the maximum retry limit is reached.
 */

public class EmailSendWorker extends Worker {

    private static final String TAG = "EasyEmail_Worker";
    public static final int MAX_RETRY = 3;

    /**
     * Creates a new worker instance.
     *
     * @param context application context provided by WorkManager
     * @param params  worker runtime parameters
     */

    public EmailSendWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Executes the background email sending task.
     * <p>
     * The worker performs the following steps:
     * <ol>
     *     <li>Loads all pending jobs from the local database</li>
     *     <li>Restores each job payload from JSON</li>
     *     <li>Chooses the correct sender based on job type</li>
     *     <li>Deletes successful jobs from the queue</li>
     *     <li>Updates retry status for failed jobs</li>
     * </ol>
     *
     * @return {@link Result#success()} when processing completes
     */

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        PendingEmailDao dao = EasyEmailDatabase.getInstance(ctx).pendingEmailDao();
        List<PendingEmail> jobs = dao.getPending();

        if (jobs.isEmpty()) return Result.success();
        for (PendingEmail job : jobs) {
            try {
                dao.updateStatus(job.id, "SENDING");
                JSONObject payload = new JSONObject(job.payload);
                EmailJsConfig config = EmailJsConfigSerializer.fromJson(
                        payload.getJSONObject("config"));

                boolean sent = false;

                if (PendingEmail.TYPE_INQUIRY.equals(job.type)) {
                    sent = EmailSender.sendSync(config, payload);
                } else if (PendingEmail.TYPE_REPLY.equals(job.type)) {
                    sent = ReplyEmailSender.sendSync(config, payload);
                }

                if (sent) {
                    dao.delete(job.id);
                    Log.d(TAG, "Job sent and removed: " + job.id);
                    EmailStateLiveData.getInstance().post(EmailState.success());
                } else {
                    handleFailure(dao, job);
                }

            } catch (Exception e) {
                Log.e(TAG, "Worker error for job " + job.id, e);
                handleFailure(dao, job);
            }
        }
        return Result.success();
    }

    /**
     * Handles a failed send attempt.
     * <p>
     * If the retry limit has been reached, the job is marked as failed permanently.
     * Otherwise, it is returned to the pending state so WorkManager can try again later.
     *
     * @param dao database access object used to update the job state
     * @param job the failed email job
     */

    private void handleFailure(PendingEmailDao dao, PendingEmail job) {
        if (job.retryCount >= MAX_RETRY - 1) {
            dao.updateStatus(job.id, "FAILED");
            Log.w(TAG, "Job permanently failed: " + job.id);
            EmailStateLiveData.getInstance().post(
                    EmailState.failed("Send email after " + MAX_RETRY + "The attempt was unsuccessful."));
        } else {
            dao.updateStatus(job.id, "PENDING");
        }
    }
}
