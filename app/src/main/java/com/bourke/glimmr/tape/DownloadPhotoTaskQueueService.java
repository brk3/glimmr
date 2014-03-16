package com.bourke.glimmr.tape;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.TaskQueueDelegateFactory;
import com.bourke.glimmr.tasks.DownloadPhotoTask;
import com.squareup.tape.TaskQueue;

public class DownloadPhotoTaskQueueService extends AbstractTaskQueueService {

    public static boolean IS_RUNNING;

    private boolean mDidWork = false;

    @Override
    public void onCreate() {
        super.onCreate();
        IS_RUNNING = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IS_RUNNING = false;
        /* if we had work to do, we need to update the pending notification with either success or
         failed status*/
        if (mDidWork) {
            clearStartedNotification();
        }
    }

    @Override
    protected void initTaskQueue() {
        TaskQueueDelegateFactory<DownloadPhotoTask> factory =
                new TaskQueueDelegateFactory<DownloadPhotoTask>(this);
        mQueue = new TaskQueue(factory.get(Constants.DOWNLOAD_IMAGE_QUEUE,
                DownloadPhotoTask.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mQueue.size() > 0) {
            showStartedNotification();
            mDidWork = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showStartedNotification() {
        String title = getString(R.string.downloading_photo);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setProgress(0, 0, true)
                .setTicker(title)
                .setSmallIcon(R.drawable.av_download_dark)
                .setContentTitle(title)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_PHOTOS_UPLOADING, notification.build());
    }

    private void clearStartedNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setProgress(0, 0, false)
                .setSmallIcon(R.drawable.av_download_dark)
                .setAutoCancel(true);

        /* if we're stopping because max retries exceeded */
        if (mNumRetries >= MAX_RETRIES) {
            final Intent intent = new Intent(this, ((Object)this).getClass());
            PendingIntent peManualRetry = PendingIntent.getService(this, 0, intent, 0);
            notification
                    .setTicker(getString(R.string.download_error))
                    .setContentTitle(getString(R.string.download_error))
                    .addAction(R.drawable.ic_action_refresh_dark, getString(R.string.retry),
                            peManualRetry)
                    .setContentText(getString(R.string.ill_try_again_later));
        } else {
            notification
                    .setTicker(getString(R.string.image_saved))
                    .setContentTitle(getString(R.string.image_saved))
                    .setContentText("TODO: set big picture here and gallery intent")
                    .setDefaults(Notification.DEFAULT_ALL);
        }

        // TODO: set big picture here and gallery intent

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_PHOTOS_UPLOADING,
                notification.build());
    }
}

