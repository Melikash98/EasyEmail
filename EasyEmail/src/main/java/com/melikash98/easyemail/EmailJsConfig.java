package com.melikash98.easyemail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for the EasyEmail library.
 * <p>
 * Holds all settings required for sending emails via EmailJS,
 * fetching replies via IMAP, and optionally integrating with
 * Firebase and push notifications.
 * <p>
 * Build an instance using the nested {@link Builder} class:
 * <p>
 * EmailJsConfig config = new EmailJsConfig.Builder()
 * .setServiceId("your_service_id")
 * .setPublicKey("your_public_key")
 * .setInquiryTemplateId("your_inquiry_template_id")
 * .setAppEmail("your@gmail.com")
 * .setAppPassword("your_app_password")
 * .build();
 * <p>
 * Once built, pass this config to {@link EasyEmail}.
 */

public class EmailJsConfig {
    private final String serviceId;
    private final String publicKey;
    private final String inquiryTemplateId;
    private final String replyTemplateId;
    private final String emailJsApiUrl;

    private final String appEmail;
    private final String appPassword;
    private final String imapHost;
    private final int imapPort;

    private final boolean firebaseEnabled;
    private final String firebaseInquiryRoot;
    private final String firebaseUserRoot;

    private final boolean notificationsEnabled;
    private final String notificationTitle;
    private final String notificationBody;

    private final Map<String, String> defaultInquiryParams;
    private final Map<String, String> defaultReplyParams;

