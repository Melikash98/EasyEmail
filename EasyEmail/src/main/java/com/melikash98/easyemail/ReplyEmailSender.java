package com.melikash98.easyemail;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReplyEmailSender {
    private static final String TAG = "EasyEmail_ReplySender";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    static void send(
            EmailJsConfig config,
            String ownerEmail,
            String ownerName,
            String replyMessage,
            String inquiryId,
            String itemId,
            String userName,
            String userEmail,
            String userPhotoUrl,
            String userUid,
            @Nullable Map<String, String> extraParams,
            EmailCallback callback
    ) {
        OkHttpClient client = new OkHttpClient();

        try {
            String formattedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date());

            JSONObject params = new JSONObject();

            for (Map.Entry<String, String> entry : config.getDefaultReplyParams().entrySet()) {
                params.put(entry.getKey(), safe(entry.getValue()));
            }
            params.put("owner_email",    safe(ownerEmail));
            params.put("owner_name",     safe(ownerName));
            params.put("app_email",      safe(config.getAppEmail()));
            params.put("reply_message",  safe(replyMessage));
            params.put("user_name",      safe(userName));
            params.put("name",           safe(userName));
            params.put("user_photo_url", safe(userPhotoUrl));
            params.put("item_id",        safe(itemId));
            params.put("inquiry_id",     safe(inquiryId));
            params.put("time",           formattedTime);

            if (extraParams != null) {
                for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                    params.put(entry.getKey(), safe(entry.getValue()));
                }
            }

            JSONObject body = new JSONObject();
            body.put("service_id",      config.getServiceId());
            body.put("template_id",     config.getReplyTemplateId());
            body.put("user_id",         config.getPublicKey());
            body.put("template_params", params);

            Log.d(TAG, "Sending reply → " + ownerEmail + " | inquiryId=" + inquiryId);

            RequestBody rb = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(config.getEmailJsApiUrl())
                    .post(rb)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("origin", "http://localhost")
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resp = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Reply sent successfully.");
                        if (config.isFirebaseEnabled() && userUid != null) {
                            FirebaseEmailUtil.saveUserReply(
                                    config, userUid, inquiryId,
                                    replyMessage, userName, userEmail, userPhotoUrl);
                        }
                        mainHandler.post(() -> {
                            if (callback != null) callback.onSuccess();
                        });
                    } else {
                        Log.e(TAG, "EmailJS " + response.code() + ": " + resp);
                        mainHandler.post(() -> {
                            if (callback != null)
                                callback.onError("EmailJS " + response.code() + ": " + resp);
                        });
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network failure sending reply", e);
                    mainHandler.post(() -> {
                        if (callback != null) callback.onError("Network error: " + e.getMessage());
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception building reply", e);
            if (callback != null) callback.onError("Internal error: " + e.getMessage());
        }
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
