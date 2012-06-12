package com.bourke.glimmr;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;

import android.util.Log;

import android.widget.ImageView;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.groups.Group;
import com.gmail.yuyang226.flickr.groups.GroupsInterface;
import com.gmail.yuyang226.flickr.photosets.Photoset;
import com.gmail.yuyang226.flickr.photosets.PhotosetsInterface;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotosInterface;

import java.lang.ref.WeakReference;

/**
 * Represents the image download task which takes an image url as the
 * parameter, after the download, set the bitmap to an associated
 * <code>ImageView</code>.
 *
 * @author(original) charles
 */
public class ImageDownloadTask extends AsyncTask<String, Integer, Bitmap> {

    private static final String TAG = "Glimmr/ImageDownloadTask";

	private WeakReference<ImageView> imgRef = null;
	private String mUrl;

	public static enum ParamType {
		PHOTO_URL, PHOTO_ID_SMALL, PHOTO_ID_SMALL_SQUARE, PHOTO_ID_MEDIUM,
        PHOTO_ID_LARGE, PHOTO_SET_ID, PHOTO_POOL_ID
	}

	private String mPhotoSecret = null;

	private ParamType mParamType = ParamType.PHOTO_URL;

	private IImageDownloadDoneListener mImageDownloadedListener;

	public ImageDownloadTask(ImageView imageView) {
		this(imageView, ParamType.PHOTO_URL, null);
	}

	public ImageDownloadTask(ImageView imageView, ParamType paramType) {
		this(imageView, paramType, null);
	}

	public ImageDownloadTask(ImageView imageView, ParamType paramType,
			IImageDownloadDoneListener listener) {
		this.imgRef = new WeakReference<ImageView>(imageView);
		this.mParamType = paramType;
		this.mImageDownloadedListener = listener;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		mUrl = params[0];
		if (params.length > 1) {
			mPhotoSecret = params[1];
		}
		String url = mUrl;
		Flickr f = FlickrHelper.getInstance().getFlickr();
		if (ParamType.PHOTO_SET_ID.equals(mParamType)) {
			String photoSetId = mUrl;
			PhotosetsInterface psi = f.getPhotosetsInterface();
			try {
				Photoset ps = psi.getInfo(photoSetId);
				url = ps.getPrimaryPhoto().getSmallSquareUrl();
			} catch (Exception e) {
				return null;
			}
		} else if (ParamType.PHOTO_POOL_ID.equals(mParamType)) {
			String photoPoolId = mUrl;
			GroupsInterface gi = f.getGroupsInterface();
			try {
				Group photoGroup = gi.getInfo(photoPoolId);
				url = photoGroup.getBuddyIconUrl();
			} catch (Exception e) {
				return null;
			}
		} else if (!mParamType.equals(ParamType.PHOTO_URL)) {
			PhotosInterface pi = f.getPhotosInterface();
			try {
				Photo photo = pi.getPhoto(mUrl, mPhotoSecret);
				switch (mParamType) {
				case PHOTO_ID_SMALL_SQUARE:
					url = photo.getSmallSquareUrl();
					break;
				case PHOTO_ID_LARGE:
					url = photo.getLargeUrl();
					break;
				case PHOTO_ID_SMALL:
					url = photo.getSmallUrl();
					break;
				case PHOTO_ID_MEDIUM:
					url = photo.getMediumUrl();
				default:
					break;
				}

			} catch (Exception e) {
                e.printStackTrace();
				Log.e(TAG, "Unable to get the photo detail information", e);
				return null;
			}
		}
		if (url == null) {
			return null;
		}
		return ImageUtils.downloadImage(url);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (this.isCancelled()) {
			result = null;
			return;
		}

		ImageCache.saveToCache(mUrl, result);
		if (imgRef != null) {
			ImageView imageView = imgRef.get();
			ImageDownloadTask bitmapDownloaderTask = getBitmapDownloaderTask(
                    imageView);
			// Change bitmap only if this process is still associated with it
			// Or if we don't use any bitmap to task association
			// (NO_DOWNLOADED_DRAWABLE mode)
			if (this == bitmapDownloaderTask &&
                    bitmapDownloaderTask != null ) {
				imageView.setImageBitmap(result);
			}
		}

		if (mImageDownloadedListener != null) {
			mImageDownloadedListener.onImageDownloaded(result);
		}
	}

	/**
	 * This method name should be changed later, for sometimes, it will return
	 * photo id.
	 *
	 * @return
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active download task (if any) associated
	 *         with this imageView. null if there is no such task.
	 */
	private ImageDownloadTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof ImageUtils.DownloadedDrawable) {
				ImageUtils.DownloadedDrawable downloadedDrawable =
                    (ImageUtils.DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}
}
