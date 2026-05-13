package com.melikash98.easyemail;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.melikash98.easyemail.Config.EmailJsConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for saving email-related data to Firebase Realtime Database.
 * <p>
 * This class is used internally by {@link EmailSender} and {@link ReplyEmailSender}.
 * It handles persisting inquiries, replies, notifications, and unread counts
 * under the user's Firebase node.
 * <p>
 * Firebase structure:
 * {userRoot}/
 * {uid}/
 * inquiries/
 * {inquiryId}/
 * replies/
 * {replyId}
 * MessageNotification/
 * {notificationId}
 * unreadReplyCount
 * <p>
 * {inquiryRoot}/
 * {inquiryId}
 * <p>
 * All methods are package-private static — this class is not meant to be instantiated.
 */

public class FirebaseEmailUtil {
    private static final String TAG = "EasyEmail_Firebase";

    /**
     * Saves an inquiry under the user's own Firebase node.
     * <p>
     * Path: {userRoot}/{uid}/inquiries/{inquiryId}
     * <p>
     * Called by {@link EmailSender} after a successful inquiry is dispatched,
     * so the user can later view their own sent inquiries.
     *
     * @param config        the library configuration (provides Firebase root paths)
     * @param uid           Firebase UID of the inquiring user
     * @param inquiryId     unique ID of the inquiry
     * @param itemId        ID of the item being inquired about
     * @param ownerEmail    owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL
     * @param userName      inquiring user's display name
     * @param categoriesId  category ID of the item
     */

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

    /**
     * Saves an inquiry under the global inquiry root in Firebase.
     * <p>
     * Path: {inquiryRoot}/{inquiryId}
     * <p>
     * This global record allows the owner or admin to look up any inquiry
     * by its ID without needing to know the user's UID.
     *
     * @param config        the library configuration (provides Firebase root paths)
     * @param inquiryId     unique ID of the inquiry
     * @param itemId        ID of the item being inquired about
     * @param userUid       Firebase UID of the inquiring user
     * @param ownerEmail    owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL
     * @param userName      inquiring user's display name
     * @param categoriesId  category ID of the item
     */

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


    /**
     * Builds the shared data map used for both user-level and global inquiry records.
     * <p>
     * Sets status to "sent" and uses Firebase ServerValue.TIMESTAMP for sentAt,
     * so the timestamp is always server-authoritative regardless of device clock.
     *
     * @param uid           Firebase UID of the inquiring user
     * @param inquiryId     unique ID of the inquiry
     * @param itemId        ID of the item being inquired about
     * @param categoriesId  category ID of the item
     * @param ownerEmail    owner's email address
     * @param ownerName     owner's display name
     * @param ownerPhotoUrl owner's profile photo URL
     * @param userName      inquiring user's display name
     * @return a map ready to be written to Firebase
     */

    private static Map<String, Object> buildInquiryMap(
            String uid, String inquiryId, String itemId, String categoriesId,
            String ownerEmail, String ownerName, String ownerPhotoUrl, String userName) {
        Map<String, Object> data = new HashMap<>();
        data.put("userUid", uid);
        data.put("inquiryId", inquiryId);
        data.put("itemId", itemId);
        data.put("categoriesId", categoriesId);
        data.put("ownerEmail", safe(ownerEmail));
        data.put("ownerName", safe(ownerName));
        data.put("ownerPhoto", safe(ownerPhotoUrl));
        data.put("userName", safe(userName));
        data.put("sentAt", ServerValue.TIMESTAMP);
        data.put("status", "sent");
        return data;
    }

    /**
     * Saves a reply sent by the user (not the owner) under the inquiry's replies node.
     * <p>
     * Path: {userRoot}/{userUid}/inquiries/{inquiryId}/replies/{pushId}
     * <p>
     * Marked with isOwnerReply=false and isRead=true since the user wrote it themselves.
     * Silently returns if userUid or inquiryId is null.
     *
     * @param config       the library configuration
     * @param userUid      Firebase UID of the replying user
     * @param inquiryId    ID of the inquiry this reply belongs to
     * @param replyText    the reply message body
     * @param userName     the replying user's display name
     * @param userEmail    the replying user's email address
     * @param userPhotoUrl the replying user's profile photo URL
     */

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
        data.put("replyId", replyId);
        data.put("inquiryId", inquiryId);
        data.put("replyText", safe(replyText));
        data.put("fromUser", safe(userName));
        data.put("userEmail", safe(userEmail));
        data.put("userPhotoUrl", safe(userPhotoUrl));
        data.put("isOwnerReply", false);
        data.put("replyDate", ServerValue.TIMESTAMP);
        data.put("receivedAt", ServerValue.TIMESTAMP);
        data.put("isRead", true);

