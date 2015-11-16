package com.pellcorp.android.flixbmc;

/**
 * Created by jason on 17/11/15.
 */
public class LoginState {
    private final String failureReason;
    private final boolean status;

    public LoginState(boolean status, String failureReason) {
        this.status = status;
        this.failureReason = failureReason;
    }

    public boolean isSuccessful() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
