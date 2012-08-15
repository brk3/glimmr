package com.bourke.glimmr.fragments.home;

import android.content.Intent;

import android.graphics.Bitmap;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.activities.PhotosetViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotosetsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadPhotosetsTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PhotosetsFragment extends BaseFragment
        implements IPhotosetsReadyListener {

    private static final String TAG = "Glimmr/PhotosetsFragment";

    private LoadPhotosetsTask mTask;
    private List<Photoset> mPhotosets = new ArrayList<Photoset>();
    private User mUser;

    public static PhotosetsFragment newInstance(User user) {
        PhotosetsFragment newFragment = new PhotosetsFragment();
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadPhotosetsTask(mActivity, this, mUser);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .photoset_list_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    private void startPhotosetViewer(Photoset photoset) {
        if (photoset == null) {
            Log.e(getLogTag(),
                    "Cannot start SetViewerActivity, photoset is null");
            return;
        }
        Log.d(getLogTag(), "Starting SetViewerActivity for "
                + photoset.getTitle());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PHOTOSETVIEWER_PHOTOSET,
                photoset);
        bundle.putSerializable(Constants.KEY_PHOTOSETVIEWER_USER, mUser);
        Intent photosetViewer = new Intent(mActivity, PhotosetViewerActivity
                .class);
        photosetViewer.putExtras(bundle);
        mActivity.startActivity(photosetViewer);
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        startPhotosetViewer(mPhotosets.get(position));
    }

    @Override
    public void onPhotosetsReady(Photosets photoSets) {
        Log.d(getLogTag(), "onPhotosetListReady");

        if (photoSets == null) {
            mAq.id(R.id.no_connection_layout).visible();
            mAq.id(R.id.list).gone();
        } else {
            mPhotosets = new ArrayList(photoSets.getPhotosets());
            SetListAdapter adapter = new SetListAdapter(
                    mActivity, R.layout.photoset_list_item,
                    (ArrayList<Photoset>)mPhotosets);
            mAq.id(R.id.list).adapter(adapter).itemClicked(this,
                    "itemClicked");
        }
        mAq.id(android.R.id.empty).invisible();
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
                        R.layout.photoset_list_item, null);
                holder = new ViewHolder();
                holder.imageItem = (ImageView)
                    convertView.findViewById(R.id.imageItem);
                holder.imageOverlay = (RelativeLayout)
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

            final Photoset photoset = mPhotosets.get(position);
            AQuery aq = mAq.recycle(convertView);

            /* Don't load image if flinging past it */
            if (aq.shouldDelay(position, convertView, parent,
                        photoset.getPrimaryPhoto().getMediumUrl())) {
                Bitmap placeholder = aq.getCachedImage(R.drawable.blank);
                aq.id(holder.imageItem).image(placeholder);
                aq.id(holder.imageOverlay).invisible();
            } else {
                /* Fetch the set cover photo */
                aq.id(holder.imageOverlay).visible();
                aq.id(holder.imageItem).image(
                        photoset.getPrimaryPhoto().getMediumUrl(),
                        Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                        0, 0, null, AQuery.FADE_IN_NETWORK);

                aq.id(holder.photosetNameText).text(photoset.getTitle());
                aq.id(holder.numImagesInSetText).text(
                        ""+photoset.getPhotoCount());
            }
            return convertView;
        }

        class ViewHolder {
            ImageView imageItem;
            ImageView numImagesIcon;
            TextView photosetNameText;
            TextView numImagesInSetText;
            RelativeLayout imageOverlay;
        }
    }
}
