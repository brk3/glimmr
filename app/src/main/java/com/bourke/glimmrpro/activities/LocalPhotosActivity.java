package com.bourke.glimmrpro.activities;

import com.bourke.glimmrpro.BuildConfig;
import android.os.Bundle;
import android.util.Log;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.UsageTips;

public class LocalPhotosActivity extends BaseActivity {

    private static final String TAG = "Glimmr/LocalPhotosActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        setContentView(R.layout.local_photos_activity);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        UsageTips.getInstance().show(this, getString(R.string.upload_photos_tip), false);
    }
}
