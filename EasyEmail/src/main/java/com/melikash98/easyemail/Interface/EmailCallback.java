package com.melikash98.easyemail.Interface;


/**
 * Callback interface for email operations in EasyEmail.
 * <p>
 * Implement this interface to handle the result of sendInquiry,
 * sendReply, and fetchOwnerReplies calls.
 * <p>
 * Usage:
 * easyEmail.sendInquiry(..., new EmailCallback() {
 * public void onSuccess() { ... }
 * public void onError(String error) { ... }
 * });
 */

public interface EmailCallback {

    /**
     * Called when the email operation completed successfully.
     */

    void onSuccess();

    /**
     * Called when the email operation failed.
     *
     * @param error a message describing what went wrong
     */

    void onError(String error);
}
