package com.pellcorp.android.flixbmc;

public class LoginResponse {
    private final String failureReason;
    private final boolean status;

    public LoginResponse(boolean status, String failureReason) {
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
