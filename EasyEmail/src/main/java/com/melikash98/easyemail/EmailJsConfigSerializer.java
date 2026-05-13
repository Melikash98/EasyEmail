package com.melikash98.easyemail;

import org.json.JSONObject;


/**
 * Utility class for converting {@link EmailJsConfig} objects to and from JSON.
 * <p>
 * This serializer is used when EasyEmail needs to persist configuration data
 * or restore it later from a stored JSON representation.
 * <p>
 * All serialization and deserialization logic is centralized here to keep the
 * rest of the codebase clean and maintainable.
 */

public class EmailJsConfigSerializer {

    /**
     * Converts an {@link EmailJsConfig} instance into a JSON object.
     *
     * @param c the configuration object to serialize
     * @return a {@link JSONObject} containing all config fields
     * @throws Exception if JSON creation fails for any reason
     */

    public static JSONObject toJson(EmailJsConfig c) throws Exception {
        JSONObject j = new JSONObject();
        j.put("serviceId", c.getServiceId());
        j.put("publicKey", c.getPublicKey());
        j.put("inquiryTemplateId", c.getInquiryTemplateId());
        j.put("replyTemplateId", c.getReplyTemplateId());
        j.put("emailJsApiUrl", c.getEmailJsApiUrl());
        j.put("appEmail", c.getAppEmail());
        j.put("appPassword", c.getAppPassword());
        j.put("imapHost", c.getImapHost());
        j.put("imapPort", c.getImapPort());
        j.put("firebaseEnabled", c.isFirebaseEnabled());
        j.put("firebaseInquiryRoot", c.getFirebaseInquiryRoot());
        j.put("firebaseUserRoot", c.getFirebaseUserRoot());
        j.put("notificationsEnabled", c.isNotificationsEnabled());
        j.put("notificationTitle", c.getNotificationTitle());
        j.put("notificationBody", c.getNotificationBody());
        return j;
    }


    /**
     * Builds an {@link EmailJsConfig} instance from a JSON object.
     * <p>
     * Missing values are replaced with safe defaults where applicable.
     *
     * @param j the JSON object containing configuration data
     * @return a fully built {@link EmailJsConfig} instance
     * @throws Exception if building the configuration fails
     */

    public static EmailJsConfig fromJson(JSONObject j) throws Exception {
        return new EmailJsConfig.Builder()
                .setServiceId(j.optString("serviceId"))
                .setPublicKey(j.optString("publicKey"))
                .setInquiryTemplateId(j.optString("inquiryTemplateId"))
                .setReplyTemplateId(j.optString("replyTemplateId"))
                .setEmailJsApiUrl(j.optString("emailJsApiUrl"))
                .setAppEmail(j.optString("appEmail"))
                .setAppPassword(j.optString("appPassword"))
                .setImapHost(j.optString("imapHost"))
                .setImapPort(j.optInt("imapPort", 993))
                .setFirebaseEnabled(j.optBoolean("firebaseEnabled", true))
                .setFirebaseInquiryRoot(j.optString("firebaseInquiryRoot"))
                .setFirebaseUserRoot(j.optString("firebaseUserRoot"))
                .setNotificationsEnabled(j.optBoolean("notificationsEnabled", true))
                .setNotificationTitle(j.optString("notificationTitle"))
                .setNotificationBody(j.optString("notificationBody"))
                .build();
    }
}
