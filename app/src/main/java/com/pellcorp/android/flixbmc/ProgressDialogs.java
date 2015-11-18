package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

public class ProgressDialogs {
    private ProgressDialog progressDialog;
    private ProgressDialog welcomeDialog;
    private final Activity ctx;

    public ProgressDialogs(Activity ctx) {
        this.ctx = ctx;
    }

    private void init(boolean isWelcome) {
        if (isWelcome) {
            if (welcomeDialog == null) {
                welcomeDialog = createProgressDialog(R.layout.welcome_progress_dialog);
            }
        } else {
            if (progressDialog == null) {
                progressDialog = createProgressDialog(R.layout.progress_dialog);
            }
        }
    }

    public void show(boolean isWelcome) {
        init(isWelcome);

        if (isWelcome) {
            if (!isShowing()) {
                show(welcomeDialog);
            }
        } else {
            show();
        }
    }

    public void show() {
        // if welcome dialog is already showing, don't create the progress one yet
        if (!isShowing()) {
            init(false);

            show(progressDialog);
        }
    }

    public boolean isShowing() {
        return isShowing(welcomeDialog) || isShowing(progressDialog);
    }

    private boolean isShowing(ProgressDialog dialog) {
        return dialog != null && dialog.isShowing();
    }

    private void show(ProgressDialog dialog) {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (isShowing(welcomeDialog)) {
            welcomeDialog.dismiss();
        } else if (isShowing(progressDialog)) {
            progressDialog.dismiss();
        }
    }

    private ProgressDialog createProgressDialog(int layout) {
        if (ctx != null) {
            ProgressDialog dialog = new ProgressDialog(ctx);

            try {
                dialog.show();
            } catch (WindowManager.BadTokenException e) {

            }

            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(layout);
            return dialog;
        } else {
            return null;
        }
    }
}
