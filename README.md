<img src="https://raw.githubusercontent.com/Melikash98/EasyEmail/main/easy_logo.png" alt="Logo" width="500px"   height="250px" style="margin-right: 10px;padding-top: 6rem;" />

# EasyEmail

[![JitPack](https://jitpack.io/v/Melikash98/EasyEmail.svg)](https://jitpack.io/#Melikash98/EasyEmail)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Melikash98/EasyEmail/blob/main/LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![Platform](https://img.shields.io/badge/platform-Android-orange.svg)](https://developer.android.com)

**EasyEmail — A lightweight Android library for sending inquiries and replies via EmailJS, with optional Firebase Realtime Database integration and IMAP inbox fetching.**

EasyEmail handles the full email workflow in Android apps: sending contact/inquiry emails, owner replies, fetching replies from an IMAP inbox, and storing everything in Firebase — all with a single fluent API.

>  **EmailJS Only** — This library exclusively supports [EmailJS](https://www.emailjs.com/) as the email delivery provider. SMTP or other providers are not supported.

---

##  Features

- **Send inquiry emails** via EmailJS with one method call
- **Send reply emails** back to users via EmailJS
- **Fetch owner replies** from any IMAP inbox (e.g. Gmail)
- **Firebase Realtime Database** integration — auto-save inquiries, replies, and notifications
- **Unread reply count** management in Firebase
- **Customizable templates** via `defaultInquiryParams` / `defaultReplyParams`
- **Extra params** support for flexible EmailJS template variables
- **Callbacks on the main thread** — safe to update UI directly in `onSuccess` / `onError`
- **Builder pattern** config — clean, readable setup with sensible defaults
- Works with both **Kotlin and Java** projects
- **JitPack ready** for instant integration

---

##  Demo

<img src="https://raw.githubusercontent.com/Melikash98/EasyEmail/main/easy_email_demo.gif" alt="easy_email_demo.gif" width="25%"   height="25%" style="margin-right: 10px;padding-top: 6rem;" />

---

##  Installation

### 1. Add JitPack repository

In your **root** `settings.gradle` (or `settings.gradle.kts`):

```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
### Step 2: Add dependency

```gradle
dependencies {
	        implementation 'com.github.Melikash98:EasyEmail:v1.0.2'
	}
```
### Step 3: Add required permissions
In your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```
### Step 4: Required Dependencies
The library requires the following in your app's `build.gradle`. If you're using JitPack, these are pulled in automatically unless excluded:
```groovy
dependencies {
    // HTTP client for EmailJS
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'

    // IMAP fetching
    implementation 'com.sun.mail:android-mail:1.6.7'
    implementation 'com.sun.mail:android-activation:1.6.7'

    // Offline queue (WorkManager + Room)
    implementation 'androidx.work:work-runtime:2.9.0'
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'

    // Firebase (only if firebaseEnabled = true)
    implementation 'com.google.firebase:firebase-database:21.0.0'
}
```
---
##  EmailJS Setup
EasyEmail **only works with EmailJS**. You must create a free account and configure your templates before using this library.

### 1. Create an EmailJS account

Go to [https://www.emailjs.com](https://www.emailjs.com) and sign up for a free account.

### 2. Connect an email service

In your EmailJS dashboard, go to **Email Services** → **Add New Service** and connect your Gmail, Outlook, or any SMTP provider. Copy the **Service ID** (e.g. `service_xxxxxxx`).

### 3. Create an Inquiry Template

Go to **Email Templates** → **Create New Template**.

This template is used when a user sends an inquiry. The following variables are automatically filled by the library — add any of them to your template:
| Variable            | Description                          |
|---------------------|--------------------------------------|
| `{{name}}`          | User's display name                  |
| `{{user_name}}`     | User's display name (alias)          |
| `{{user_email}}`    | User's email address                 |
| `{{user_phone}}`    | User's phone number                  |
| `{{message}}`       | Inquiry message body                 |
| `{{term}}`          | Term/condition field                 |
| `{{time}}`          | Formatted send time (dd/MM/yyyy HH:mm) |
| `{{owner_email}}`   | Receiver's (owner's) email           |
| `{{owner_name}}`    | Receiver's (owner's) name            |
| `{{owner_photo_url}}`| Receiver's photo URL                |
| `{{app_email}}`     | Your app's email (from config)       |
| `{{item_id}}`       | Item/listing ID                      |
| `{{categories_id}}` | Category ID                          |
| `{{inquiry_id}}`    | Auto-generated unique inquiry UUID   |

Copy the **Template ID** (e.g. `template_xxxxxxx`).
### 4. Create a Reply Template

Create a second template for owner replies. Variables available:

| Variable             | Description                        |
|----------------------|------------------------------------|
| `{{owner_email}}`    | Owner's email address              |
| `{{owner_name}}`     | Owner's display name               |
| `{{reply_message}}`  | The reply message body             |
| `{{user_name}}`      | User being replied to              |
| `{{name}}`           | User being replied to (alias)      |
| `{{user_photo_url}}` | User's photo URL                   |
| `{{item_id}}`        | Item/listing ID                    |
| `{{inquiry_id}}`     | The original inquiry UUID          |
| `{{time}}`           | Formatted reply time               |
| `{{app_email}}`      | Your app's email (from config)     |

> **Important:** For IMAP reply fetching to work, your reply template **must include `{{inquiry_id}}`** somewhere in the email **Subject** line, so the library can match incoming emails to the correct inquiry.

Copy this template's **Template ID** as well.

### 5. Get your Public Key

In your EmailJS dashboard, go to **Account** → **General** → copy your **Public Key**.

### 6. Pass the values to the library

```java
EmailJsConfig config = new EmailJsConfig.Builder()
    .setServiceId("service_xxxxxxx")
    .setPublicKey("your_public_key")
    .setInquiryTemplateId("template_xxxxxxx")
    .setReplyTemplateId("template_yyyyyyy")
    // optional IMAP settings (only needed for fetchOwnerReplies)
    .setAppEmail("your.app@gmail.com")
    .setAppPassword("your_app_password")
    .build();
```
---

##  Configuration — `EmailJsConfig`

All configuration is done via the `Builder`. Most fields have sensible defaults.

```java
EmailJsConfig config = new EmailJsConfig.Builder()

    // ── Required for sending ──────────────────────────────────────
    .setServiceId("service_xxxxxxx")          // EmailJS Service ID
    .setPublicKey("your_public_key")          // EmailJS Public Key
    .setInquiryTemplateId("template_xxxxxxx") // Template ID for inquiries

    // ── Required for replies ──────────────────────────────────────
    .setReplyTemplateId("template_yyyyyyy")   // Template ID for replies

    // ── Required for IMAP fetching ────────────────────────────────
    .setAppEmail("your.app@gmail.com")        // IMAP login email
    .setAppPassword("xxxx xxxx xxxx xxxx")    // App password (NOT your real password)
    .setImapHost("imap.gmail.com")            // Default: "imap.gmail.com"
    .setImapPort(993)                         // Default: 993

    // ── Firebase (optional) ───────────────────────────────────────
    .setFirebaseEnabled(true)                 // Default: true
    .setFirebaseInquiryRoot("Emails")         // Root node for inquiries. Default: "Emails"
    .setFirebaseUserRoot("Users")             // Root node for users. Default: "Users"

    // ── Notifications (optional) ──────────────────────────────────
    .setNotificationsEnabled(true)            // Default: true
    .setNotificationTitle("New Reply")        // Default: "New Reply"
    .setNotificationBody("You have received a new reply.") // Default value

    // ── Custom default template params (optional) ─────────────────
    .setDefaultInquiryParams(myInquiryMap)    // Extra fixed params for inquiry template
    .setDefaultReplyParams(myReplyMap)        // Extra fixed params for reply template

    // ── Advanced (optional) ───────────────────────────────────────
    .setEmailJsApiUrl("https://api.emailjs.com/api/v1.0/email/send") // Default value

    .build();
```

> **Gmail IMAP note:** Google no longer allows sign-in with your normal password from third-party apps. You must generate an **App Password** from your Google Account → Security → 2-Step Verification → App Passwords.

---

##  Usage

### Initialize

```java
EasyEmail easyEmail = new EasyEmail(context, config);
```
---

### 1. Send an Inquiry

Use this when a user wants to contact an owner/seller/host.

```java
easyEmail.sendInquiry(
    "owner@example.com",   // ownerEmail  — recipient
    "John Owner",          // ownerName
    "https://...",         // ownerPhotoUrl (can be null or empty)
    "Alice User",          // userName     — sender
    "alice@example.com",   // userEmail
    "+1234567890",         // userPhone
    "Flexible",            // term
    "",                    // time (leave empty to auto-fill current time)
    "Hello, is this still available?", // message
    "item_001",            // itemId
    "cat_furniture",       // categoriesId
    "uid_abc123",          // userUid (Firebase UID, nullable)
    new EmailCallback() {
        @Override
        public void onSuccess() {
            // called on main thread — safe to update UI
            Toast.makeText(context, "Inquiry sent!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String error) {
            // called on main thread
            Log.e("EasyEmail", "Error: " + error);
        }
    }
);
```

#### With extra template params

```java
Map<String, String> extras = new HashMap<>();
extras.put("property_type", "Apartment");
extras.put("floor_number",  "3");

easyEmail.sendInquiry(
    ownerEmail, ownerName, ownerPhotoUrl,
    userName, userEmail, userPhone,
    term, time, message, itemId, categoriesId, userUid,
    extras,       // <-- extra params merged into the EmailJS template
    callback
);
```

---

### 2. Send a Reply

Use this when an owner wants to reply to an inquiry. The reply is sent via EmailJS **and** optionally saved to Firebase.

```java
easyEmail.sendReply(
    "owner@example.com",    // ownerEmail  — sender of the reply
    "John Owner",           // ownerName
    "Yes, it is available. Please contact me.", // replyMessage
    "inquiry-uuid-here",    // inquiryId   — must match the original inquiry
    "item_001",             // itemId
    "Alice User",           // userName    — original inquiry sender
    "alice@example.com",    // userEmail
    "https://...",          // userPhotoUrl
    "uid_abc123",           // userUid (Firebase UID, nullable)
    new EmailCallback() {
        @Override
        public void onSuccess() {
            Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String error) {
            Log.e("EasyEmail", "Reply error: " + error);
        }
    }
);
```

---

### 3. Fetch Owner Replies (IMAP)

Use this to pull replies that an owner sent from their email client (outside the app) into Firebase, so the user can see them inside the app.

```java
easyEmail.fetchOwnerReplies(
    "uid_abc123",          // userUid
    "inquiry-uuid-here",   // inquiryId (must appear in the email subject)
    "https://...",         // ownerPhotoUrl
    new EmailCallback() {
        @Override
        public void onSuccess() {
            //  Note: this callback is on a background thread.
            // Post to the main thread before updating any UI.
            runOnUiThread(() ->
                Toast.makeText(context, "Replies fetched!", Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public void onError(String error) {
            runOnUiThread(() -> Log.e("EasyEmail", "IMAP error: " + error));
        }
    }
);
```

>  **Thread notice:** Unlike `sendInquiry` and `sendReply`, the `fetchOwnerReplies` callback is delivered on a **background thread**. Wrap any UI updates inside `runOnUiThread {}` or `Handler(Looper.getMainLooper()).post {}`.

---

### 4. Observing Email State (LiveData)

You can observe the send state anywhere in your app:

```java
EmailStateLiveData.getInstance().getLiveData().observe(this, state -> {
    switch (state.getStatus()) {
        case LOADING:
            // Show progress indicator
            break;
        case QUEUED:
            // Email is queued (offline)
            Toast.makeText(this, state.getMessage(), Toast.LENGTH_SHORT).show();
            break;
        case SUCCESS:
            // Email sent successfully
            break;
        case FAILED:
            // Send failed after retries
            Log.e("EasyEmail", state.getMessage());
            break;
    }
});
```
---

## 🔥 Firebase Data Structure
When `firebaseEnabled = true`, the library writes to Firebase Realtime Database using the following structure:

```
{firebaseUserRoot}/                          ← default: "Users"
  └── {userUid}/
        ├── inquiries/
        │     └── {inquiryId}/
        │           ├── userUid
        │           ├── inquiryId
        │           ├── itemId
        │           ├── categoriesId
        │           ├── ownerEmail
        │           ├── ownerName
        │           ├── ownerPhoto
        │           ├── userName
        │           ├── sentAt          ← ServerValue.TIMESTAMP
        │           ├── status          ← "sent"
        │           └── replies/
        │                 └── {pushId}/
        │                       ├── replyId
        │                       ├── replyText
        │                       ├── isOwnerReply   ← true / false
        │                       ├── replyDate
        │                       ├── receivedAt
        │                       └── isRead
        ├── MessageNotification/
        │     └── {pushId}/
        │           ├── message
        │           ├── type            ← "NEW_REPLY"
        │           ├── inquiryId
        │           ├── replyId
        │           ├── isRead
        │           └── timestamp
        └── unreadReplyCount            ← integer

{firebaseInquiryRoot}/                       ← default: "Emails"
  └── {inquiryId}/
        └── (same fields as user inquiry node)
```

###  Disable Firebase

```java
new EmailJsConfig.Builder()
        ...
        .setFirebaseEnabled(false)
        .build();
```
---
###  Custom Firebase Roots

```java
new EmailJsConfig.Builder()
        ...
        .setFirebaseInquiryRoot("Inquiries")   // default: "Emails"
        .setFirebaseUserRoot("AppUsers")        // default: "Users"
        .build();
```
---

##  Known Limitations

- **EmailJS only** — no SMTP, SendGrid, or other provider support.
- **IMAP deduplication** — `fetchOwnerReplies` scans the last 50 inbox messages every time it is called. Calling it multiple times for the same inquiry **will save duplicate reply entries** in Firebase. Implement your own call guard (e.g. check `status` in Firebase before fetching) if needed.
- **OkHttpClient instances** — a new `OkHttpClient` is created per send call. For high-frequency use, consider wrapping `EasyEmail` as a singleton in your app.
- **App password security** — your IMAP app password is held in memory inside `EmailJsConfig`. Do not log the config object and do not store the password in plain-text source files. Use `BuildConfig` fields or an encrypted store instead.

---

## 📋 API Reference

### `EasyEmail`

| Method | Description |
|--------|-------------|
| `sendInquiry(...)` | Send an inquiry email via EmailJS |
| `sendInquiry(..., extraParams, callback)` | Same, with extra template variables |
| `sendReply(...)` | Send a reply email via EmailJS |
| `sendReply(..., extraParams, callback)` | Same, with extra template variables |
| `fetchOwnerReplies(userUid, inquiryId, ownerPhotoUrl, callback)` | Fetch IMAP replies and save to Firebase |

### `EmailCallback`

```java
public interface EmailCallback {
    void onSuccess();
    void onError(String error);
}
```

### `EmailJsConfig.Builder` — all fields

| Method | Default | Required |
|--------|---------|----------|
| `setServiceId(String)` | — | ✅ for send |
| `setPublicKey(String)` | — | ✅ for send |
| `setInquiryTemplateId(String)` | — | ✅ for send |
| `setReplyTemplateId(String)` | — | ✅ for reply |
| `setAppEmail(String)` | — | ✅ for fetch |
| `setAppPassword(String)` | — | ✅ for fetch |
| `setImapHost(String)` | `imap.gmail.com` | |
| `setImapPort(int)` | `993` | |
| `setEmailJsApiUrl(String)` | EmailJS v1.0 URL | |
| `setFirebaseEnabled(boolean)` | `true` | |
| `setFirebaseInquiryRoot(String)` | `"Emails"` | |
| `setFirebaseUserRoot(String)` | `"Users"` | |
| `setNotificationsEnabled(boolean)` | `true` | |
| `setNotificationTitle(String)` | `"New Reply"` | |
| `setNotificationBody(String)` | `"You have received a new reply."` | |
| `setDefaultInquiryParams(Map)` | empty | |
| `setDefaultReplyParams(Map)` | empty | |

---

##  License
This project is licensed under the MIT License.

---
## Keywords

android email library, emailjs android, android inquiry email,android imap library, firebase email android, android contact form library,send email android java

---
## 👨‍💻 Author

**Melikash98**

If you find EasyEmail useful, please consider giving it a ⭐ star on GitHub —
it helps the project grow and motivates further development.

For feature requests, bug reports, or suggestions, please open an issue.
Your feedback is highly appreciated.


