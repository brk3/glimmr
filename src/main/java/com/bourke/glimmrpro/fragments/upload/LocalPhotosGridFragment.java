package com.bourke.glimmrpro.fragments.upload;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.androidquery.AQuery;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.activities.PhotoUploadActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.UsageTips;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.googlecode.flickrjandroid.photos.GeoData;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photosets.Photosets;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;

import java.util.ArrayList;

/**
 * A PhotoGridFragment that displays photos found on the device.  Selected photos are then passed to
 * PhotoUploadActivity where info for each can be edited etc. before upload.
 *
 * Uses CursorLoaders to iterate the MediaStore and construct a MatrixCursor containing a thumbnail
 * path and a path to the full sized image.
 *
 * There should be more efficient ways of doing this, i.e. I think the adapter should be able to
 * load data as it's needed rather than having to grab it all first.
 *
 * Based on code from:
 * http://android-er.blogspot.ie/2012/10/list-images-with-thumbnails.html
 */
public class LocalPhotosGridFragment extends PhotoGridFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener {

    private static final String TAG = "Glimmr/LocalPhotosGridFragment";

    private final int THUMBNAIL_LOADER_ID  = 0;
    private final int IMAGE_LOADER_ID  = 1;

    private final int COL_THUMB_DATA = 1;
    private final int COL_IMAGE_DATA = 2;
    private final int COL_IMAGE_TITLE = 3;

    private MatrixCursor mMatrixCursor;
    private Cursor mThumbCursor;
    private Cursor mImageCursor;

    private String mThumbImageId;
    private String mThumbImageData;

    private MediaStoreImagesAdapter mAdapter;

    @Override
    protected void initGridView() {
        mGridView = (GridView) mLayout.findViewById(R.id.gridview);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setMultiChoiceModeListener(this);
        mShowDetailsOverlay = false;
        mAdapter = new MediaStoreImagesAdapter(getActivity(), R.layout.gridview_item, null,
                new String[]{}, new int[]{ R.id.image });
        mGridView.setAdapter(mAdapter);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UsageTips.getInstance().show(mActivity,
                        getString(R.string.upload_photos_tip), false);
            }
        });
        mMatrixCursor = new MatrixCursor(new String[]{ "_id", "thumb_data", "image_data",
                "image_title" });
        mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        getActivity().getLoaderManager().initLoader(THUMBNAIL_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loader_id, Bundle arg1) {
        CursorLoader cLoader;
        Uri uri;

        if (loader_id == THUMBNAIL_LOADER_ID) {
            uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
            cLoader = new CursorLoader(mActivity, uri, null, null, null, null);
        } else {
            /** Query Image Content provider with thumbnail image id */
            String image_id = arg1.getString("image_id");
            StringBuilder query = new StringBuilder().append(MediaStore.Images.Media._ID)
                    .append("=").append(image_id);
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            cLoader = new CursorLoader(mActivity, uri, null, query.toString(), null, null);
        }
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == THUMBNAIL_LOADER_ID) {
            mThumbCursor = cursor;

            /** If we're on the first thumbnail */
            if (mThumbCursor.moveToFirst()) {
                mThumbImageId = mThumbCursor.getString(mThumbCursor.getColumnIndex(
                        MediaStore.Images.Thumbnails._ID));
                mThumbImageData = mThumbCursor.getString(mThumbCursor.getColumnIndex(
                                MediaStore.Images.Thumbnails.DATA));

                /** Pass the thumb image id to the image data loader */
                String image_id = mThumbCursor.getString(mThumbCursor.getColumnIndex(
                                MediaStore.Images.Thumbnails.IMAGE_ID));
                Bundle data = new Bundle();
                data.putString("image_id", image_id);

                mActivity.getLoaderManager().initLoader(IMAGE_LOADER_ID, data, this);
            }
        } else if (cursorLoader.getId() == IMAGE_LOADER_ID) {
            mImageCursor = cursor;

            if (mImageCursor.moveToFirst()) {
                final String imageDataUri = mImageCursor.getString(mImageCursor.getColumnIndex(
                        MediaStore.Images.ImageColumns.DATA));
                final String imageTitle = mImageCursor.getString(mImageCursor.getColumnIndex(
                        MediaStore.Images.ImageColumns.TITLE));

                /** Add new row to the matrixcursor object */
                mMatrixCursor.addRow(new Object[]{mThumbImageId, mThumbImageData, imageDataUri,
                        imageTitle});

                /** Take the next thumbnail */
                if (mThumbCursor.moveToNext()) {
                    mThumbImageId = mThumbCursor.getString(
                            mThumbCursor.getColumnIndex(MediaStore.Images.Thumbnails._ID));
                    mThumbImageData = mThumbCursor.getString(
                            mThumbCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));

                    String image_id = mThumbCursor.getString(mThumbCursor.getColumnIndex(
                            MediaStore.Images.Thumbnails.IMAGE_ID));
                    Bundle data = new Bundle();
                    data.putString("image_id", image_id);

                    /** Restart the image loader to get the next image details */
                    mActivity.getLoaderManager().restartLoader(IMAGE_LOADER_ID, data, this);
                } else {
                    /** Done - update adapter */
                    if(mThumbCursor.isAfterLast()) {
                        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        mAdapter.swapCursor(mMatrixCursor);
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    /**
     * When items are selected/de-selected.
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
            long id, boolean checked) {
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
        for (int i=0; i < mGridView.getCount(); i++) {
            if (checkArray.get(i)) {
                LocalPhoto photo = new LocalPhoto();
                UploadMetaData metadata = new UploadMetaData();

                mMatrixCursor.moveToPosition(i);
                photo.setUri(mMatrixCursor.getString(COL_IMAGE_DATA));
                metadata.setTitle(mMatrixCursor.getString(COL_IMAGE_TITLE));
                photo.setMetadata(metadata);

                selectedImages.add(photo);
            }
            if (selectedImages.size() == checkArray.size()) {
                break;
            }
        }
        if (Constants.DEBUG) {
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

        public MediaStoreImagesAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to, 0);
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

            getCursor().moveToPosition(position);
            String thumbPath = getCursor().getString(COL_THUMB_DATA);
            mAq.id(holder.image).image(thumbPath,
                    Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                    0, 0, null, AQuery.FADE_IN_NETWORK);

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
