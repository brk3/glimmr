package com.bourke.glimmr;

import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.util.Log;

public class ContactsFragment extends BaseFragment
        implements IContactsPhotosReadyListener {

    protected String TAG = "Glimmr/ContactsFragment";

	private AQuery mGridAq;
    private RelativeLayout mLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout.gridview_fragment,
                container, false);
        super.initOAuth();
        return mLayout;
    }

    /**
     * Once we're authorised to access the user's account, start a task to
     * fetch N photos from each of their contacts.
     */
    @Override
    public void onAuthorised(OAuth oauth) {
        new LoadContactsPhotosTask(this).execute(oauth);
    }

    /**
     * Once LoadContactsPhotosTask comes back with the user's list of contacts
     * and photos from each, set up the GridView adapter etc.
     */
    @Override
    public void onContactsPhotosReady(PhotoList photos, boolean cancelled) {
        Log.d(TAG, "onContactsPhotosReady");
		mGridAq = new AQuery(mActivity, mLayout);
        mPhotos = photos;

		ArrayAdapter<Photo> adapter = new ArrayAdapter<Photo>(mActivity,
                R.layout.gridview_item, mPhotos) {

            // TODO: implement ViewHolder pattern
			@Override
			public View getView(int position, View convertView,
                    ViewGroup parent) {

				if (convertView == null) {
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
