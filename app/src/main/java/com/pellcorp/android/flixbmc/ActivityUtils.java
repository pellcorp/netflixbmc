package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class ActivityUtils {

    public static AlertDialog createErrorDialog(final Activity activity, String message, final boolean doFinish) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
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

    public static AlertDialog createSettingsMissingDialog(final Activity activity, String message, final boolean doFinish) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.settings_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.startActivity(new Intent(
                                activity,
                                PreferenceActivity.class));
                        if (doFinish) {
                            activity.finish();
                        }
                    }
                });
        return builder.create();
    }

}
