package com.bourke.glimmr.tape;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.MainActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.TaskQueueDelegateFactory;
import com.bourke.glimmr.tasks.UploadPhotoTask;
import com.squareup.tape.TaskQueue;

public class UploadPhotoTaskQueueService extends AbstractTaskQueueService {

    private static final String TAG = "Glimmr/UploadPhotoTaskQueueService";

    public static boolean IS_RUNNING;

    private boolean mDidWork = false;

    @Override
    public void onCreate() {
        super.onCreate();
        IS_RUNNING = true;
    }

    @Override
    protected void initTaskQueue() {
        TaskQueueDelegateFactory<UploadPhotoTask> factory =
                new TaskQueueDelegateFactory<UploadPhotoTask>(this);
        mQueue = new TaskQueue(factory.get(Constants.UPLOAD_QUEUE, UploadPhotoTask.class));
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
        String title = getString(R.string.uploading_photos, mQueue.size());
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setProgress(0, 0, true)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_action_upload_dark)
                .setContentTitle(title)
                .setAutoCancel(true);

        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.KEY_PAGER_START_INDEX, 1);  // open on "Photos" page
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pe = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setContentIntent(pe);
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Constants.NOTIFICATION_PHOTOS_UPLOADING, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IS_RUNNING = false;
        /* if we had work to do, we need to update the pending notification */
        if (mDidWork) {
            clearStartedNotification();
        }
    }

    private void clearStartedNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setProgress(0, 0, false)
                .setSmallIcon(R.drawable.ic_action_upload_dark)
                .setAutoCancel(true);

        /* if we're stopping because max retries exceeded */
        if (mNumRetries >= MAX_RETRIES) {
            final Intent intent = new Intent(this, UploadPhotoTaskQueueService.class);
            PendingIntent peManualRetry = PendingIntent.getService(this, 0, intent, 0);
            notification
                    .setTicker(getString(R.string.upload_problem))
                    .setContentTitle(getString(R.string.upload_problem))
                    .addAction(R.drawable.ic_action_refresh_dark, getString(R.string.retry),
                            peManualRetry)
                    .setContentText(getString(R.string.ill_try_again_later));
        } else {
            notification
                    .setTicker(getString(R.string.photos_uploaded))
                    .setContentTitle(getString(R.string.photos_uploaded))
                    .setContentText(getString(R.string.tap_for_photostream))
                    .setDefaults(Notification.DEFAULT_ALL);
        }

        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.KEY_PAGER_START_INDEX, 1);  // open on "Photos" page
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pe = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setContentIntent(pe);
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Constants.NOTIFICATION_PHOTOS_UPLOADING,
                notification.build());
    }
}
