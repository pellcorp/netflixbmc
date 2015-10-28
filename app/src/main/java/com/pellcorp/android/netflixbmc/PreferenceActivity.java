package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.os.Bundle;

import java.util.List;

public class PreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(
                android.R.id.content,
                new PreferenceFragment())
                .commit();
    }
}