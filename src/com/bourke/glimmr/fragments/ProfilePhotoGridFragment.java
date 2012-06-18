package com.bourke.glimmr;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;

/**
 * Subclass of PhotoGridFragment to show a GridView of photos along with
 * a banner to differentiate what profile the photos belong to.
 */
public class ProfilePhotoGridFragment extends PhotoGridFragment
        implements IPhotoGridReadyListener {

    private static final String TAG = "Glimmr/ProfilePhotoGridFragment";

	private AQuery mGridAq;
    private User mUser;

    public static ProfilePhotoGridFragment newInstance(int type, User user) {
        ProfilePhotoGridFragment newFragment = new ProfilePhotoGridFragment();
        newFragment.mType = type;
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.profile_gridview_fragment, container, false);
        super.initOAuth();
        return mLayout;
    }

    /**
     * Once we're authorised to access the user's account, start a task to
     * fetch the appropriate photos.
     */
    @Override
    public void onAuthorised(OAuth oauth) {
        switch (mType) {
            case TYPE_PHOTO_STREAM:
                //new LoadPhotostreamTask(this).execute(oauth);
                break;
            case TYPE_FAVORITES_STREAM:
                //new LoadContactsPhotosTask(this).execute(oauth);
                break;
            default:
                Log.e(TAG, "Unknown ProfilePhotoGridFragment type: " + mType);
        }
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
                aq.id(R.id.viewsText).text("Views: " + String.valueOf(photo
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
