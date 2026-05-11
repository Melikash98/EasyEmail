package com.melikash98.easyemail;

import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

public class ImapFetcher {
    private static final String TAG = "EasyEmail_IMAP";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * دریافت reply های owner برای یک inquiry خاص
     *
     * @param config        تنظیمات IMAP
     * @param userUid       Firebase UID کاربر
     * @param inquiryId     آی‌دی inquiry (در subject ایمیل باید وجود داشته باشه)
     * @param ownerPhotoUrl عکس owner برای ذخیره
     * @param callback      نتیجه
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
                // آخرین 50 ایمیل رو بررسی می‌کنیم
                int start = Math.max(1, total - 49);
                Message[] messages = inbox.getMessages(start, total);

                for (Message msg : messages) {
                    String subject = msg.getSubject();
                    // فقط reply هایی که مربوط به این inquiryId هستن
                    if (subject != null && subject.contains(inquiryId)) {
                        String from = msg.getFrom() != null && msg.getFrom().length > 0
                                ? msg.getFrom()[0].toString()
                                : "";
                        String body = extractTextBody(msg);
                        long sentDate = msg.getSentDate() != null
                                ? msg.getSentDate().getTime()
                                : System.currentTimeMillis();

                        Log.d(TAG, "Found reply for " + inquiryId + " from " + from);

                        // ذخیره در Firebase
                        FirebaseEmailUtil.saveOwnerReply(
                                config, userUid, inquiryId,
                                body, from, ownerPhotoUrl, sentDate
                        );
                        savedCount++;
                    }
                }

                Log.d(TAG, "Fetch complete. Saved " + savedCount + " replies.");
                if (callback != null) callback.onSuccess();

            } catch (Exception e) {
                Log.e(TAG, "IMAP fetch failed", e);
                if (callback != null) callback.onError("IMAP error: " + e.getMessage());
            } finally {
                try { if (inbox != null && inbox.isOpen()) inbox.close(false); } catch (Exception ignored) {}
                try { if (store != null && store.isConnected()) store.close(); } catch (Exception ignored) {}
            }
        });
    }

    // ─── Extract plain text from MIME ─────────────────────────────────────────
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
