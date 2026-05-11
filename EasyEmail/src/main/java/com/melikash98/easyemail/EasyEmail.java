package com.melikash98.easyemail;

public class EasyEmail {
    private final EmailJsConfig config;

    public EasyEmail(EmailJsConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
    }
    public void sendInquiry(
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
        config.validateForSend();
        EmailSender.send(
                config, ownerEmail, ownerName, ownerPhotoUrl,
                userName, userEmail, userPhone,
                term, time, message, itemId, categoriesId, userUid,
                callback
        );
    }
    public void sendReply(
            String ownerEmail,
            String ownerName,
            String replyMessage,
            String inquiryId,
            String itemId,
            String userName,
            String userEmail,
            String userPhotoUrl,
            String userUid,
            EmailCallback callback
    ) {
        config.validateForReply();
        ReplyEmailSender.send(
                config, ownerEmail, ownerName, replyMessage,
                inquiryId, itemId, userName, userEmail,
                userPhotoUrl, userUid, callback
        );
    }
    public void fetchOwnerReplies(
            String userUid,
            String inquiryId,
            String ownerPhotoUrl,
            EmailCallback callback
    ) {
        config.validateForFetch();
        ImapFetcher.fetchRepliesForInquiry(config, userUid, inquiryId, ownerPhotoUrl, callback);
    }
}
