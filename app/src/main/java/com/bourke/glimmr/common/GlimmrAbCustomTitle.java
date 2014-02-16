package com.bourke.glimmr.common;

import android.app.ActionBar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.BaseActivity;

/**
 * Use to create an actionbar with a custom title font.
 */
public class GlimmrAbCustomTitle {

    private final BaseActivity mActivity;

    private final TextUtils mTextUtils;

    public GlimmrAbCustomTitle(BaseActivity a) {
        mActivity = a;
        mTextUtils = new TextUtils(a.getAssets());
    }

    public void init(ActionBar actionbar) {
        LayoutInflater inflator = (LayoutInflater)
            mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_title_item, null);
        TextView abTitle = (TextView) v.findViewById(R.id.abTitle);
        mTextUtils.setFont(abTitle, TextUtils.FONT_SHADOWSINTOLIGHT);
        abTitle.setText(mActivity.getString(R.string.app_name));
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setCustomView(v);
    }
}
