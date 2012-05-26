package com.bourke.glimmr;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import android.widget.RelativeLayout;

public class GroupsFragment extends SherlockFragment {

    protected String TAG = "Glimmr/FriendsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.standard_list_fragment, container, false);
        //initMainList(layout);
        return layout;
    }
}
