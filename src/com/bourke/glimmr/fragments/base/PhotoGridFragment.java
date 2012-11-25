package com.bourke.glimmr.fragments.base;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.activities.ProfileActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.event.Events.PhotoItemLongClickDialogListener;
import com.bourke.glimmr.R;

import com.commonsware.cwac.endless.EndlessAdapter;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Fragment that contains a GridView of photos.
 *
 * Can be used to display many of the Flickr "categories" of photos, i.e.
 * photostreams, favorites, contacts photos, etc.
 */
public abstract class PhotoGridFragment extends BaseFragment
        implements IPhotoListReadyListener, PhotoItemLongClickDialogListener {

    private static final String TAG = "Glimmr/PhotoGridFragment";

    protected AdapterView mGridView;
    protected EndlessGridAdapter mAdapter;

    protected List<Photo> mPhotos = new ArrayList<Photo>();
    protected List<Photo> mNewPhotos = new ArrayList<Photo>();
    protected int mPage = 1;
    protected boolean mMorePages = true;
    protected boolean mShowProfileOverlay = false;
    protected boolean mShowDetailsOverlay = true;

    private ViewGroup mNoConnectionLayout;
    private ViewGroup mEmptyView;

    public abstract String getNewestPhotoId();
    public abstract void storeNewestPhotoId(Photo photo);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (Constants.DEBUG) {
            Log.d("(PhotoGridFragment)" + getLogTag(), "onCreateView");
        }
        mLayout = (RelativeLayout) inflater.inflate(R.layout.gridview_fragment,
                container, false);
        mAq = new AQuery(mActivity, mLayout);
        mNoConnectionLayout =
            (ViewGroup) mLayout.findViewById(R.id.no_connection_layout);
        mEmptyView = (ViewGroup) mLayout.findViewById(android.R.id.empty);
        mGridView = (AdapterView) mLayout.findViewById(R.id.gridview);
        initGridView();
        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constants.DEBUG) {
            Log.d("(PhotoGridFragment)" + getLogTag(), "onResume");
        }
        if (mPhotos != null && !mPhotos.isEmpty()) {
            LinearLayout layoutEmpty = (LinearLayout)
                mLayout.findViewById(android.R.id.empty);
            layoutEmpty.setVisibility(View.INVISIBLE);
            GridView gridView = (GridView) mLayout.findViewById(R.id.gridview);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPhotosReady(List<Photo> photos) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onPhotosReady");
        if (photos == null) {
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
        } else {
            mNoConnectionLayout.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            checkForNewPhotos(photos);
            mPhotos.addAll(photos);
            mAdapter.onDataReady();
        }
        mEmptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void refresh() {
        Log.d(getLogTag(), "refresh");
        mPhotos.clear();
        mPage = 1;
    }

    private void initGridView() {
        mAdapter = new EndlessGridAdapter(mPhotos);
        mAdapter.setRunInBackground(false);
        mGridView = (GridView) mLayout.findViewById(R.id.gridview);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                PhotoViewerActivity.startPhotoViewer(mActivity, mPhotos,
                    position);
            }
        });
        mGridView.setOnItemLongClickListener(
                new GridView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                    int position, long id) {
                if (mOAuth == null) {
                    return false;
                }
                if (position < mPhotos.size()) {
                    SherlockDialogFragment d =
                        PhotoItemLongClickDialog.newInstance(mActivity,
                            PhotoGridFragment.this, mPhotos.get(position));
                    d.show(mActivity.getSupportFragmentManager(),
                        "photo_item_long_click");
                } else {
                    Log.e(getLogTag(), String.format(
                            "Cannot call showGridItemContextMenu, " +
                            "mPhotos.size(%d) != position:(%d)",
                            mPhotos.size(), position));
                }
                /* True indicates we're finished with event and triggers haptic
                 * feedback */
                return true;
            }
        });
        mGridView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLongClickDialogSelection(Photo photo, int which) {
        if (photo == null) {
            Log.e(getLogTag(), "showGridItemContextMenu: photo is null");
            return;
        }
        ProfileActivity.startProfileViewer(mActivity, photo.getOwner());
    }

    /**
     * If we have a most recent id stored, see if it exists in the photo
     * list we just fetched. If so, all photos before that id in the list
     * are said to be new.
     */
    protected void checkForNewPhotos(List<Photo> photos) {
        if (photos == null || photos.isEmpty()) {
            if (Constants.DEBUG)
                Log.d(getLogTag(), "checkForNewPhotos: photos null or empty");
            return;
        }

        mNewPhotos = new ArrayList<Photo>();
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);

        String newestId = getNewestPhotoId();
        if (newestId != null) {
            for (int i=0; i < photos.size(); i++) {
                Photo p = photos.get(i);
                if (p.getId().equals(newestId)) {
                    mNewPhotos = photos.subList(0, i);
                    if (Constants.DEBUG)
                        Log.d(getLogTag(), String.format("Found %d new photos",
                                mNewPhotos.size()));
                    break;
                }
            }
        }

        if (mNewPhotos != null && !mNewPhotos.isEmpty()) {
            storeNewestPhotoId(mNewPhotos.get(0));
        } else {
            if (Constants.DEBUG)
                Log.d(getLogTag(), "mNewPhotos null or empty, using most " +
                    "recent fetched photo as newest");
            storeNewestPhotoId(photos.get(0));
        }
    }

    /**
     * Return false by default to indicate to the EndlessAdapter that there's
     * no more data to load.
     *
     * Subclasses that support pagination should override this.
     */
    protected boolean cacheInBackground() {
        return false;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public EndlessAdapter getAdapter() {
        return mAdapter;
    }

    class EndlessGridAdapter extends EndlessAdapter {

        public EndlessGridAdapter(List<Photo> list) {
            super(mActivity, new GridAdapter(list), R.layout.pending);
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

        public GridAdapter(List<Photo> items) {
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
                holder.imageOverlay = (LinearLayout) convertView
                    .findViewById(R.id.imageOverlay);
                holder.image = (ImageView) convertView.findViewById(
                        R.id.image_item);
                holder.imageNewRibbon = (ImageView) convertView.findViewById(
                        R.id.imageNewRibbon);
                holder.ownerText = (TextView) convertView.findViewById(
                        R.id.ownerText);
                holder.viewsText = (TextView) convertView.findViewById(
                        R.id.viewsText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Photo photo = getItem(position);

            /* Don't load image if flinging past it */
            if (mAq.shouldDelay(position, convertView, parent,
                        photo.getLargeSquareUrl())) {
                Bitmap placeholder = mAq.getCachedImage(R.drawable.blank);
                mAq.id(holder.image).image(placeholder);
                holder.imageOverlay.setVisibility(View.INVISIBLE);
            } else {
                if (mShowDetailsOverlay) {
                    holder.imageOverlay.setVisibility(View.VISIBLE);
                } else {
                    holder.imageOverlay.setVisibility(View.INVISIBLE);
                }

                mActivity.setFont(holder.ownerText, Constants.FONT_ROBOTOBOLD);

                /* Fetch the main photo */
                mAq.id(holder.image).image(photo.getLargeSquareUrl(),
                        Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                        0, 0, null, AQuery.FADE_IN_NETWORK);

                /* Set the overlay views and owner info */
                String viewsText = String.format("%s: %s",
                        mActivity.getString(R.string.views),
                        String.valueOf(photo.getViews()));
                holder.viewsText.setText(viewsText);
                if (photo.getOwner() != null) {
                    holder.ownerText.setText(photo.getOwner().getUsername());
                    holder.imageOverlay.setOnClickListener(
                            new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProfileActivity.startProfileViewer(mActivity,
                                photo.getOwner());
                        }
                    });
                }

                /* Show ribbon in corner if photo is new */
                holder.imageNewRibbon.setVisibility(View.INVISIBLE);
                if (mNewPhotos != null) {
                    for (Photo p : mNewPhotos) {
                        if (p.getId().equals(photo.getId())) {
                            holder.imageNewRibbon.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            return convertView;
        }

        class ViewHolder {
            LinearLayout imageOverlay;
            ImageView image;
            ImageView imageNewRibbon;
            TextView ownerText;
            TextView viewsText;
        }
    }

    static class PhotoItemLongClickDialog extends SherlockDialogFragment {
        private PhotoItemLongClickDialogListener mListener;
        private Context mContext;
        private Photo mPhoto;

        public static PhotoItemLongClickDialog newInstance(Context context,
                PhotoItemLongClickDialogListener listener, Photo photo) {
            PhotoItemLongClickDialog newDialog =
                new PhotoItemLongClickDialog();
            newDialog.mListener = listener;
            newDialog.mContext = context;
            newDialog.mPhoto = photo;
            return newDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setItems(R.array.photo_item_long_click_dialog_items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onLongClickDialogSelection(mPhoto, which);
                    }
                });
            return builder.create();
        }
    }

}
