package com.melikash98.easyemail;

public class EmailState {
    public enum Status {
        IDLE,
        LOADING,
        QUEUED,
        SUCCESS,
        FAILED
    }
    private final Status status;
    private final String message;

    private EmailState(Status status, String message) {
        this.status  = status;
        this.message = message;
    }

    public static EmailState idle() {
        return new EmailState(Status.IDLE, null);
    }

    public static EmailState loading() {
        return new EmailState(Status.LOADING, null);
    }

    public static EmailState queued(int queueCount) {
        return new EmailState(Status.QUEUED,
                queueCount + "Email waiting to be sent");
    }

    public static EmailState success() {
        return new EmailState(Status.SUCCESS, "Email sent successfully.");
    }

    public static EmailState failed(String error) {
        return new EmailState(Status.FAILED, error);
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }

    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isQueued()  { return status == Status.QUEUED;  }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isFailed()  { return status == Status.FAILED;  }
}
