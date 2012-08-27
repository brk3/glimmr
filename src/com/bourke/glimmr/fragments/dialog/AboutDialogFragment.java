package com.bourke.glimmr.fragments.dialog;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.R;

public final class AboutDialogFragment extends SherlockDialogFragment {

    private static final String TAG = "Glimmr/AboutDialogFragment";

    private ViewGroup mLayout;
    private AQuery mAq;

    public AboutDialogFragment() {
        /* Empty constructor required for DialogFragment */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.about_fragment, container, false);
        mAq = new AQuery(getSherlockActivity(), mLayout);
        return mLayout;
    }
}
