package com.bourke.glimmr.fragments.base;

import android.graphics.Bitmap;
import android.graphics.Color;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.IPhotoListReadyListener;
import com.bourke.glimmr.R;

import com.commonsware.cwac.endless.EndlessAdapter;

import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;

/**
 * Fragment that contains a GridView of photos.
 *
 * Can be used to display many of the Flickr "categories" of photos, i.e.
 * photostreams, favorites, contacts photos, etc.
 */
public abstract class PhotoGridFragment extends BaseFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoGridFragment";

    private EndlessGridAdapter mAdapter;

    protected int mPage = 1;
    protected boolean mMorePages = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .standard_gridview_fragment, container, false);
        return mLayout;
    }

    /**
     * Once the task comes back with the list of photos, set up the GridView
     * adapter etc. to display them.
     */
    @Override
    public void onPhotosReady(PhotoList photos, boolean cancelled) {
        Log.d(getLogTag(), "onPhotosReady");
        mGridAq = new AQuery(mActivity, mLayout);
        if (mAdapter == null || mCameFromPause) {
            // TODO: store mPage in onPause and restore it in onResume
            mCameFromPause = false;
            mPage = 1;
            //
            mPhotos = photos;
            mAdapter = new EndlessGridAdapter(photos);
            mAdapter.setRunInBackground(false);
            mGridAq.id(R.id.gridview).adapter(mAdapter).itemClicked(this,
                    "startPhotoViewer");
        } else {
            mPhotos.addAll(photos);
            mAdapter.addAll(photos);
            mAdapter.onDataReady();
        }
    }

    /**
     * Return false by default to indicate to the EndlessAdapter that there's
     * no more data to load.
     * Subclasses that support pagination should override this.
     */
    protected boolean cacheInBackground() {
        return false;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class EndlessGridAdapter extends EndlessAdapter {

        public EndlessGridAdapter(PhotoList list) {
            super(mActivity, new GridAdapter(list), R.layout.pending);
        }

        public void addAll(PhotoList list) {
            ArrayAdapter<Photo> a = (ArrayAdapter<Photo>) getWrappedAdapter();
            for (Photo photo : list) {
                a.add(photo);
            }
        }

        @Override
        protected boolean cacheInBackground() throws Exception {
            return PhotoGridFragment.this.cacheInBackground();
        }

        @Override
        protected void appendCachedData() {
        }
    }

    class GridAdapter extends ArrayAdapter<Photo> {

        public GridAdapter(PhotoList items) {
            super(mActivity, R.layout.gridview_item, android.R.id.text1,
                    items);
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.gridview_item, null);
                holder = new ViewHolder();
                holder.imageOverlay = (RelativeLayout) convertView
                    .findViewById(R.id.imageOverlay);
                holder.image = (ImageView) convertView.findViewById(
                        R.id.image_item);
                holder.ownerText = (TextView) convertView.findViewById(
                        R.id.ownerText);
                holder.viewsText = (TextView) convertView.findViewById(
                        R.id.viewsText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Photo photo = getItem(position);
            AQuery aq = mGridAq.recycle(convertView);

            /* Don't load image if flinging past it */
            if (aq.shouldDelay(position, convertView, parent,
                        photo.getSmallUrl())) {
                Bitmap placeholder = aq.getCachedImage(R.drawable.blank);
                aq.id(holder.image).image(placeholder);
            } else {
                aq.id(holder.image).image(photo.getSmallUrl(),
                        Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                        0, 0, null, AQuery.FADE_IN_NETWORK);
                aq.id(holder.image).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPhotoViewer(position);
                    }
                });

                /* Set tint on the image to flickr_blue when clicked */
                holder.image.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            ((ImageView) v).setColorFilter(Color.argb(
                                    100, 0, 99, 220));
                        } else {
                            ((ImageView) v).setColorFilter(null);
                        }
                        return false;
                    }
                });

                aq.id(holder.viewsText).text("Views: " + String.valueOf(photo
                            .getViews()));
                if (photo.getOwner() != null) {
                    aq.id(holder.ownerText).text(photo.getOwner()
                            .getUsername());
                    aq.id(holder.imageOverlay).clicked(
                            new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startProfileViewer(photo.getOwner());
                        }
                    });
                }
            }

            return convertView;
        }

        class ViewHolder {
            RelativeLayout imageOverlay;
            ImageView image;
            TextView ownerText;
            TextView viewsText;
        }
    }
}
