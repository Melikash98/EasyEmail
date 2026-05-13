package com.melikash98.easyemail;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.melikash98.easyemail.Config.EmailJsConfig;
import com.melikash98.easyemail.Config.EmailJsConfigSerializer;
import com.melikash98.easyemail.Interface.PendingEmailDao;
import com.melikash98.easyemail.State.EmailState;
import com.melikash98.easyemail.State.EmailStateLiveData;

import org.json.JSONObject;

import java.util.List;

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
