package com.melikash98.easyemail;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailSender {
    private static final String TAG = "EasyEmail_Sender";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    static void send(
            EmailJsConfig config,
            String ownerEmail,
            String ownerName,
            String ownerPhotoUrl,
            String userName,
            String userEmail,
            String userPhone,
            String term,
            String time,
            String message,
            String itemId,
            String categoriesId,
            String userUid,
            EmailCallback callback
    ) {
        final String inquiryId = UUID.randomUUID().toString();
        OkHttpClient client = new OkHttpClient();

        try {
            String formattedTime = (time != null && !time.isEmpty())
                    ? time
                    : new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            JSONObject params = new JSONObject();
            params.put("name", safe(userName));
            params.put("user_name", safe(userName));
            params.put("time", formattedTime);
            params.put("owner_email", safe(ownerEmail));
            params.put("owner_name", safe(ownerName));
            params.put("owner_photo_url", safe(ownerPhotoUrl));
            params.put("app_email", safe(config.getAppEmail()));
            params.put("user_email", safe(userEmail));
            params.put("user_phone", safe(userPhone));
            params.put("term", safe(term));
            params.put("message", safe(message));
            params.put("item_id", safe(itemId));
            params.put("categories_id", safe(categoriesId));
            params.put("inquiry_id", inquiryId);

            JSONObject body = new JSONObject();
            body.put("service_id", config.getServiceId());
            body.put("template_id", config.getInquiryTemplateId());
            body.put("user_id", config.getPublicKey());
            body.put("template_params", params);

            Log.d(TAG, "Sending inquiry → " + ownerEmail + " | id=" + inquiryId);

            // Pre-save to Firebase (optimistic)
            if (config.isFirebaseEnabled() && userUid != null) {
                FirebaseEmailUtil.saveUserInquiry(
                        config, userUid, inquiryId, itemId,
                        ownerEmail, ownerName, ownerPhotoUrl, userName, categoriesId);
                FirebaseEmailUtil.saveGlobalInquiry(
                        config, inquiryId, itemId, userUid,
                        ownerEmail, ownerName, ownerPhotoUrl, userName, categoriesId);
            }

            RequestBody rb = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(config.getEmailJsApiUrl())
                    .post(rb)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("origin", "http://localhost")
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network failure", e);
                    mainHandler.post(() -> {
                        if (callback != null) callback.onError("Network error: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resp = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Response [" + response.code() + "]: " + resp);
                    if (response.isSuccessful()) {
                        mainHandler.post(() -> {
                            if (callback != null) callback.onSuccess();
                        });
                    } else {
                        mainHandler.post(() -> {
                            if (callback != null)
                                callback.onError("EmailJS " + response.code() + ": " + resp);
                        });
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception building inquiry", e);
            if (callback != null) callback.onError("Internal error: " + e.getMessage());
        }
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

}
