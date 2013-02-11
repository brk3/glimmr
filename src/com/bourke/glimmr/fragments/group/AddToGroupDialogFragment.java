package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import android.widget.GridView;
import android.widget.AbsListView;

/**
 * Present a dialog to that allows the user to select photos from their
 * photo stream to add to the current group.
 */
public class AddToGroupDialogFragment extends BaseDialogFragment {

    public static final String TAG = "Glimmr/AddToGroupDialogFragment";

    public static AddToGroupDialogFragment newInstance() {
        AddToGroupDialogFragment newFragment = new AddToGroupDialogFragment();
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle("Choose some photos");

        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.add_to_group_fragment, container, false);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        boolean retainInstance = false;
        PhotoStreamGridFragment frag =
            PhotoStreamGridFragment.newInstance(retainInstance,
                    AbsListView.CHOICE_MODE_MULTIPLE);
        ft.replace(R.id.photoStreamFragment, frag);
        ft.commit();

        return mLayout;
    }
}
