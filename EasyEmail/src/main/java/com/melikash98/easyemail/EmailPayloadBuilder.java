package com.melikash98.easyemail;

import com.melikash98.easyemail.Config.EmailJsConfig;
import com.melikash98.easyemail.Config.EmailJsConfigSerializer;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EmailPayloadBuilder {
    public static JSONObject buildInquiry(
            EmailJsConfig config,
            String ownerEmail, String ownerName, String ownerPhotoUrl,
            String userName, String userEmail, String userPhone,
            String term, String time, String message,
            String itemId, String categoriesId, String userUid,
            Map<String, String> extraParams
    ) throws Exception {
        String inquiryId = UUID.randomUUID().toString();
        String formattedTime = (time != null && !time.isEmpty())
                ? time
                : new SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(new Date());

        JSONObject p = new JSONObject();
        p.put("inquiryId",     inquiryId);
        p.put("ownerEmail",    safe(ownerEmail));
        p.put("ownerName",     safe(ownerName));
        p.put("ownerPhotoUrl", safe(ownerPhotoUrl));
        p.put("userName",      safe(userName));
        p.put("userEmail",     safe(userEmail));
        p.put("userPhone",     safe(userPhone));
        p.put("term",          safe(term));
        p.put("time",          formattedTime);
        p.put("message",       safe(message));
        p.put("itemId",        safe(itemId));
        p.put("categoriesId",  safe(categoriesId));
        p.put("userUid",       safe(userUid));
        p.put("config",        EmailJsConfigSerializer.toJson(config));
        if (extraParams != null) {
            JSONObject extra = new JSONObject();
            for (Map.Entry<String, String> e : extraParams.entrySet())
                extra.put(e.getKey(), safe(e.getValue()));
            p.put("extraParams", extra);
        }
        return p;
    }

    public static JSONObject buildReply(
            EmailJsConfig config,
            String ownerEmail, String ownerName, String replyMessage,
            String inquiryId, String itemId,
            String userName, String userEmail, String userPhotoUrl,
            String userUid, Map<String, String> extraParams
    ) throws Exception {
        JSONObject p = new JSONObject();
        p.put("ownerEmail",    safe(ownerEmail));
        p.put("ownerName",     safe(ownerName));
        p.put("replyMessage",  safe(replyMessage));
        p.put("inquiryId",     safe(inquiryId));
        p.put("itemId",        safe(itemId));
        p.put("userName",      safe(userName));
        p.put("userEmail",     safe(userEmail));
        p.put("userPhotoUrl",  safe(userPhotoUrl));
        p.put("userUid",       safe(userUid));
        p.put("config",        EmailJsConfigSerializer.toJson(config));
        if (extraParams != null) {
            JSONObject extra = new JSONObject();
            for (Map.Entry<String, String> e : extraParams.entrySet())
                extra.put(e.getKey(), safe(e.getValue()));
            p.put("extraParams", extra);
        }
        return p;
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
