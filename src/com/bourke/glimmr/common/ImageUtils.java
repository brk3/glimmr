package com.bourke.glimmr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;

import android.net.http.AndroidHttpClient;

import android.util.Log;

import java.io.InputStream;
import java.io.IOException;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

public final class ImageUtils {

    private static final String TAG = "Glimmr/ImageUtils";

	private static Map<String, SoftReference<Bitmap>> imageCache =
        new ConcurrentHashMap<String, SoftReference<Bitmap>>(20);

	/**
	 * This method must be called in a thread other than UI.
	 *
	 * @param url
	 * @return
	 */
	public static Bitmap downloadImage(String url) {
		// final int IO_BUFFER_SIZE = 4 * 1024;

		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					// return BitmapFactory.decodeStream(inputStream);
					// Bug on slow connections, fixed in future release.
					return BitmapFactory.decodeStream(new FlushedInputStream(
							inputStream));
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			Log.d(TAG, "I/O error while retrieving bitmap from " + url);
            e.printStackTrace();
			getRequest.abort();
		} catch (IllegalStateException e) {
			Log.d(TAG, "Incorrect URL:" + url);
            e.printStackTrace();
			getRequest.abort();
		} catch (Exception e) {
			Log.d(TAG, "Error while retrieving bitmap from " + url);
            e.printStackTrace();
			getRequest.abort();
		} finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}
		return null;
	}

	public static class DownloadedDrawable extends ColorDrawable {

		private WeakReference<ImageDownloadTask> taskRef;

		public DownloadedDrawable(ImageDownloadTask task) {
			taskRef = new WeakReference<ImageDownloadTask>(task);
		}

		public ImageDownloadTask getBitmapDownloaderTask() {
			if (taskRef != null) {
				return taskRef.get();
			} else {
				return null;
			}
		}
	}

	public static void putToCache(String url, Bitmap bitmap) {
		imageCache.put(url, new SoftReference<Bitmap>(bitmap));
	}

	public static Bitmap getFromCache(String url) {
		if (imageCache.containsKey(url)) {
			return imageCache.get(url).get();
		} else {
			return null;
		}
	}

}
