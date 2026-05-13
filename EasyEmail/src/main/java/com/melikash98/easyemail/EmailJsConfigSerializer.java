package com.melikash98.easyemail;

import org.json.JSONObject;

public class EmailJsConfigSerializer {
    public static JSONObject toJson(EmailJsConfig c) throws Exception {
        JSONObject j = new JSONObject();
        j.put("serviceId",          c.getServiceId());
        j.put("publicKey",          c.getPublicKey());
        j.put("inquiryTemplateId",  c.getInquiryTemplateId());
        j.put("replyTemplateId",    c.getReplyTemplateId());
        j.put("emailJsApiUrl",      c.getEmailJsApiUrl());
        j.put("appEmail",           c.getAppEmail());
        j.put("appPassword",        c.getAppPassword());
        j.put("imapHost",           c.getImapHost());
        j.put("imapPort",           c.getImapPort());
        j.put("firebaseEnabled",    c.isFirebaseEnabled());
        j.put("firebaseInquiryRoot",c.getFirebaseInquiryRoot());
        j.put("firebaseUserRoot",   c.getFirebaseUserRoot());
        j.put("notificationsEnabled", c.isNotificationsEnabled());
        j.put("notificationTitle",  c.getNotificationTitle());
        j.put("notificationBody",   c.getNotificationBody());
        return j;
    }

    public static EmailJsConfig fromJson(JSONObject j) throws Exception {
        return new EmailJsConfig.Builder()
                .setServiceId(          j.optString("serviceId"))
                .setPublicKey(          j.optString("publicKey"))
                .setInquiryTemplateId(  j.optString("inquiryTemplateId"))
                .setReplyTemplateId(    j.optString("replyTemplateId"))
                .setEmailJsApiUrl(      j.optString("emailJsApiUrl"))
                .setAppEmail(           j.optString("appEmail"))
                .setAppPassword(        j.optString("appPassword"))
                .setImapHost(           j.optString("imapHost"))
                .setImapPort(           j.optInt("imapPort", 993))
                .setFirebaseEnabled(    j.optBoolean("firebaseEnabled", true))
                .setFirebaseInquiryRoot(j.optString("firebaseInquiryRoot"))
                .setFirebaseUserRoot(   j.optString("firebaseUserRoot"))
                .setNotificationsEnabled(j.optBoolean("notificationsEnabled", true))
                .setNotificationTitle(  j.optString("notificationTitle"))
                .setNotificationBody(   j.optString("notificationBody"))
                .build();
    }
}
