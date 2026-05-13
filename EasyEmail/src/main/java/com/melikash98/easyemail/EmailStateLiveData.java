package com.melikash98.easyemail;

import androidx.lifecycle.MutableLiveData;

/**
 * Singleton holder for observing EasyEmail state changes.
 * <p>
 * This class exposes a shared {@link MutableLiveData} instance that can be observed
 * by the UI or other components to track email workflow states such as loading,
 * queued, success, and failure.
 * <p>
 * It is designed as a singleton so the entire library works with a single shared
 * state source.
 */

public class EmailStateLiveData {
    private static EmailStateLiveData INSTANCE;
    private final MutableLiveData<EmailState> liveData = new MutableLiveData<>();

    /**
     * Private constructor to prevent direct instantiation.
     * <p>
     * Initializes the state with {@link EmailState#idle()}.
     */

    private EmailStateLiveData() {
        liveData.postValue(EmailState.idle());
    }

    /**
     * Returns the singleton instance of this class.
     * <p>
     * Uses double-checked locking to ensure thread-safe lazy initialization.
     *
     * @return the shared {@link EmailStateLiveData} instance
     */

    public static EmailStateLiveData getInstance() {
        if (INSTANCE == null) {
            synchronized (EmailStateLiveData.class) {
                if (INSTANCE == null) INSTANCE = new EmailStateLiveData();
            }
        }
        return INSTANCE;
    }

    /**
     * Returns the shared LiveData object for observing email state updates.
     *
     * @return observable {@link MutableLiveData} of {@link EmailState}
     */

    public MutableLiveData<EmailState> getLiveData() {
        return liveData;
    }

    /**
     * Posts a new email state to observers.
     * <p>
     * This method is safe to call from background threads.
     *
     * @param state the new email state to publish
     */

    public void post(EmailState state) {
        liveData.postValue(state);
    }
}
