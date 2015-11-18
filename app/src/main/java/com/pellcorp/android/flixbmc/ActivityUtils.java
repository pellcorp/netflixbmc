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

                activity.finish();
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
}
