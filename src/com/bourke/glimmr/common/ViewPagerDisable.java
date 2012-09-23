package com.bourke.glimmr.common;

import android.content.Context;

import android.support.v4.view.ViewPager;

import android.util.AttributeSet;

import android.view.MotionEvent;

/**
 * Extend ViewPager to allow the paging to be disabled/mEnabled.
 */
public class ViewPagerDisable extends ViewPager {

    private boolean mEnabled;

    public ViewPagerDisable(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        mEnabled = enabled;
    }
}
