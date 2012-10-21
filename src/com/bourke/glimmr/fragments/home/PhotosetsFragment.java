package com.bourke.glimmr.fragments.home;

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

import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.util.ArrayList;
import java.util.List;
import android.widget.LinearLayout;
import android.graphics.Typeface;

/**
 *
 */
public class PhotosetsFragment extends BaseFragment
        implements IPhotosetsReadyListener {

    private static final String TAG = "Glimmr/PhotosetsFragment";

    private LoadPhotosetsTask mTask;
    private List<Photoset> mPhotosets = new ArrayList<Photoset>();

    public static PhotosetsFragment newInstance() {
        PhotosetsFragment newFragment = new PhotosetsFragment();
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadPhotosetsTask(this, this, mActivity.getUser());
        mTask.execute(mOAuth);
    }

    @Override
    protected void refresh() {
        super.refresh();
        startTask();
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
        if (getResources().getBoolean(R.bool.sw600dp_land)) {
            mLayout = (RelativeLayout) inflater.inflate(
                    R.layout.gridview_fragment, container, false);
        } else {
            mLayout = (RelativeLayout) inflater.inflate(
                    R.layout.listview_fragment, container, false);
        }
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        PhotosetViewerActivity.startPhotosetViewer(mActivity,
                mPhotosets.get(position));
    }

    @Override
    public void onPhotosetsReady(Photosets photoSets) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onPhotosetListReady");

        int adapterView = R.id.list;
        if (getResources().getBoolean(R.bool.sw600dp_land)) {
            adapterView = R.id.gridview;
        }

        if (photoSets == null) {
            mAq.id(R.id.no_connection_layout).visible();
            mAq.id(adapterView).gone();
        } else {
            mAq.id(adapterView).visible();
            mAq.id(R.id.no_connection_layout).gone();
            mPhotosets = new ArrayList<Photoset>(photoSets.getPhotosets());
            SetListAdapter adapter = new SetListAdapter(
                    mActivity, R.layout.photoset_cover_item,
                    (ArrayList<Photoset>)mPhotosets);
            mAq.id(adapterView).adapter(adapter).itemClicked(this,
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

            mActivity.setFont(holder.photosetNameText,
                    Constants.FONT_ROBOTOBOLD);

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

                aq.id(holder.photosetNameText).text(
                        photoset.getTitle().toUpperCase());
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
            LinearLayout imageOverlay;
        }
    }
}
