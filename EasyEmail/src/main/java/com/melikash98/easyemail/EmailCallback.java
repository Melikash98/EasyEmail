package com.melikash98.easyemail;

public interface EmailCallback {
    void onSuccess();
    void onError(String error);
}
