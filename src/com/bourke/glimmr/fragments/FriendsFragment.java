package com.bourke.glimmr;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.gmail.yuyang226.flickr.oauth.OAuth;

public class FriendsFragment extends BaseFragment {

    protected String TAG = "Glimmr/FriendsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getSherlockActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.standard_list_fragment, container, false);
        //initMainList(layout);
        return layout;
    }

    @Override
    public void onAuthorised(OAuth oauth) {

    }
}
