package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

public abstract class AbstractProgressActivity extends Activity implements ProgressSpinner {
    private ProgressDialog progressDialog;

    private void init() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);

            try {
                progressDialog.show();
            } catch (WindowManager.BadTokenException e) {
            }

            try {
                progressDialog.setCancelable(false);
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                progressDialog.setContentView(R.layout.progress_dialog);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void showSpinner() {
        if (!isShowing()) {
            init();

            try {
                progressDialog.show();
            } catch (WindowManager.BadTokenException e) {
            }
        }
    }

    @Override
    public void hideSpinner() {
        if (isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean isShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }
}
