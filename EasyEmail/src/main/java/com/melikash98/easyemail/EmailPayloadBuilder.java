package com.melikash98.easyemail;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


/**
 * Utility class responsible for building JSON payloads used by EasyEmail requests.
 * <p>
 * This class prepares the data sent for:
 * <ul>
 *     <li>Inquiry email requests</li>
 *     <li>Reply email requests</li>
 * </ul>
 * <p>
 * It also ensures that all nullable string values are converted into safe non-null
 * values before being added to the final JSON object.
 */

public class EmailPayloadBuilder {

    /**
     * Builds the JSON payload for sending an inquiry email.
     * <p>
     * This payload includes all inquiry-related fields such as owner information,
     * user information, item details, config data, and optional extra parameters.
     * A unique inquiry ID is generated automatically.
     * <p>
     * If the {@code time} value is empty or null, the current date/time will be used
     * as a fallback.
     *
     * @param config        the current EasyEmail configuration
     * @param ownerEmail    owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL
     * @param userName      name of the user sending the inquiry
     * @param userEmail     email address of the user
     * @param userPhone     user's phone number
     * @param term          inquiry term or duration
     * @param time          inquiry time; if empty, current date/time is used
     * @param message       inquiry message content
     * @param itemId        ID of the related item
     * @param categoriesId  category ID of the related item
     * @param userUid       unique user identifier
     * @param extraParams   optional extra template parameters; may be null
     * @return a {@link JSONObject} containing the full inquiry payload
     * @throws Exception if JSON creation fails
     */

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
        p.put("inquiryId", inquiryId);
        p.put("ownerEmail", safe(ownerEmail));
        p.put("ownerName", safe(ownerName));
        p.put("ownerPhotoUrl", safe(ownerPhotoUrl));
        p.put("userName", safe(userName));
        p.put("userEmail", safe(userEmail));
        p.put("userPhone", safe(userPhone));
        p.put("term", safe(term));
        p.put("time", formattedTime);
        p.put("message", safe(message));
        p.put("itemId", safe(itemId));
        p.put("categoriesId", safe(categoriesId));
        p.put("userUid", safe(userUid));
        p.put("config", EmailJsConfigSerializer.toJson(config));
        if (extraParams != null) {
            JSONObject extra = new JSONObject();
            for (Map.Entry<String, String> e : extraParams.entrySet())
                extra.put(e.getKey(), safe(e.getValue()));
            p.put("extraParams", extra);
        }
        return p;
    }

    /**
     * Builds the JSON payload for sending a reply email.
     * <p>
     * This payload includes owner information, reply content, inquiry reference data,
     * user information, config data, and optional extra parameters.
     *
     * @param config       the current EasyEmail configuration
     * @param ownerEmail   replying owner's email address
     * @param ownerName    replying owner's display name
     * @param replyMessage reply message content
     * @param inquiryId    ID of the original inquiry
     * @param itemId       ID of the related item
     * @param userName     original user's display name
     * @param userEmail    original user's email address
     * @param userPhotoUrl original user's profile photo URL
     * @param userUid      unique identifier of the original user
     * @param extraParams  optional extra template parameters; may be null
     * @return a {@link JSONObject} containing the full reply payload
     * @throws Exception if JSON creation fails
     */

    public static JSONObject buildReply(
            EmailJsConfig config,
            String ownerEmail, String ownerName, String replyMessage,
            String inquiryId, String itemId,
            String userName, String userEmail, String userPhotoUrl,
            String userUid, Map<String, String> extraParams
    ) throws Exception {
        JSONObject p = new JSONObject();
        p.put("ownerEmail", safe(ownerEmail));
        p.put("ownerName", safe(ownerName));
        p.put("replyMessage", safe(replyMessage));
        p.put("inquiryId", safe(inquiryId));
        p.put("itemId", safe(itemId));
        p.put("userName", safe(userName));
        p.put("userEmail", safe(userEmail));
        p.put("userPhotoUrl", safe(userPhotoUrl));
        p.put("userUid", safe(userUid));
        p.put("config", EmailJsConfigSerializer.toJson(config));
        if (extraParams != null) {
            JSONObject extra = new JSONObject();
            for (Map.Entry<String, String> e : extraParams.entrySet())
                extra.put(e.getKey(), safe(e.getValue()));
            p.put("extraParams", extra);
        }
        return p;
    }

    /**
     * Safely converts a string into a non-null value.
     * <p>
     * Returns an empty string when the input is null to prevent JSON or template issues.
     *
     * @param s input string
     * @return the original string, or an empty string if null
     */

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
