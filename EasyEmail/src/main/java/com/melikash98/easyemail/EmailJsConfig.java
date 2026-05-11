package com.melikash98.easyemail;

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

    private EmailJsConfig(Builder builder) {
        this.serviceId            = builder.serviceId;
        this.publicKey            = builder.publicKey;
        this.inquiryTemplateId    = builder.inquiryTemplateId;
        this.replyTemplateId      = builder.replyTemplateId;
        this.emailJsApiUrl        = builder.emailJsApiUrl;
        this.appEmail             = builder.appEmail;
        this.appPassword          = builder.appPassword;
        this.imapHost             = builder.imapHost;
        this.imapPort             = builder.imapPort;
        this.firebaseEnabled      = builder.firebaseEnabled;
        this.firebaseInquiryRoot  = builder.firebaseInquiryRoot;
        this.firebaseUserRoot     = builder.firebaseUserRoot;
        this.notificationsEnabled = builder.notificationsEnabled;
        this.notificationTitle    = builder.notificationTitle;
        this.notificationBody     = builder.notificationBody;
    }
    public String getServiceId()           { return serviceId; }
    public String getPublicKey()           { return publicKey; }
    public String getInquiryTemplateId()   { return inquiryTemplateId; }
    public String getReplyTemplateId()     { return replyTemplateId; }
    public String getEmailJsApiUrl()       { return emailJsApiUrl; }
    public String getAppEmail()            { return appEmail; }
    public String getAppPassword()         { return appPassword; }
    public String getImapHost()            { return imapHost; }
    public int    getImapPort()            { return imapPort; }
    public boolean isFirebaseEnabled()     { return firebaseEnabled; }
    public String getFirebaseInquiryRoot() { return firebaseInquiryRoot; }
    public String getFirebaseUserRoot()    { return firebaseUserRoot; }
    public boolean isNotificationsEnabled(){ return notificationsEnabled; }
    public String getNotificationTitle()   { return notificationTitle; }
    public String getNotificationBody()    { return notificationBody; }

    public void validateForSend() {
        if (isEmpty(serviceId))          throw new IllegalStateException("serviceId is required.");
        if (isEmpty(publicKey))          throw new IllegalStateException("publicKey is required.");
        if (isEmpty(inquiryTemplateId))  throw new IllegalStateException("inquiryTemplateId is required.");
    }

    public void validateForReply() {
        validateForSend();
        if (isEmpty(replyTemplateId))    throw new IllegalStateException("replyTemplateId is required.");
    }

    public void validateForFetch() {
        if (isEmpty(appEmail))    throw new IllegalStateException("appEmail is required for IMAP.");
        if (isEmpty(appPassword)) throw new IllegalStateException("appPassword is required for IMAP.");
        if (isEmpty(imapHost))    throw new IllegalStateException("imapHost is required for IMAP.");
        if (imapPort <= 0)        throw new IllegalStateException("imapPort must be a valid port.");
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static class Builder {
        private String serviceId;
        private String publicKey;
        private String inquiryTemplateId;
        private String replyTemplateId;
        private String emailJsApiUrl      = "https://api.emailjs.com/api/v1.0/email/send";
        private String appEmail;
        private String appPassword;
        private String imapHost           = "imap.gmail.com";
        private int    imapPort           = 993;
        private boolean firebaseEnabled   = true;
        private String firebaseInquiryRoot = "Emails";
        private String firebaseUserRoot    = "Users";
        private boolean notificationsEnabled = true;
        private String notificationTitle  = "New Reply";
        private String notificationBody   = "You have received a new reply.";

        public Builder setServiceId(String v)           { serviceId = v;            return this; }
        public Builder setPublicKey(String v)           { publicKey = v;            return this; }
        public Builder setInquiryTemplateId(String v)   { inquiryTemplateId = v;    return this; }
        public Builder setReplyTemplateId(String v)     { replyTemplateId = v;      return this; }
        public Builder setEmailJsApiUrl(String v)       { emailJsApiUrl = v;        return this; }
        public Builder setAppEmail(String v)            { appEmail = v;             return this; }
        public Builder setAppPassword(String v)         { appPassword = v;          return this; }
        public Builder setImapHost(String v)            { imapHost = v;             return this; }
        public Builder setImapPort(int v)               { imapPort = v;             return this; }
        public Builder setFirebaseEnabled(boolean v)    { firebaseEnabled = v;      return this; }
        public Builder setFirebaseInquiryRoot(String v) { firebaseInquiryRoot = v;  return this; }
        public Builder setFirebaseUserRoot(String v)    { firebaseUserRoot = v;     return this; }
        public Builder setNotificationsEnabled(boolean v){ notificationsEnabled = v;return this; }
        public Builder setNotificationTitle(String v)   { notificationTitle = v;    return this; }
        public Builder setNotificationBody(String v)    { notificationBody = v;     return this; }

        public EmailJsConfig build() { return new EmailJsConfig(this); }
    }

}
