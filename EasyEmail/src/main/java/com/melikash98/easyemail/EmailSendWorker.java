package com.melikash98.easyemail;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.util.List;

import javax.xml.transform.Result;

public class EmailSendWorker extends Worker {

    private static final String TAG = "EasyEmail_Worker";
    public  static final int    MAX_RETRY = 3;

    public EmailSendWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        PendingEmailDao dao = EasyEmailDatabase.getInstance(ctx).pendingEmailDao();
        List<PendingEmail> jobs = dao.getPending();

        if (jobs.isEmpty()) return Result.success();

        // Rebuild config from stored payload (config fields are embedded in payload)
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

    private void handleFailure(PendingEmailDao dao, PendingEmail job) {
        if (job.retryCount >= MAX_RETRY - 1) {
            dao.updateStatus(job.id, "FAILED");
            Log.w(TAG, "Job permanently failed: " + job.id);
        } else {
            dao.updateStatus(job.id, "PENDING");
        }
    }
}
