package com.melikash98.easyemail;


/**
 * Represents the current state of the EasyEmail sending flow.
 * <p>
 * This class is used to expose a simple and immutable state model for
 * UI updates, callbacks, or observable state handling.
 * Each instance contains:
 * <ul>
 *     <li>A status value describing the current phase</li>
 *     <li>An optional message for display or debugging</li>
 * </ul>
 */

public class EmailState {

    /**
     * Possible states of an email sending operation.
     */

    public enum Status {
        IDLE,
        LOADING,
        QUEUED,
        SUCCESS,
        FAILED
    }
    private final Status status;
    private final String message;


    /**
     * Creates a new immutable email state.
     *
     * @param status  the current operation status
     * @param message optional message associated with the state
     */


    private EmailState(Status status, String message) {
        this.status  = status;
        this.message = message;
    }

    /**
     * Creates an idle state.
     *
     * @return an {@link EmailState} with {@link Status#IDLE}
     */
    public static EmailState idle() {
        return new EmailState(Status.IDLE, null);
    }

    /**
     * Creates a loading state.
     *
     * @return an {@link EmailState} with {@link Status#LOADING}
     */

    public static EmailState loading() {
        return new EmailState(Status.LOADING, null);
    }

    /**
     * Creates a queued state with the current queue size.
     *
     * @param queueCount number of emails currently waiting in the queue
     * @return an {@link EmailState} with {@link Status#QUEUED}
     */

    public static EmailState queued(int queueCount) {
        return new EmailState(Status.QUEUED,
                queueCount + "Email waiting to be sent");
    }

    /**
     * Creates a success state.
     *
     * @return an {@link EmailState} with {@link Status#SUCCESS}
     */

    public static EmailState success() {
        return new EmailState(Status.SUCCESS, "Email sent successfully.");
    }

    /**
     * Creates a failed state with an error message.
     *
     * @param error error description to store in the state
     * @return an {@link EmailState} with {@link Status#FAILED}
     */

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
