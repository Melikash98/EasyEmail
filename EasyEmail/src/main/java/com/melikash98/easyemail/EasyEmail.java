package com.melikash98.easyemail;

import android.content.Context;

import androidx.annotation.Nullable;

import com.melikash98.easyemail.Config.EmailJsConfig;

import java.util.Map;

/**
 * EasyEmail — Main entry point of the EasyEmail Android library.
 * <p>
 * This class provides a simple API for:
 * - Sending inquiry emails to item owners via EmailJS
 * - Sending reply emails from owners to users via EmailJS
 * - Fetching owner replies from an IMAP mailbox
 * <p>
 * All operations require a valid {@link EmailJsConfig} instance.
 * Config validation is performed automatically before each network call.
 * <p>
 * Usage:
 * EasyEmail easyEmail = new EasyEmail(config);
 * easyEmail.sendInquiry(..., callback);
 * easyEmail.sendReply(..., callback);
 * easyEmail.fetchOwnerReplies(..., callback);
 */

public class EasyEmail {

    // Holds all settings needed for EmailJS requests and IMAP fetching
    private final EmailJsConfig config;
    private final EmailQueueManager queueManager;

    /**
     * Creates an EasyEmail instance with the given configuration.
     *
     * @param config EmailJS and IMAP settings — must not be null
     * @throws IllegalArgumentException if config is null
     */
    public EasyEmail(Context context, EmailJsConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
        this.queueManager = new EmailQueueManager(context, config);
    }

    /**
     * Sends an inquiry email to an item owner.
     * Calls the full overload with extraParams set to null.
     *
     * @param ownerEmail    recipient owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL (used in email template)
     * @param userName      inquiring user's display name
     * @param userEmail     inquiring user's email (used as reply-to)
     * @param userPhone     inquiring user's phone number
     * @param term          rental or purchase term (e.g. "6 months")
     * @param time          desired start time or availability
     * @param message       the user's inquiry message body
     * @param itemId        ID of the item being inquired about
     * @param categoriesId  category ID of the item
     * @param userUid       unique ID of the inquiring user (e.g. Firebase UID)
     * @param callback      receives success or failure after the request
     */

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
        queueManager.enqueueInquiry(
                ownerEmail, ownerName, ownerPhotoUrl, userName,
                userEmail, userPhone, term, time, message, itemId, categoriesId,
                userUid, null, callback
        );
    }

    /**
     * Sends an inquiry email to an item owner, with optional extra template parameters.
     * <p>
     * Validates the config before sending. Delegates delivery to {@link EmailSender}.
     * Use extraParams to pass any additional key-value pairs your EmailJS template needs.
     *
     * @param ownerEmail    recipient owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL (used in email template)
     * @param userName      inquiring user's display name
     * @param userEmail     inquiring user's email (used as reply-to)
     * @param userPhone     inquiring user's phone number
     * @param term          rental or purchase term (e.g. "6 months")
     * @param time          desired start time or availability
     * @param message       the user's inquiry message body
     * @param itemId        ID of the item being inquired about
     * @param categoriesId  category ID of the item
     * @param userUid       unique ID of the inquiring user (e.g. Firebase UID)
     * @param extraParams   optional extra key-value pairs for the email template; may be null
     * @param callback      receives success or failure after the request
     */

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
        queueManager.enqueueInquiry(
                ownerEmail, ownerName, ownerPhotoUrl,
                userName, userEmail, userPhone,
                term, time, message, itemId, categoriesId,
                userUid, extraParams, callback
        );
    }

    /**
     * Sends a reply email from an item owner to the inquiring user.
     * Calls the full overload with extraParams set to null.
     *
     * @param ownerEmail   replying owner's email address
     * @param ownerName    owner's display name
     * @param replyMessage the owner's reply message body
     * @param inquiryId    ID of the original inquiry being replied to
     * @param itemId       ID of the item associated with the inquiry
     * @param userName     original inquirer's display name
     * @param userEmail    original inquirer's email (reply recipient)
     * @param userPhotoUrl inquirer's profile photo URL (used in email template)
     * @param userUid      unique ID of the original inquirer
     * @param callback     receives success or failure after the request
     */

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

    /**
     * Sends a reply email from an item owner to the inquiring user,
     * with optional extra template parameters.
     * <p>
     * Validates the config before sending. Delegates delivery to {@link ReplyEmailSender}.
     *
     * @param ownerEmail   replying owner's email address
     * @param ownerName    owner's display name
     * @param replyMessage the owner's reply message body
     * @param inquiryId    ID of the original inquiry being replied to
     * @param itemId       ID of the item associated with the inquiry
     * @param userName     original inquirer's display name
     * @param userEmail    original inquirer's email (reply recipient)
     * @param userPhotoUrl inquirer's profile photo URL (used in email template)
     * @param userUid      unique ID of the original inquirer
     * @param extraParams  optional extra key-value pairs for the email template; may be null
     * @param callback     receives success or failure after the request
     */

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
        queueManager.enqueueReply(
                ownerEmail, ownerName, replyMessage,
                inquiryId, itemId, userName, userEmail,
                userPhotoUrl, userUid, extraParams, callback
        );
    }

    /**
     * Fetches all owner replies for a given inquiry from the configured IMAP mailbox.
     * <p>
     * Validates the config before connecting. Delegates fetching to {@link ImapFetcher}.
     * This is a network operation — avoid calling it on the main thread.
     *
     * @param userUid       UID of the user who sent the original inquiry
     * @param inquiryId     ID of the inquiry whose replies should be fetched
     * @param ownerPhotoUrl owner's profile photo URL (attached to fetched reply objects)
     * @param callback      receives the fetched replies or an error
     */

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
