package com.bourke.glimmr.common;

import android.content.Context;

import android.graphics.Typeface;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.R;

public class GlimmrAbCustomSubTitle extends GlimmrAbCustomTitle {

    private TextView mAbSubTitle;

    public GlimmrAbCustomSubTitle(Context context) {
        super(context);
    }

    @Override
    public void init(ActionBar actionbar) {
        LayoutInflater inflator = (LayoutInflater)
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_subtitle_item, null);
        mAbTitle = (TextView) v.findViewById(R.id.abTitle);
        mAbSubTitle = (TextView) v.findViewById(R.id.abSubtitle);
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),
                Constants.FONT_SHADOWSINTOLIGHT);
        mAbTitle.setTypeface(typeface);
        mAbTitle.setText(mContext.getString(R.string.app_name));
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setCustomView(v);
    }

    public void setActionBarSubtitle(String subTitle) {
        mAbSubTitle.setText(subTitle);
    }

}
