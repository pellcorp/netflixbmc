package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

public class ActivityUtils {

    public static AlertDialog createErrorDialog(final Activity activity, String title, String message, final boolean doFinish) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (doFinish) {
                            activity.finish();
                        }
                    }
                });
        return builder.create();
    }

    public static AlertDialog createSettingsMissingDialog(final Activity activity, String message) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.settings_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.startActivity(new Intent(
                        activity,
                        PreferenceActivity.class));
                activity.finish();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
            }
        });
        return builder.create();
    }

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_dialog);
        return dialog;
    }
}