        ref.setValue(data)
                .addOnSuccessListener(v -> Log.d(TAG, "User reply saved: " + replyId))
                .addOnFailureListener(e -> Log.e(TAG, "User reply save failed", e));
    }


    /**
     * Saves a reply sent by the item owner under the user's inquiry replies node.
     * <p>
     * Path: {userRoot}/{uid}/inquiries/{inquiryId}/replies/{pushId}
     * <p>
     * Marked with isOwnerReply=true and isRead=false so the user is alerted.
     * After saving, triggers a push notification if notifications are enabled in config.
     *
     * @param config        the library configuration
     * @param uid           Firebase UID of the user who originally sent the inquiry
     * @param inquiryId     ID of the inquiry being replied to
     * @param replyText     the owner's reply message body
     * @param fromOwner     the owner's display name or email
     * @param ownerPhotoUrl the owner's profile photo URL
     * @param sentDateMs    the timestamp of when the reply was sent (in milliseconds)
     */

    public static void saveOwnerReply(
            EmailJsConfig config, String uid, String inquiryId,
            String replyText, String fromOwner, String ownerPhotoUrl, long sentDateMs) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("inquiries").child(inquiryId)
                .child("replies").push();

        String replyId = ref.getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("replyId", replyId);
        data.put("inquiryId", inquiryId);
        data.put("replyText", safe(replyText));
        data.put("fromOwner", safe(fromOwner));
        data.put("ownerPhotoUrl", safe(ownerPhotoUrl));
        data.put("ownerEmail", safe(fromOwner));
        data.put("isOwnerReply", true);
        data.put("replyDate", sentDateMs);
        data.put("receivedAt", ServerValue.TIMESTAMP);
        data.put("isRead", false);

        ref.setValue(data)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Owner reply saved: " + replyId);
                    if (config.isNotificationsEnabled()) {
                        saveNewReplyNotification(config, uid, inquiryId, replyId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Owner reply save failed", e));
    }

    /**
     * Saves a new reply notification under the user's MessageNotification node.
     * <p>
     * Path: {userRoot}/{uid}/MessageNotification/{pushId}
     * <p>
     * After saving, triggers {@link #updateUnreadReplyCount} to keep the
     * unread badge count in sync.
     *
     * @param config    the library configuration (provides notification title/body)
     * @param uid       Firebase UID of the user to notify
     * @param inquiryId ID of the inquiry the reply belongs to
     * @param replyId   ID of the new reply that triggered this notification
     */

    public static void saveNewReplyNotification(
            EmailJsConfig config, String uid, String inquiryId, String replyId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("MessageNotification").push();

        Map<String, Object> data = new HashMap<>();
        data.put("message", config.getNotificationBody());
        data.put("isRead", false);
        data.put("timestamp", ServerValue.TIMESTAMP);
        data.put("type", "NEW_REPLY");
        data.put("replyId", replyId);
        data.put("inquiryId", inquiryId);

        ref.setValue(data)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Notification saved");
                    updateUnreadReplyCount(config, uid);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Notification save failed", e));
    }

    /**
     * Counts all unread NEW_REPLY notifications and writes the total to Firebase.
     * <p>
     * Path written: {userRoot}/{uid}/unreadReplyCount
     * <p>
     * Reads all entries under MessageNotification, filters by type="NEW_REPLY"
     * and isRead=false, then saves the count. This value can be observed in the
     * app to display a badge on the messages screen.
     * <p>
     * Silently returns if uid is null.
     *
     * @param config the library configuration
     * @param uid    Firebase UID of the user whose unread count should be updated
     */

    public static void updateUnreadReplyCount(EmailJsConfig config, String uid) {
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(config.getFirebaseUserRoot())
                .child(uid).child("MessageNotification");

        ref.get().addOnCompleteListener(task -> {
            long unread = 0;
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snap : task.getResult().getChildren()) {
                    String type = snap.child("type").getValue(String.class);
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

    /**
     * Returns the given string as-is, or an empty string if it is null.
     * Prevents null values from being written into Firebase nodes.
     *
     * @param s the string to check
     * @return the original string, or "" if null
     */

    private static String safe(String s) {
        return s != null ? s : "";
    }

}
