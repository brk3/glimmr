package com.bourke.glimmrpro.fragments.base;

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

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.event.Events.PhotoItemLongClickDialogListener;
import com.bourke.glimmrpro.R;

import com.commonsware.cwac.endless.EndlessAdapter;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

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

    private GridView mGridView;
    private EndlessGridAdapter mAdapter;

    protected PhotoList mPhotos = new PhotoList();
    protected List<Photo> mNewPhotos = new ArrayList<Photo>();
    protected int mPage = 1;
    protected boolean mMorePages = true;
    protected boolean mShowProfileOverlay = false;
    protected boolean mShowDetailsOverlay = true;

    public abstract String getNewestPhotoId();
    public abstract void storeNewestPhotoId(Photo photo);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout.gridview_fragment,
                container, false);
        mAq = new AQuery(mActivity, mLayout);
        initGridView();
        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constants.DEBUG) Log.d(getLogTag(), "onResume");
        if (mPhotos != null && !mPhotos.isEmpty()) {
            mAq.id(android.R.id.empty).invisible();
            mAq.id(R.id.gridview).visible();
        }
    }

    @Override
    public void onPhotosReady(PhotoList photos) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onPhotosReady");
        if (photos == null) {
            mAq.id(R.id.no_connection_layout).visible();
            mAq.id(R.id.gridview).gone();
        } else {
            mAq.id(R.id.no_connection_layout).gone();
            mAq.id(R.id.gridview).visible();
            checkForNewPhotos(photos);
            mPhotos.addAll(photos);
            mAdapter.onDataReady();
        }
        mAq.id(android.R.id.empty).invisible();
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
                startPhotoViewer(mPhotos, position);
            }
        });
        mGridView.setOnItemLongClickListener(
                new GridView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                    int position, long id) {
                if (position < mPhotos.size()) {
                    SherlockDialogFragment d = PhotoItemLongClickDialog
                        .newInstance(mActivity, PhotoGridFragment.this, mPhotos.get(position));
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
        mAq.id(R.id.gridview).invisible();
    }

    @Override
    public void onLongClickDialogSelection(Photo photo, int which) {
        if (photo == null) {
            Log.e(getLogTag(), "showGridItemContextMenu: photo is null");
            return;
        }
        startProfileViewer(photo.getOwner());
    }

    /**
     * If we have a most recent id stored, see if it exists in the photo
     * list we just fetched. If so, all photos before that id in the list
     * are said to be new.
     */
    protected void checkForNewPhotos(PhotoList photos) {
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

        public EndlessGridAdapter(PhotoList list) {
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
            AQuery aq = mAq.recycle(convertView);

            /* Don't load image if flinging past it */
            if (aq.shouldDelay(position, convertView, parent,
                        photo.getLargeSquareUrl())) {
                Bitmap placeholder = aq.getCachedImage(R.drawable.blank);
                aq.id(holder.image).image(placeholder);
                aq.id(holder.imageOverlay).invisible();
            } else {
                if (mShowDetailsOverlay) {
                    aq.id(holder.imageOverlay).visible();
                } else {
                    aq.id(holder.imageOverlay).invisible();
                }

                /* Fetch the main photo */
                aq.id(holder.image).image(photo.getLargeSquareUrl(),
                        Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                        0, 0, null, AQuery.FADE_IN_NETWORK);

                /* Set the overlay views and owner info */
                String viewsText = String.format("%s: %s",
                        mActivity.getString(R.string.views),
                        String.valueOf(photo.getViews()));
                aq.id(holder.viewsText).text(viewsText);
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

                /* Show ribbon in corner if photo is new */
                mAq.id(holder.imageNewRibbon).invisible();
                if (mNewPhotos != null) {
                    for (Photo p : mNewPhotos) {
                        if (p.getId().equals(photo.getId())) {
                            mAq.id(holder.imageNewRibbon).visible();
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
