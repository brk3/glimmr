package com.bourke.glimmr;

import android.app.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import com.gmail.yuyang226.flickr.photos.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public final class PhotoFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/PhotoFragment";

    private Activity mActivity;

    private Photo mPhoto;

    private ImageView mImageView;

    public PhotoFragment(Photo photo) {
        mPhoto = photo;
        mActivity = getSherlockActivity();
    }

    public static PhotoFragment newInstance(Photo photo) {
        return new PhotoFragment(photo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
        mImageView = (ImageView) layout.findViewById(R.id.image_item);
        return layout;
    }

	@Override
	public void onStart() {
		super.onStart();
        /*
		if (mPhoto != null) {
            String photoId = mPhoto.getId();
			File cacheDir = mActivity.getCacheDir();
			File imageFile = new File(cacheDir, photoId + ".jpg");
			if (imageFile.exists()) {
				try {
					Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(
							imageFile));
					onImageDownloaded(bm);
				} catch (FileNotFoundException e) {
                    e.printStackTrace();
				}
			} else {
				ImageDownloadTask task = new ImageDownloadTask(mImageView,
						ImageDownloadTask.ParamType.PHOTO_ID_LARGE, this);
				task.execute(photoId, mPhotoSecret);
			}
		} else {
			mProgressBar.setVisibility(View.GONE);
			Toast.makeText(this, getString(R.string.error_get_big_image),
					Toast.LENGTH_SHORT).show();
		}
        */
    }

	public void onImageDownloaded(Bitmap bitmap) {
        /*
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
		}
		if (bitmap == null) {
			Toast.makeText(this, getString(R.string.error_download_big_photo),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		this.mPhotoBitmap = bitmap;
		if (mImageView != null) {
			mImageView.setImageBitmap(mPhotoBitmap);
			fitToScreen();
			center(true, true);
		}
        */
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
