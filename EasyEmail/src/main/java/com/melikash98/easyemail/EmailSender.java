package com.melikash98.easyemail;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.melikash98.easyemail.Config.EmailJsConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Handles sending inquiry emails via the EmailJS HTTP API.
 * <p>
 * This class is used internally by {@link EasyEmail#sendInquiry}.
 * It builds the JSON payload, optionally saves the inquiry to Firebase,
 * posts the request asynchronously using OkHttp, and delivers the result
 * back on the main thread via {@link EmailCallback}.
 * <p>
 * This class is not meant to be instantiated — all methods are package-private static.
 */

public class EmailSender {
    private static final String TAG = "EasyEmail_Sender";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());


    /**
     * Builds and sends an inquiry email to the item owner via EmailJS.
     * <p>
     * Steps performed:
     * 1. Generates a unique inquiryId (UUID) for tracking this inquiry
     * 2. Resolves the display time — uses provided value or current timestamp
     * 3. Builds the EmailJS template_params JSON from all provided fields
     * 4. Merges defaultInquiryParams from config and any extraParams on top
     * 5. Optionally saves the inquiry to Firebase (if firebaseEnabled)
     * 6. Posts the request asynchronously via OkHttp
     * 7. Delivers onSuccess or onError back on the main thread
     *
     * @param config        the EmailJS and IMAP configuration
     * @param ownerEmail    recipient owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL (used in email template)
     * @param userName      inquiring user's display name
     * @param userEmail     inquiring user's email address
     * @param userPhone     inquiring user's phone number
     * @param term          rental or purchase term (e.g. "6 months")
     * @param time          desired start time; falls back to current date/time if null or empty
     * @param message       the user's inquiry message body
     * @param itemId        ID of the item being inquired about
     * @param categoriesId  category ID of the item
     * @param userUid       Firebase UID of the inquiring user; used for Firebase saving
     * @param extraParams   optional extra key-value pairs to merge into the template params; may be null
     * @param callback      receives onSuccess or onError on the main thread
     */

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
            @Nullable Map<String, String> extraParams,
            EmailCallback callback
    ) {
        final String inquiryId = UUID.randomUUID().toString();
        OkHttpClient client = new OkHttpClient();

        try {
            String formattedTime = (time != null && !time.isEmpty())
                    ? time
                    : new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            JSONObject params = new JSONObject();
            for (Map.Entry<String, String> entry : config.getDefaultInquiryParams().entrySet()) {
                params.put(entry.getKey(), safe(entry.getValue()));
            }

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

            if (extraParams != null) {
                for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                    params.put(entry.getKey(), safe(entry.getValue()));
                }
            }

            JSONObject body = new JSONObject();
            body.put("service_id", config.getServiceId());
            body.put("template_id", config.getInquiryTemplateId());
            body.put("user_id", config.getPublicKey());
            body.put("template_params", params);

            Log.d(TAG, "Sending inquiry → " + ownerEmail + " | id=" + inquiryId);

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
    /**
     * Synchronous version — called by EmailSendWorker on a background thread.
     * Returns true if EmailJS returned 2xx, false otherwise.
     */
    static boolean sendSync(EmailJsConfig config, JSONObject payload) {
        try {
            String inquiryId     = payload.optString("inquiryId");
            String ownerEmail    = payload.optString("ownerEmail");
            String ownerName     = payload.optString("ownerName");
            String ownerPhotoUrl = payload.optString("ownerPhotoUrl");
            String userName      = payload.optString("userName");
            String userEmail     = payload.optString("userEmail");
            String userPhone     = payload.optString("userPhone");
            String term          = payload.optString("term");
            String time          = payload.optString("time");
            String message       = payload.optString("message");
            String itemId        = payload.optString("itemId");
            String categoriesId  = payload.optString("categoriesId");
            String userUid       = payload.optString("userUid");

            JSONObject params = new JSONObject();
            for (Map.Entry<String, String> entry : config.getDefaultInquiryParams().entrySet())
                params.put(entry.getKey(), safe(entry.getValue()));

            params.put("name",            safe(userName));
            params.put("user_name",       safe(userName));
            params.put("time",            time);
            params.put("owner_email",     safe(ownerEmail));
            params.put("owner_name",      safe(ownerName));
            params.put("owner_photo_url", safe(ownerPhotoUrl));
            params.put("app_email",       safe(config.getAppEmail()));
            params.put("user_email",      safe(userEmail));
            params.put("user_phone",      safe(userPhone));
            params.put("term",            safe(term));
            params.put("message",         safe(message));
            params.put("item_id",         safe(itemId));
            params.put("categories_id",   safe(categoriesId));
            params.put("inquiry_id",      inquiryId);

            JSONObject extraJson = payload.optJSONObject("extraParams");
            if (extraJson != null) {
                Iterator<String> keys = extraJson.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    params.put(k, extraJson.optString(k));
                }
            }

            JSONObject body = new JSONObject();
            body.put("service_id",      config.getServiceId());
            body.put("template_id",     config.getInquiryTemplateId());
            body.put("user_id",         config.getPublicKey());
            body.put("template_params", params);

            OkHttpClient client = new OkHttpClient();
            RequestBody rb = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(config.getEmailJsApiUrl())
                    .post(rb)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("origin", "http://localhost")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                boolean ok = response.isSuccessful();
                if (ok && config.isFirebaseEnabled() && !userUid.isEmpty()) {
                    FirebaseEmailUtil.saveUserInquiry(config, userUid, inquiryId,
                            itemId, ownerEmail, ownerName, ownerPhotoUrl,
                            userName, categoriesId);
                    FirebaseEmailUtil.saveGlobalInquiry(config, inquiryId, itemId,
                            userUid, ownerEmail, ownerName, ownerPhotoUrl,
                            userName, categoriesId);
                }
                return ok;
            }

        } catch (Exception e) {
            Log.e(TAG, "sendSync failed", e);
            return false;
        }
    }

    /**
     * Returns the given string as-is, or an empty string if it is null.
     * Prevents null values from being serialized into the JSON payload.
     *
     * @param s the string to check
     * @return the original string, or "" if null
     */

    private static String safe(String s) {
        return s != null ? s : "";
    }

}
