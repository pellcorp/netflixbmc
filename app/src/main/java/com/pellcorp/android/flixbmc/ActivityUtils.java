package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class ActivityUtils {
    public enum DialogType {
        OK_FINISH, OK_RECREATE, OK_FINISH_NO_CANCEL
    }

    public static void createErrorDialog(final Activity activity, int messageId, final DialogType dialogType) {
        createErrorDialog(activity, activity.getString(messageId), dialogType);
    }

    public static void createErrorDialog(final Activity activity, String message, final DialogType dialogType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);

        int positiveButton = dialogType.equals(DialogType.OK_RECREATE) ? R.string.retry : android.R.string.ok;
        builder.setPositiveButton(positiveButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (dialogType.equals(DialogType.OK_FINISH) || dialogType.equals(DialogType.OK_FINISH_NO_CANCEL)) {
                            activity.finish();
                        } else if (dialogType.equals(DialogType.OK_RECREATE)) {
                            activity.recreate();
                        }
                    }
                });

        if (dialogType.equals(DialogType.OK_FINISH)) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        } else if (dialogType.equals(DialogType.OK_RECREATE)) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    activity.finish();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void createSettingsMissingDialog(final Activity activity, int messageId) {
        createSettingsMissingDialog(activity, activity.getString(messageId));
    }

    public static void createSettingsMissingDialog(final Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                activity.startActivity(new Intent(
                        activity,
                        PreferenceActivity.class));
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                activity.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static ProgressDialog createProgressDialog(Context context) {
        return createProgressDialog(context, R.layout.progress_dialog);
    }

    public static ProgressDialog createSplashDialog(Context context) {
        return createProgressDialog(context, R.layout.welcome_progress_dialog);
    }

    public static ProgressDialog createProgressDialog(Context mContext, int layout) {
        ProgressDialog dialog = new ProgressDialog(mContext);

        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(layout);
        dialog.dismiss(); // hack to hide after its configured as we might not need it immediately
        return dialog;
    }
}
