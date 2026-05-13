package com.melikash98.easyemail.State;

import androidx.lifecycle.MutableLiveData;

public class EmailStateLiveData {
    private static EmailStateLiveData INSTANCE;
    private final MutableLiveData<EmailState> liveData = new MutableLiveData<>();

    private EmailStateLiveData() {
        liveData.postValue(EmailState.idle());
    }

    public static EmailStateLiveData getInstance() {
        if (INSTANCE == null) {
            synchronized (EmailStateLiveData.class) {
                if (INSTANCE == null) INSTANCE = new EmailStateLiveData();
            }
        }
        return INSTANCE;
    }

    public MutableLiveData<EmailState> getLiveData() {
        return liveData;
    }

    public void post(EmailState state) {
        liveData.postValue(state);
    }
}
