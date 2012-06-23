package com.bourke.glimmr;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.groups.Group;
import com.gmail.yuyang226.flickr.groups.GroupList;
import com.gmail.yuyang226.flickr.oauth.OAuth;

/**
 *
 */
public class GroupListFragment extends BaseFragment
        implements IGroupListReadyListener {

    private static final String TAG = "Glimmr/GroupListFragment";

    public static GroupListFragment newInstance(OAuth oauth) {
        GroupListFragment newFragment = new GroupListFragment();
        newFragment.mOAuth = oauth;
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO new LoadGroupsTask(this, mOAuth.getUser()).execute(mOAuth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .standard_list_fragment, container, false);
        return mLayout;
    }

    @Override
    public void onGroupListReady(GroupList groups, boolean cancelled) {
        Log.d(TAG, "onGroupListReady");
		mGridAq = new AQuery(mActivity, mLayout);

		ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(mActivity,
                R.layout.group_list_item, groups) {

            // TODO: implement ViewHolder pattern
            // TODO: add aquery delay loading for fling scrolling
			@Override
			public View getView(final int position, View convertView,
                    ViewGroup parent) {

				if (convertView == null) {
					convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.group_list_item, null);
				}

                final Group group = getItem(position);
				AQuery aq = mGridAq.recycle(convertView);

                /*
                boolean useMemCache = true;
                boolean useFileCache = true;
                aq.id(R.id.image_item).image(photo.getSmallUrl(), useMemCache,
                        useFileCache,  0, 0, null, AQuery.FADE_IN_NETWORK);
                aq.id(R.id.image_item).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPhotoViewer(position);
                    }
                });

                aq.id(R.id.viewsText).text("Views: " + String.valueOf(photo
                            .getViews()));
                aq.id(R.id.ownerText).text(photo.getOwner().getUsername());
                aq.id(R.id.ownerText).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startProfileViewer(photo.getOwner());
                    }
                });
                */

				return convertView;
			}
		};
        // TODO mGridAq.id(R.id.list).adapter(adapter).itemClicked(this,
        //        "startGroupViewer");
		mGridAq.id(R.id.list).adapter(adapter);
    }
}
