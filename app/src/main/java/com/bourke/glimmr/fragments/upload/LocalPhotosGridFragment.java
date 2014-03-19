package com.bourke.glimmr.fragments.upload;

import com.bourke.glimmr.BuildConfig;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.PhotoUploadActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.UsageTips;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.googlecode.flickrjandroid.photos.GeoData;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photosets.Photosets;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;

import java.util.ArrayList;

/**
 * A PhotoGridFragment that displays photos found on the device.  Selected photos are then passed to
 * PhotoUploadActivity where info for each can be edited etc. before upload.
 *
 * Based on code from:
 * http://android-er.blogspot.ie/2012/11/list-mediastoreimagesthumbnails-in.html
 */
public class LocalPhotosGridFragment extends PhotoGridFragment
        implements AbsListView.MultiChoiceModeListener{

    private static final String TAG = "Glimmr/LocalPhotosGridFragment";

    private static final Uri SOURCE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String SOURCE_TITLE = MediaStore.Images.Media.TITLE;
    private static final String SOURCE_DATA = MediaStore.Images.Media.DATA;

    private static final Uri THUMB_URI = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    private static final String THUMB_DATA = MediaStore.Images.Thumbnails.DATA;
    private static final String THUMB_IMAGE_ID = MediaStore.Images.Thumbnails.IMAGE_ID;

    private MediaStoreImagesAdapter mAdapter;

    @Override
    protected void initGridView() {
        mGridView = (GridView) mLayout.findViewById(R.id.gridview);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setMultiChoiceModeListener(this);
        mShowDetailsOverlay = false;
        String[] from = {MediaStore.MediaColumns.TITLE};
        int[] to = {android.R.id.text1};

        CursorLoader cursorLoader = new CursorLoader(
                getActivity(),
                SOURCE_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.TITLE);

        Cursor cursor = cursorLoader.loadInBackground();

        mAdapter = new MediaStoreImagesAdapter(
                getActivity(),
                R.layout.gridview_item,
                cursor,
                from,
                to);

        mGridView.setAdapter(mAdapter);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            final String usageTip = getString(R.string.upload_photos_tip);
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                UsageTips.getInstance().show(mActivity, usageTip, true);
            }
        });
    }

    /**
     * When items are selected/de-selected.
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mGridView.invalidateViews();
    }

    /**
     * Respond to clicks on the actions in the CAB
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.menu_delete:
//                deleteSelectedItems();
//                mode.finish(); // Action picked, so close the CAB
//                return true;
            default:
                return false;
        }
    }

    /**
     * Inflate the menu for the CAB
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
//        inflater.inflate(R.menu.context, menu);
        return true;
    }

    /**
     * Called when the CAB is removed.
     *
     * Get URIs for selected photos and start PhotoUploadActivity to process
     * them.
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        final ArrayList<LocalPhoto> selectedImages = new ArrayList<LocalPhoto>();
        final SparseBooleanArray checkArray = mGridView.getCheckedItemPositions();
        Cursor cursor = mAdapter.getCursor();

        for (int i=0; i < mGridView.getCount(); i++) {
            if (checkArray.get(i)) {
                LocalPhoto photo = new LocalPhoto();
                UploadMetaData metadata = new UploadMetaData();

                cursor.moveToPosition(i);

                photo.setUri(cursor.getString(cursor.getColumnIndex(SOURCE_DATA)));
                metadata.setTitle(cursor.getString(cursor.getColumnIndex(SOURCE_TITLE)));
                photo.setMetadata(metadata);

                selectedImages.add(photo);
            }
            if (selectedImages.size() == checkArray.size()) {
                break;
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getSelectedPhotos: " + selectedImages.size());
        }
        if (selectedImages.size() > 0) {
            PhotoUploadActivity.startPhotoUploadActivity(mActivity, selectedImages);
        }
    }

    /**
     * Perform updates to the CAB due to an invalidate() request
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /* Override so as not to display the usual PhotoGridFragment options */
    }

    @Override
    protected String getNewestPhotoId() {
        return null;
    }

    @Override
    protected void storeNewestPhotoId(Photo photo) {
    }

    public class LocalPhoto {
        private String mUri;
        private UploadMetaData mMetadata;
        private Photosets mPhotosets;
        private GeoData mGeoData;

        public GeoData getGeoData() {
            return mGeoData;
        }

        public void setGeoData(GeoData geoData) {
            mGeoData = geoData;
        }

        public String getUri() {
            return mUri;
        }

        public void setUri(String uri) {
            mUri = uri;
        }

        public UploadMetaData getMetadata() {
            return mMetadata;
        }

        public void setMetadata(UploadMetaData metadata) {
            mMetadata = metadata;
        }

        public Photosets getPhotosets() {
            return mPhotosets;
        }

        public void setPhotosets(Photosets photosets) {
            mPhotosets = photosets;
        }
    }

    public class MediaStoreImagesAdapter extends SimpleCursorAdapter {

        private final Cursor mCursor;

        public MediaStoreImagesAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mCursor = c;
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.gridview_item, null);
                holder = new ViewHolder();
                holder.imageOverlay = (LinearLayout) convertView.findViewById(R.id.imageOverlay);
                holder.image = (ImageView) convertView.findViewById(R.id.image_item);
                holder.imageNewRibbon = (ImageView) convertView.findViewById(R.id.imageNewRibbon);
                holder.ownerText = (TextView) convertView.findViewById(R.id.ownerText);
                holder.viewsText = (TextView) convertView.findViewById(R.id.viewsText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.imageOverlay.setVisibility(View.INVISIBLE);

            mCursor.moveToPosition(position);

            int myID = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID));

            String[] thumbColumns = {THUMB_DATA, THUMB_IMAGE_ID};
            CursorLoader thumbCursorLoader = new CursorLoader(
                    getActivity(),
                    THUMB_URI,
                    thumbColumns,
                    THUMB_IMAGE_ID + "=" + myID,
                    null,
                    null);
            Cursor thumbCursor = thumbCursorLoader.loadInBackground();

            Bitmap myBitmap = null;
            if(thumbCursor.moveToFirst()){
                int thCulumnIndex = thumbCursor.getColumnIndex(THUMB_DATA);
                String thumbPath = thumbCursor.getString(thCulumnIndex);
                myBitmap = BitmapFactory.decodeFile(thumbPath);
                holder.image.setImageBitmap(myBitmap);
            }

            /* Set tint on selected items */
            SparseBooleanArray checkArray = mGridView.getCheckedItemPositions();
            if (checkArray.get(position)) {
                int highlightColor = mActivity.getResources().getColor(
                        R.color.transparent_flickr_pink);
                holder.image.setColorFilter(highlightColor);
            } else {
                holder.image.setColorFilter(null);
            }

            return convertView;
        }
    }
}
