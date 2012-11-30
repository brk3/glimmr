package com.bourke.glimmr.common;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.R;
import android.app.Activity;

/**
 * Use to create an actionbar with a custom title font.
 */
public class GlimmrAbCustomTitle {

    protected TextView mAbTitle;
    protected BaseActivity mActivity;

    private TextUtils mTextUtils;

    public GlimmrAbCustomTitle(BaseActivity a) {
        mActivity = a;
        mTextUtils = new TextUtils(a.getAssets());
    }

    public void init(ActionBar actionbar) {
        LayoutInflater inflator = (LayoutInflater)
            mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_title_item, null);
        mAbTitle = (TextView) v.findViewById(R.id.abTitle);

        /* NOTE: Large screens don't use a custom title in landscape as there's
         * a problem/bug where it sits to the right of the tabs */
        if (mAbTitle != null) {
            mTextUtils.setFont(mAbTitle, TextUtils.FONT_SHADOWSINTOLIGHT);
            mAbTitle.setText(mActivity.getString(R.string.app_name));
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setCustomView(v);
        } else {
            actionbar.setTitle("");
        }
    }
}
