package com.pellcorp.android.flixbmc.web;

public class LoginResponse {
    private boolean isInvalidCredentials;
    private boolean isUnableToProcessRequest;

    private final String failureReason;
    private final boolean status;

    public LoginResponse(boolean status, String failureReason) {
        this.status = status;
        this.failureReason = failureReason;
        if (failureReason != null) {
            this.isInvalidCredentials = failureReason.contains("The login information you entered");

            this.isUnableToProcessRequest = failureReason.contains("Sorry, we are unable to process your request");
        } else {
            this.isInvalidCredentials = false;
            this.isUnableToProcessRequest = false;
        }
    }

    public boolean isSuccessful() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public boolean isInvalidCredentials() {
        return isInvalidCredentials;
    }

    public boolean isUnableToProcessRequest() {
        return isUnableToProcessRequest;
    }
}
