package com.bourke.glimmr.fragments.base;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.event.IPhotoListReadyListener;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import android.util.Log;

/**
 * Fragment that contains a GridView of photos.
 *
 * Can be used to display many of the Flickr "categories" of photos, i.e.
 * photostreams, favorites, contacts photos, etc.
 */
public abstract class PhotoGridFragment extends BaseFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoGridFragment";

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
        Log.d(TAG, "onPhotosReady");
        mGridAq = new AQuery(mActivity, mLayout);
        mPhotos = photos;

        ArrayAdapter<Photo> adapter = new ArrayAdapter<Photo>(mActivity,
                R.layout.gridview_item, photos) {

            // TODO: implement ViewHolder pattern
            // TODO: add aquery delay loading for fling scrolling
            @Override
            public View getView(final int position, View convertView,
                    ViewGroup parent) {

                if(convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.gridview_item, null);
                }

                final Photo photo = getItem(position);
                AQuery aq = mGridAq.recycle(convertView);

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
                if (photo.getOwner() != null) {
                    aq.id(R.id.ownerText).text(photo.getOwner().getUsername());
                    aq.id(R.id.ownerText).clicked(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startProfileViewer(photo.getOwner());
                        }
                    });
                }

                return convertView;
            }
        };
        mGridAq.id(R.id.gridview).adapter(adapter).itemClicked(this,
                "startPhotoViewer");
    }
}