    private EmailJsConfig(Builder builder) {
        this.serviceId = builder.serviceId;
        this.publicKey = builder.publicKey;
        this.inquiryTemplateId = builder.inquiryTemplateId;
        this.replyTemplateId = builder.replyTemplateId;
        this.emailJsApiUrl = builder.emailJsApiUrl;
        this.appEmail = builder.appEmail;
        this.appPassword = builder.appPassword;
        this.imapHost = builder.imapHost;
        this.imapPort = builder.imapPort;
        this.firebaseEnabled = builder.firebaseEnabled;
        this.firebaseInquiryRoot = builder.firebaseInquiryRoot;
        this.firebaseUserRoot = builder.firebaseUserRoot;
        this.notificationsEnabled = builder.notificationsEnabled;
        this.notificationTitle = builder.notificationTitle;
        this.notificationBody = builder.notificationBody;
        this.defaultInquiryParams = builder.defaultInquiryParams != null
                ? Collections.unmodifiableMap(new HashMap<>(builder.defaultInquiryParams))
                : Collections.emptyMap();
        this.defaultReplyParams = builder.defaultReplyParams != null
                ? Collections.unmodifiableMap(new HashMap<>(builder.defaultReplyParams))
                : Collections.emptyMap();
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getInquiryTemplateId() {
        return inquiryTemplateId;
    }

    public String getReplyTemplateId() {
        return replyTemplateId;
    }

    public String getEmailJsApiUrl() {
        return emailJsApiUrl;
    }

    public String getAppEmail() {
        return appEmail;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public String getImapHost() {
        return imapHost;
    }

    public int getImapPort() {
        return imapPort;
    }

    public boolean isFirebaseEnabled() {
        return firebaseEnabled;
    }

    public String getFirebaseInquiryRoot() {
        return firebaseInquiryRoot;
    }

    public String getFirebaseUserRoot() {
        return firebaseUserRoot;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public String getNotificationBody() {
        return notificationBody;
    }

    public Map<String, String> getDefaultInquiryParams() {
        return defaultInquiryParams;
    }

    public Map<String, String> getDefaultReplyParams() {
        return defaultReplyParams;
    }

    /**
     * Validates that all fields required for sending an inquiry are present.
     * <p>
     * Called automatically by {@link EasyEmail#sendInquiry} before making a request.
     *
     * @throws IllegalStateException if serviceId, publicKey, or inquiryTemplateId is missing
     */

    public void validateForSend() {
        if (isEmpty(serviceId)) throw new IllegalStateException("serviceId is required.");
        if (isEmpty(publicKey)) throw new IllegalStateException("publicKey is required.");
        if (isEmpty(inquiryTemplateId))
            throw new IllegalStateException("inquiryTemplateId is required.");
    }

    /**
     * Validates that all fields required for sending a reply are present.
     * <p>
     * Extends {@link #validateForSend()} with an additional check for replyTemplateId.
     * Called automatically by {@link EasyEmail#sendReply} before making a request.
     *
     * @throws IllegalStateException if any required field is missing
     */

    public void validateForReply() {
        validateForSend();
        if (isEmpty(replyTemplateId))
            throw new IllegalStateException("replyTemplateId is required.");
    }

    /**
     * Validates that all fields required for fetching replies via IMAP are present.
     * <p>
     * Called automatically by {@link EasyEmail#fetchOwnerReplies} before connecting.
     *
     * @throws IllegalStateException if appEmail, appPassword, imapHost, or imapPort is missing
     */

    public void validateForFetch() {
        if (isEmpty(appEmail)) throw new IllegalStateException("appEmail is required for IMAP.");
        if (isEmpty(appPassword))
            throw new IllegalStateException("appPassword is required for IMAP.");
        if (isEmpty(imapHost)) throw new IllegalStateException("imapHost is required for IMAP.");
        if (imapPort <= 0) throw new IllegalStateException("imapPort must be a valid port.");
    }

    /**
     * Returns true if the given string is null or blank.
     *
     * @param s the string to check
     * @return true if null or empty after trimming
     */


    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Builder for constructing an {@link EmailJsConfig} instance.
     * <p>
     * Required fields: serviceId, publicKey, inquiryTemplateId.
     * For IMAP fetching: appEmail, appPassword, imapHost.
     * For replies: replyTemplateId.
     * <p>
     * All other fields have sensible defaults and are optional.
     */

    public static class Builder {
        private String serviceId;
        private String publicKey;
        private String inquiryTemplateId;
        private String replyTemplateId;
        private String emailJsApiUrl = "https://api.emailjs.com/api/v1.0/email/send";
        private String appEmail;
        private String appPassword;
        private String imapHost = "imap.gmail.com";
        private int imapPort = 993;
        private boolean firebaseEnabled = true;
        private String firebaseInquiryRoot = "Emails";
        private String firebaseUserRoot = "Users";
        private boolean notificationsEnabled = true;
        private String notificationTitle = "New Reply";
        private String notificationBody = "You have received a new reply.";
        private Map<String, String> defaultInquiryParams;
        private Map<String, String> defaultReplyParams;

        public Builder setServiceId(String v) {
            serviceId = v;
            return this;
        }

        public Builder setPublicKey(String v) {
            publicKey = v;
            return this;
        }

        public Builder setInquiryTemplateId(String v) {
            inquiryTemplateId = v;
            return this;
        }

        public Builder setReplyTemplateId(String v) {
            replyTemplateId = v;
            return this;
        }

        public Builder setEmailJsApiUrl(String v) {
            emailJsApiUrl = v;
            return this;
        }

        public Builder setAppEmail(String v) {
            appEmail = v;
            return this;
        }

        public Builder setAppPassword(String v) {
            appPassword = v;
            return this;
        }

        public Builder setImapHost(String v) {
            imapHost = v;
            return this;
        }

        public Builder setImapPort(int v) {
            imapPort = v;
            return this;
        }

        public Builder setFirebaseEnabled(boolean v) {
            firebaseEnabled = v;
            return this;
        }

        public Builder setFirebaseInquiryRoot(String v) {
            firebaseInquiryRoot = v;
            return this;
        }

        public Builder setFirebaseUserRoot(String v) {
            firebaseUserRoot = v;
            return this;
        }

        public Builder setNotificationsEnabled(boolean v) {
            notificationsEnabled = v;
            return this;
        }

        public Builder setNotificationTitle(String v) {
            notificationTitle = v;
            return this;
        }

        public Builder setNotificationBody(String v) {
            notificationBody = v;
            return this;
        }

        public Builder setDefaultInquiryParams(Map<String, String> v) {
            defaultInquiryParams = v;
            return this;
        }


        /**
         * @param v extra key-value pairs merged into every reply email request
         */

        public Builder setDefaultReplyParams(Map<String, String> v) {
            defaultReplyParams = v;
            return this;
        }

        /**
         * Builds and returns the {@link EmailJsConfig} instance.
         * <p>
         * Note: this does not validate required fields — validation happens
         * at send/fetch time via validateForSend, validateForReply, validateForFetch.
         *
         * @return a fully constructed EmailJsConfig
         */

        public EmailJsConfig build() {
            return new EmailJsConfig(this);
        }
    }

}
