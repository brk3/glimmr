package com.bourke.glimmr;

import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;

public class PhotoStreamFragment extends BaseFragment
        implements IPhotoStreamReadyListener {

    private static final String TAG = "Glimmr/PhotoStreamFragment";

	private AQuery mGridAq;

    /**
     * Once we're authorised to access the user's account, start a task to
     * fetch their photostream.
     */
    @Override
    public void onAuthorised(OAuth oauth) {
        new LoadPhotostreamTask(this).execute(oauth);
    }

    /**
     * Once LoadPhotostreamTask comes back with the user's list of photos, set
     * up the GridView adapter etc.
     */
    @Override
    public void onPhotoStreamReady(PhotoList photos, boolean cancelled) {
		mGridAq = new AQuery(mActivity);
        mPhotos = photos;

		ArrayAdapter<Photo> adapter = new ArrayAdapter<Photo>(mActivity,
                R.layout.gridview_item, photos) {

            // TODO: implement ViewHolder pattern
			@Override
			public View getView(int position, View convertView,
                    ViewGroup parent) {

				if(convertView == null) {
					convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.gridview_item, null);
				}

                Photo photo = getItem(position);
				AQuery aq = mGridAq.recycle(convertView);

                boolean useMemCache = true;
                boolean useFileCache = true;
                aq.id(R.id.image_item).image(photo.getLargeSquareUrl(),
                        useMemCache, useFileCache,  0, 0, null,
                        AQuery.FADE_IN_NETWORK);
                aq.id(R.id.viewsText).text("views:" + String.valueOf(photo
                            .getViews()));
                aq.id(R.id.ownerText).text(photo.getOwner().getUsername());

				return convertView;
			}
		};
        mGridAq.id(R.id.gridview).adapter(adapter).itemClicked(this,
                "startPhotoViewer");
		mGridAq.id(R.id.gridview).adapter(adapter);
    }
}
