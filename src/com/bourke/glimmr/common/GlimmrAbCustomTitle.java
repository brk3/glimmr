package com.bourke.glimmrpro.common;

import android.content.Context;

import android.graphics.Typeface;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.R;

/**
 * Use to create an actionbar with a custom title font.
 */
public class GlimmrAbCustomTitle {

    protected TextView mAbTitle;
    protected Context mContext;

    public GlimmrAbCustomTitle(Context context) {
        mContext = context;
    }

    public void init(ActionBar actionbar) {
        LayoutInflater inflator = (LayoutInflater)
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_title_item, null);
        mAbTitle = (TextView) v.findViewById(R.id.abTitle);

        /* NOTE: Large screens don't use a custom title in landscape as there's
         * a problem/bug where it sits to the right of the tabs */
        if (mAbTitle != null) {
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),
                    Constants.FONT_SHADOWSINTOLIGHT);
            mAbTitle.setTypeface(typeface);
            mAbTitle.setText(mContext.getString(R.string.app_name));
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setCustomView(v);
        } else {
            actionbar.setTitle("");
        }
    }
}
