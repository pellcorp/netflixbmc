package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Created by jason on 26/10/15.
 */
public class ActivityUtils {

    public static AlertDialog createErrorDialog(final Activity activity, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Error Message");
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }

    public static AlertDialog createSettingsMissingDialog(final Activity activity, String message) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.settings_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.startActivity(new Intent(
                                activity,
                                PreferenceFragment.class));
                    }
                });
        return builder.create();
    }

}
