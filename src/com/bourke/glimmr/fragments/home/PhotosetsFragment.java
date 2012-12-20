package com.bourke.glimmr.fragments.home;

import android.graphics.Bitmap;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.activities.PhotosetViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotosetsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadPhotosetsTask;

import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.util.ArrayList;
import java.util.List;

public class PhotosetsFragment extends BaseFragment
        implements IPhotosetsReadyListener {

    private static final String TAG = "Glimmr/PhotosetsFragment";

    private LoadPhotosetsTask mTask;
    private List<Photoset> mPhotosets = new ArrayList<Photoset>();

    private View mLayoutNoConnection;
    private AdapterView mAdapterView;  /* Will either be a GridView or ListView
                                          depending on screen size */
    private SetListAdapter mAdapter;
    private View mViewEmpty;

    public static PhotosetsFragment newInstance() {
        PhotosetsFragment newFragment = new PhotosetsFragment();
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPhotosetsTask(this, mActivity.getUser());
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            if (Constants.DEBUG) Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        /* If sw600dp-land, use gridview layout, otherwise use list style
         * layout */
        if (getResources().getBoolean(R.bool.sw600dp_land)) {
            mLayout = (RelativeLayout) inflater.inflate(
                    R.layout.gridview_fragment, container, false);
            mAdapterView = (GridView) mLayout.findViewById(R.id.gridview);
        } else {
            mLayout = (RelativeLayout) inflater.inflate(
                    R.layout.listview_fragment, container, false);
            mAdapterView = (ListView) mLayout.findViewById(R.id.list);
        }

        mLayoutNoConnection =
            (LinearLayout) mLayout.findViewById(R.id.no_connection_layout);
        mViewEmpty = (LinearLayout) mLayout.findViewById(android.R.id.empty);
        mAq = new AQuery(mActivity, mLayout);

        initAdapterView();

        return mLayout;
    }

    private void initAdapterView() {
        mAdapter = new SetListAdapter(mActivity, R.layout.photoset_cover_item,
                (ArrayList<Photoset>)mPhotosets);
        mAdapterView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        PhotosetViewerActivity.startPhotosetViewer(mActivity,
                            mPhotosets.get(position));
                    }
                });
        mAdapterView.setAdapter(mAdapter);
    }

    @Override
    public void onPhotosetsReady(Photosets photoSets) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onPhotosetListReady");
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (photoSets == null) {
            mLayoutNoConnection.setVisibility(View.VISIBLE);
            mAdapterView.setVisibility(View.GONE);
        } else {
            mAdapterView.setVisibility(View.VISIBLE);
            mLayoutNoConnection.setVisibility(View.GONE);
            mPhotosets.clear();
            mPhotosets.addAll(photoSets.getPhotosets());
            mAdapter.notifyDataSetChanged();
        }
        mViewEmpty.setVisibility(View.INVISIBLE);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class SetListAdapter extends ArrayAdapter<Photoset> {

        public SetListAdapter(BaseActivity activity, int textViewResourceId,
                ArrayList<Photoset> objects) {
            super(activity, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.photoset_cover_item, null);
                holder = new ViewHolder();
                holder.imageItem = (ImageView)
                    convertView.findViewById(R.id.imageItem);
                holder.imageOverlay = (LinearLayout)
                    convertView.findViewById(R.id.imageOverlay);
                holder.photosetNameText = (TextView)
                    convertView.findViewById(R.id.photosetNameText);
                holder.numImagesInSetText = (TextView)
                    convertView.findViewById(R.id.numImagesInSetText);
                holder.numImagesIcon = (ImageView)
                    convertView.findViewById(R.id.numImagesIcon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mTextUtils.setFont(holder.photosetNameText,
                    TextUtils.FONT_ROBOTOBOLD);

            final Photoset photoset = mPhotosets.get(position);

            /* Don't load image if flinging past it */
            if (mAq.shouldDelay(position, convertView, parent,
                        photoset.getPrimaryPhoto().getMediumUrl())) {
                Bitmap placeholder = mAq.getCachedImage(R.drawable.blank);
                mAq.id(holder.imageItem).image(placeholder);
                holder.imageOverlay.setVisibility(View.INVISIBLE);
            } else {
                /* Fetch the set cover photo */
                holder.imageOverlay.setVisibility(View.VISIBLE);
                mAq.id(holder.imageItem).image(
                        photoset.getPrimaryPhoto().getMediumUrl(),
                        Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                        0, 0, null, AQuery.FADE_IN_NETWORK);

                holder.photosetNameText.setText(
                        photoset.getTitle().toUpperCase());
                holder.numImagesInSetText.setText(
                        ""+photoset.getPhotoCount());
            }
            return convertView;
        }

        class ViewHolder {
            ImageView imageItem;
            ImageView numImagesIcon;
            TextView photosetNameText;
            TextView numImagesInSetText;
            LinearLayout imageOverlay;
        }
    }
}
