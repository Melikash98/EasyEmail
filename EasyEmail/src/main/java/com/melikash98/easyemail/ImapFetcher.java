package com.melikash98.easyemail;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

/**
 * Fetches owner reply emails from an IMAP mailbox for a given inquiry.
 * <p>
 * This class is used internally by {@link EasyEmail#fetchOwnerReplies}.
 * It connects to the configured IMAP server on a background thread,
 * searches the inbox for messages whose subject contains the inquiryId,
 * and saves each matched reply to Firebase via {@link FirebaseEmailUtil#saveOwnerReply}.
 * <p>
 * The result is delivered back on the main thread via {@link EmailCallback}.
 * <p>
 * This class is not meant to be instantiated — all methods are package-private static.
 */

public class ImapFetcher {
    private static final String TAG = "EasyEmail_IMAP";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Connects to the IMAP inbox and fetches all replies for the given inquiry.
     * <p>
     * Steps performed:
     * 1. Configures an IMAP SSL session using settings from {@link EmailJsConfig}
     * 2. Connects to the mail server with the app email and password
     * 3. Opens the INBOX in read-only mode
     * 4. Fetches the last 50 messages to limit bandwidth usage
     * 5. Filters messages whose subject contains the inquiryId
     * 6. Extracts the sender, body text, and sent date from each matched message
     * 7. Saves each reply to Firebase via {@link FirebaseEmailUtil#saveOwnerReply}
     * 8. Delivers onSuccess or onError back on the main thread
     * 9. Closes the folder and store in the finally block regardless of outcome
     *
     * @param config        the library configuration (provides IMAP host, port, credentials)
     * @param userUid       Firebase UID of the user who originally sent the inquiry
     * @param inquiryId     ID of the inquiry to search for in email subjects
     * @param ownerPhotoUrl owner's profile photo URL attached to each saved reply
     * @param callback      receives onSuccess or onError on the main thread
     */

    public static void fetchRepliesForInquiry(
            EmailJsConfig config,
            String userUid,
            String inquiryId,
            String ownerPhotoUrl,
            EmailCallback callback
    ) {
        executor.execute(() -> {
            Store store = null;
            Folder inbox = null;
            int savedCount = 0;

            try {
                Properties props = new Properties();
                props.put("mail.store.protocol", "imaps");
                props.put("mail.imaps.host", config.getImapHost());
                props.put("mail.imaps.port", String.valueOf(config.getImapPort()));
                props.put("mail.imaps.ssl.enable", "true");
                props.put("mail.imaps.timeout", "10000");

                Session session = Session.getInstance(props);
                store = session.getStore("imaps");
                store.connect(config.getImapHost(), config.getAppEmail(), config.getAppPassword());

                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                int total = inbox.getMessageCount();
                int start = Math.max(1, total - 49);
                Message[] messages = inbox.getMessages(start, total);

                for (Message msg : messages) {
                    String subject = msg.getSubject();
                    if (subject != null && subject.contains(inquiryId)) {
                        String from = msg.getFrom() != null && msg.getFrom().length > 0
                                ? msg.getFrom()[0].toString()
                                : "";
                        String body = extractTextBody(msg);
                        long sentDate = msg.getSentDate() != null
                                ? msg.getSentDate().getTime()
                                : System.currentTimeMillis();

                        Log.d(TAG, "Found reply for " + inquiryId + " from " + from);

                        FirebaseEmailUtil.saveOwnerReply(
                                config, userUid, inquiryId,
                                body, from, ownerPhotoUrl, sentDate
                        );
                        savedCount++;
                    }
                }

                Log.d(TAG, "Fetch complete. Saved " + savedCount + " replies.");
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess();
                });
            } catch (Exception e) {
                Log.e(TAG, "IMAP fetch failed", e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError("IMAP error: " + e.getMessage());
                });
            } finally {
                try {
                    if (inbox != null && inbox.isOpen()) inbox.close(false);
                } catch (Exception ignored) {
                }
                try {
                    if (store != null && store.isConnected()) store.close();
                } catch (Exception ignored) {
                }
            }
        });
    }


    /**
     * Extracts the plain text body from an email message.
     * <p>
     * Handles two content types:
     * - Plain String: returned directly
     * - MimeMultipart: delegated to {@link #extractFromMultipart}
     * <p>
     * Returns an empty string if the body cannot be read.
     *
     * @param msg the email message to extract text from
     * @return the plain text body, or "" if extraction fails
     */

    private static String extractTextBody(Message msg) {
        try {
            Object content = msg.getContent();
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof MimeMultipart) {
                return extractFromMultipart((MimeMultipart) content);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not extract body", e);
        }
        return "";
    }

    /**
     * Recursively extracts plain text from a MimeMultipart email body.
     * <p>
     * Iterates over all body parts and collects text/plain content.
     * Nested multipart structures are handled recursively.
     * HTML-only parts are intentionally skipped to avoid returning markup.
     *
     * @param mp the MimeMultipart to extract text from
     * @return the concatenated plain text content, trimmed; or "" if nothing is found
     */

    private static String extractFromMultipart(MimeMultipart mp) {
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < mp.getCount(); i++) {
                javax.mail.BodyPart part = mp.getBodyPart(i);
                String ct = part.getContentType().toLowerCase();
                if (ct.startsWith("text/plain")) {
                    sb.append(part.getContent().toString());
                } else if (ct.startsWith("multipart/")) {
                    sb.append(extractFromMultipart((MimeMultipart) part.getContent()));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Multipart extraction error", e);
        }
        return sb.toString().trim();
    }
}
