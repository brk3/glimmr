package com.bourke.glimmr;

import android.graphics.Bitmap;

/**
 * Represents the listener which will be notified after an image was downloaded
 * from the network.
 *
 * @author(original) charles
 *
 */
public interface IImageDownloadDoneListener {

	/**
	 * @param bitmap the bitmap downloaded.
	 */
	void onImageDownloaded(Bitmap bitmap);
}
