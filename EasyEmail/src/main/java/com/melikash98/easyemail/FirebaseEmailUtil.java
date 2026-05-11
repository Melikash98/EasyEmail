package com.melikash98.easyemail;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class FirebaseEmailUtil {
    private static final String TAG = "EasyEmail_Firebase";

    // ─── Save User Inquiry ─────────────────────────────────────────────────────
    public static void saveUserInquiry(
            EmailJsConfig config, String uid, String inquiryId, String itemId,
            String ownerEmail, String ownerName, String ownerPhotoUrl,
            String userName, String categoriesId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("inquiries").child(inquiryId);

        ref.setValue(buildInquiryMap(uid, inquiryId, itemId, categoriesId,
                        ownerEmail, ownerName, ownerPhotoUrl, userName))
                .addOnSuccessListener(v -> Log.d(TAG, "User inquiry saved: " + inquiryId))
                .addOnFailureListener(e -> Log.e(TAG, "User inquiry save failed", e));
    }

    // ─── Save Global Inquiry ───────────────────────────────────────────────────
    public static void saveGlobalInquiry(
            EmailJsConfig config, String inquiryId, String itemId, String userUid,
            String ownerEmail, String ownerName, String ownerPhotoUrl,
            String userName, String categoriesId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseInquiryRoot())
                .child(inquiryId);

        ref.setValue(buildInquiryMap(userUid, inquiryId, itemId, categoriesId,
                        ownerEmail, ownerName, ownerPhotoUrl, userName))
                .addOnSuccessListener(v -> Log.d(TAG, "Global inquiry saved: " + inquiryId))
                .addOnFailureListener(e -> Log.e(TAG, "Global inquiry save failed", e));
    }

    private static Map<String, Object> buildInquiryMap(
            String uid, String inquiryId, String itemId, String categoriesId,
            String ownerEmail, String ownerName, String ownerPhotoUrl, String userName) {
        Map<String, Object> data = new HashMap<>();
        data.put("userUid",      uid);
        data.put("inquiryId",    inquiryId);
        data.put("itemId",       itemId);
        data.put("categoriesId", categoriesId);
        data.put("ownerEmail",   safe(ownerEmail));
        data.put("ownerName",    safe(ownerName));
        data.put("ownerPhoto",   safe(ownerPhotoUrl));
        data.put("userName",     safe(userName));
        data.put("sentAt",       ServerValue.TIMESTAMP);
        data.put("status",       "sent");
        return data;
    }

    // ─── Save User Reply ───────────────────────────────────────────────────────
    public static void saveUserReply(
            EmailJsConfig config, String userUid, String inquiryId,
            String replyText, String userName, String userEmail, String userPhotoUrl) {

        if (userUid == null || inquiryId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(userUid).child("inquiries").child(inquiryId)
                .child("replies").push();

        String replyId = ref.getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("replyId",      replyId);
        data.put("inquiryId",    inquiryId);
        data.put("replyText",    safe(replyText));
        data.put("fromUser",     safe(userName));      // نام فقط، بدون ایمیل
        data.put("userEmail",    safe(userEmail));     // ایمیل جداگانه
        data.put("userPhotoUrl", safe(userPhotoUrl));
        data.put("isOwnerReply", false);
        data.put("replyDate",    ServerValue.TIMESTAMP);
        data.put("receivedAt",   ServerValue.TIMESTAMP);
        data.put("isRead",       true);

        ref.setValue(data)
                .addOnSuccessListener(v -> Log.d(TAG, "User reply saved: " + replyId))
                .addOnFailureListener(e -> Log.e(TAG, "User reply save failed", e));
    }

    // ─── Save Owner Reply (از IMAP) ────────────────────────────────────────────
    public static void saveOwnerReply(
            EmailJsConfig config, String uid, String inquiryId,
            String replyText, String fromOwner, String ownerPhotoUrl, long sentDateMs) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("inquiries").child(inquiryId)
                .child("replies").push();

        String replyId = ref.getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("replyId",       replyId);
        data.put("inquiryId",     inquiryId);
        data.put("replyText",     safe(replyText));
        data.put("fromOwner",     safe(fromOwner));
        data.put("ownerPhotoUrl", safe(ownerPhotoUrl));
        data.put("ownerEmail",    safe(fromOwner));
        data.put("isOwnerReply",  true);
        data.put("replyDate",     sentDateMs);
        data.put("receivedAt",    ServerValue.TIMESTAMP);
        data.put("isRead",        false);

        ref.setValue(data)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Owner reply saved: " + replyId);
                    if (config.isNotificationsEnabled()) {
                        saveNewReplyNotification(config, uid, inquiryId, replyId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Owner reply save failed", e));
    }

    // ─── Notification ──────────────────────────────────────────────────────────
    public static void saveNewReplyNotification(
            EmailJsConfig config, String uid, String inquiryId, String replyId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("MessageNotification").push();

        Map<String, Object> data = new HashMap<>();
        data.put("message",   config.getNotificationBody());
        data.put("isRead",    false);
        data.put("timestamp", ServerValue.TIMESTAMP);
        data.put("type",      "NEW_REPLY");
        data.put("replyId",   replyId);
        data.put("inquiryId", inquiryId);

        ref.setValue(data)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Notification saved");
                    updateUnreadReplyCount(config, uid);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Notification save failed", e));
    }

    public static void updateUnreadReplyCount(EmailJsConfig config, String uid) {
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("MessageNotification");

        ref.get().addOnCompleteListener(task -> {
            long unread = 0;
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snap : task.getResult().getChildren()) {
                    String type  = snap.child("type").getValue(String.class);
                    Boolean read = snap.child("isRead").getValue(Boolean.class);
                    if ("NEW_REPLY".equals(type) && (read == null || !read)) unread++;
                }
            }
            long finalUnread = unread;
            FirebaseDatabase.getInstance()
                    .getReference(config.getFirebaseUserRoot())
                    .child(uid).child("unreadReplyCount")
                    .setValue(unread)
                    .addOnSuccessListener(v -> Log.d(TAG, "Unread count: " + finalUnread));
        });
    }

    // ─── Helper ────────────────────────────────────────────────────────────────
    private static String safe(String s) { return s != null ? s : ""; }

}
