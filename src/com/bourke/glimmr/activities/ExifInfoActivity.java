package com.bourke.glimmr.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.bourke.glimmr.R;

/**
 *
 */
public class ExifInfoActivity extends BaseActivity {

    private static final String TAG = "Glimmr/ExifInfoActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoviewer_overlay);
    }
}
