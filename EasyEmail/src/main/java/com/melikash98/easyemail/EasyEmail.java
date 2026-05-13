package com.melikash98.easyemail;

import androidx.annotation.Nullable;

import java.util.Map;

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
        sendInquiry(
                ownerEmail, ownerName, ownerPhotoUrl,
                userName, userEmail, userPhone,
                term, time, message, itemId, categoriesId, userUid,
                null,
                callback
        );
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
            @Nullable Map<String, String> extraParams,
            EmailCallback callback
    ) {
        config.validateForSend();
        EmailSender.send(
                config, ownerEmail, ownerName, ownerPhotoUrl,
                userName, userEmail, userPhone,
                term, time, message, itemId, categoriesId, userUid,
                extraParams,
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
        sendReply(
                ownerEmail, ownerName, replyMessage,
                inquiryId, itemId, userName, userEmail,
                userPhotoUrl, userUid,
                null,
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
            @Nullable Map<String, String> extraParams,
            EmailCallback callback
    ) {
        config.validateForReply();
        ReplyEmailSender.send(
                config, ownerEmail, ownerName, replyMessage,
                inquiryId, itemId, userName, userEmail,
                userPhotoUrl, userUid,
                extraParams,
                callback
        );
    }
    public void fetchOwnerReplies(
            String userUid,
            String inquiryId,
            String ownerPhotoUrl,
            EmailCallback callback
    ) {
        config.validateForFetch();
        ImapFetcher.fetchRepliesForInquiry(
                config, userUid, inquiryId, ownerPhotoUrl, callback);
    }
}
