package com.bourke.glimmr.activities;

import android.os.Bundle;
import android.util.Log;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.UsageTips;

public class LocalPhotosActivity extends BaseActivity {

    private static final String TAG = "Glimmr/LocalPhotosActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(TAG, "onCreate");

        setContentView(R.layout.local_photos_activity);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        UsageTips.getInstance().show(this, getString(R.string.upload_photos_tip), false);
    }
}
