package com.bourke.glimmr.fragments.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
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

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.activities.ProfileViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.event.Events.PhotoItemLongClickDialogListener;
import com.commonsware.cwac.endless.EndlessAdapter;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Fragment that contains a GridView of photos.
 *
 * Can be used to display many of the Flickr "categories" of photos, i.e.
 * photostreams, favorites, contacts photos, etc.
 */
public abstract class PhotoGridFragment extends BaseFragment
        implements IPhotoListReadyListener, PhotoItemLongClickDialogListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "Glimmr/PhotoGridFragment";

    protected GridView mGridView;
    private EndlessGridAdapter mAdapter;

    protected final List<Photo> mPhotos = new ArrayList<Photo>();
    private List<Photo> mNewPhotos = new ArrayList<Photo>();
    protected int mPage = 1;
    protected boolean mMorePages = true;
    protected boolean mShowDetailsOverlay = true;
    protected AsyncTask mTask;

    protected boolean mRetainInstance = true;
    protected int mGridChoiceMode = ListView.CHOICE_MODE_SINGLE;

    private ViewGroup mNoConnectionLayout;

    private SwipeRefreshLayout mSwipeLayout;

    protected abstract String getNewestPhotoId();
    protected abstract void storeNewestPhotoId(Photo photo);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d("(PhotoGridFragment)" + getLogTag(), "onCreateView");
        }
        mLayout = (RelativeLayout) inflater.inflate(R.layout.gridview_fragment, container,
                false);

        mSwipeLayout = (SwipeRefreshLayout) mLayout.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorScheme(R.color.flickr_pink, R.color.flickr_blue, R.color.flickr_pink,
                R.color.flickr_blue);
        mSwipeLayout.setOnRefreshListener(this);

        mNoConnectionLayout = (ViewGroup) mLayout.findViewById(R.id.no_connection_layout);

        initGridView();

        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Log.d("(PhotoGridFragment)" + getLogTag(), "onResume");
        }
        if (!mPhotos.isEmpty()) {
            GridView gridView = (GridView) mLayout.findViewById(R.id.gridview);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPhotosReady(List<Photo> photos, Exception e) {
        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        mSwipeLayout.setRefreshing(false);

        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
        if (photos == null) {
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            return;
        }
        if (photos.isEmpty()) {
            mMorePages = false;
        }
        mNoConnectionLayout.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
        checkForNewPhotos(photos);
        mPhotos.addAll(photos);
        mAdapter.onDataReady();
    }

    @Override
    public void onRefresh() {
        mSwipeLayout.setRefreshing(true);
        refresh();
    }

    @Override
    protected void refresh() {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "refresh");
        mPage = 1;
        mMorePages = true;
        if (mPhotos.size() > 0) {
            mPhotos.clear();
            cacheInBackground();
        }
    }

    protected int getGridChoiceMode() {
        return mGridChoiceMode;
    }

    public List<Photo> getSelectedPhotos() {
        List<Photo> ret = new ArrayList<Photo>();
        if (mGridView.getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE) {
            Log.e(TAG, "PhotoGridFragment not in CHOICE_MODE_MULTIPLE");
            return ret;
        }
        SparseBooleanArray checkArray = mGridView.getCheckedItemPositions();
        for (int i=0; i < checkArray.size(); i++) {
            if (checkArray.valueAt(i)) {
                ret.add(mPhotos.get(checkArray.keyAt(i)));
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "getSelectedPhotos: " + ret.size());
        return ret;
    }

    protected void initGridView() {
        mAdapter = new EndlessGridAdapter(mPhotos);
        mAdapter.setRunInBackground(false);
        mGridView = (GridView) mLayout.findViewById(R.id.gridview);
        mGridView.setAdapter(mAdapter);
        mGridView.setChoiceMode(getGridChoiceMode());

        mGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mGridView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
                    SparseBooleanArray checkArray = mGridView.getCheckedItemPositions();
                    if (checkArray != null) {
                        BusProvider.getInstance().post(new PhotoGridItemClickedEvent(
                                checkArray.get(position)));
                    }
                } else {
                    PhotoViewerActivity.startPhotoViewer(mActivity, mPhotos, position);
                }
                mGridView.invalidateViews();
            }
        });

        mGridView.setOnItemLongClickListener(
                new GridView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                    int position, long id) {
                /* Only available if logged in */
                if (mOAuth == null) {
                    return false;
                }
                if (mGridView.getChoiceMode() ==
                        ListView.CHOICE_MODE_MULTIPLE) {
                    return false;
                }
                if (position < mPhotos.size()) {
                    DialogFragment d =
                        PhotoItemLongClickDialog.newInstance(mActivity,
                            PhotoGridFragment.this, mPhotos.get(position));
                    d.show(mActivity.getSupportFragmentManager(),
                        "photo_item_long_click");
                    /* True indicates we're finished with event and triggers
                     * haptic feedback  */
                    return true;
                }
                return false;
            }
        });
        mGridView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLongClickDialogSelection(Photo photo, int which) {
        if (photo != null) {
            Intent profileViewer = new Intent(mActivity,
                    ProfileViewerActivity.class);
            profileViewer.putExtra(ProfileViewerActivity.KEY_PROFILE_ID,
                    photo.getOwner().getId());
            profileViewer.setAction(ProfileViewerActivity.ACTION_VIEW_USER_BY_ID);
            startActivity(profileViewer);
        } else {
            Log.e(getLogTag(), "showGridItemContextMenu: photo is null");
        }
    }

    /**
     * If we have a most recent id stored, see if it exists in the photo
     * list we just fetched. If so, all photos before that id in the list
     * are said to be new.
     */
    protected void checkForNewPhotos(List<Photo> photos) {
        if (photos == null || photos.isEmpty()) {
            if (BuildConfig.DEBUG)
                Log.d(getLogTag(), "checkForNewPhotos: photos null or empty");
            return;
        }

        mNewPhotos = new ArrayList<Photo>();
        String newestId = getNewestPhotoId();
        if (newestId != null) {
            for (int i=0; i < photos.size(); i++) {
                Photo p = photos.get(i);
                if (p.getId().equals(newestId)) {
                    mNewPhotos = photos.subList(0, i);
                    if (BuildConfig.DEBUG)
                        Log.d(getLogTag(), String.format("Found %d new photos",
                                mNewPhotos.size()));
                    break;
                }
            }
        }

        if (mNewPhotos != null && !mNewPhotos.isEmpty()) {
            storeNewestPhotoId(mNewPhotos.get(0));
        } else {
            if (BuildConfig.DEBUG)
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
            super(new GridAdapter(list));
        }

        @Override
        protected boolean cacheInBackground() throws Exception {
            return PhotoGridFragment.this.cacheInBackground();
        }

        @Override
        protected void appendCachedData() {
        }

        @Override
        protected View getPendingView(ViewGroup parent) {
            return new View(mActivity);
        }
    }

    class GridAdapter extends ArrayAdapter<Photo> {

        private final boolean mHighQualityThumbnails;

        public GridAdapter(List<Photo> items) {
            super(mActivity, R.layout.gridview_item, android.R.id.text1,
                    items);
            mHighQualityThumbnails = mDefaultSharedPrefs.getBoolean(
                Constants.KEY_HIGH_QUALITY_THUMBNAILS, false);
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

            if (mShowDetailsOverlay) {
                holder.imageOverlay.setVisibility(View.VISIBLE);
            } else {
                holder.imageOverlay.setVisibility(View.INVISIBLE);
            }

            mTextUtils.setFont(holder.ownerText,
                    TextUtils.FONT_ROBOTOBOLD);

            /* Fetch the main photo */
            String thumbnailUrl = photo.getLargeSquareUrl();
            if (mHighQualityThumbnails) {
                thumbnailUrl = photo.getMediumUrl();
            }

            Picasso.with(mActivity).load(thumbnailUrl).into(holder.image);

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
                        Intent profileViewer = new Intent(mActivity,
                                ProfileViewerActivity.class);
                        profileViewer.putExtra(
                                ProfileViewerActivity.KEY_PROFILE_ID,
                                photo.getOwner().getId());
                        profileViewer.setAction(ProfileViewerActivity.ACTION_VIEW_USER_BY_ID);
                        startActivity(profileViewer);
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

            /* If in multiple choice mode, set tint on selected items */
            if (mGridView.getChoiceMode() ==
                    ListView.CHOICE_MODE_MULTIPLE) {
                SparseBooleanArray checkArray =
                    mGridView.getCheckedItemPositions();

                if (checkArray != null) {
                    if (checkArray.get(position)) {
                        int highlightColor = mActivity.getResources()
                            .getColor(R.color.transparent_flickr_pink);
                        holder.image.setColorFilter(highlightColor);
                    } else {
                        holder.image.setColorFilter(null);
                    }
                }
            }

            return convertView;
        }
    }

    public static class ViewHolder {
        public LinearLayout imageOverlay;
        public ImageView image;
        public ImageView imageNewRibbon;
        public TextView ownerText;
        public TextView viewsText;
    }

    static class PhotoItemLongClickDialog extends DialogFragment {
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

    /**
     * Event published when an item in the grid is clicked.
     */
    public static class PhotoGridItemClickedEvent {
        public final boolean mIsChecked;

        public PhotoGridItemClickedEvent(boolean isChecked) {
            mIsChecked = isChecked;
        }

        public boolean isChecked() {
            return mIsChecked;
        }
    }
}
