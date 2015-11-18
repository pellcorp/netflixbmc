package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;

public class ProgressDialogs {
    private final ProgressDialog progressDialog;
    private final ProgressDialog welcomeDialog;

    public ProgressDialogs(Activity activity) {
        progressDialog = ActivityUtils.createProgressDialog(activity);
        welcomeDialog =  ActivityUtils.createSplashDialog(activity);
    }

    public void show(boolean isWelcome) {
        if (isWelcome) {
            if (!isShowing()) {
                welcomeDialog.show();
            }
        } else {
            show();
        }
    }

    public void show() {
        if (!isShowing()) {
            progressDialog.show();
        }
    }

    public boolean isShowing() {
        return welcomeDialog.isShowing() || progressDialog.isShowing();
    }

    public void dismiss() {
        if (welcomeDialog.isShowing()) {
            welcomeDialog.dismiss();
        } else if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
