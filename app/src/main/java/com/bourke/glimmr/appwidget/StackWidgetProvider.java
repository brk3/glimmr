package com.bourke.glimmr.appwidget;

import com.bourke.glimmr.BuildConfig;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.common.Constants;

public class StackWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "Glimmr/StackWidgetProvider";

    private static final String ACTION_START_VIEWER =
        "com.bourke.glimmr.appwidget.StackWidgetProvider.ACTION_START_VIEWER";
    private static final String ACTION_REFRESH =
        "com.bourke.glimmr.appwidget.StackWidgetProvider.ACTION_REFRESH";
    public static final String VIEW_INDEX =
        "com.bourke.glimmr.appwidget.StackWidgetProvider.VIEW_INDEX";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        if (intent.getAction().equals(ACTION_START_VIEWER)) {
            //int appWidgetId = intent.getIntExtra(
            //        AppWidgetManager.EXTRA_APPWIDGET_ID,
            //        AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(VIEW_INDEX, 0);
            String photoListFile = intent.getStringExtra(
                    PhotoViewerActivity.KEY_PHOTO_LIST_FILE);
            Intent photoViewer = new Intent(context,
                    PhotoViewerActivity.class);
            photoViewer.putExtra(
                    PhotoViewerActivity.KEY_START_INDEX,
                    viewIndex);
            photoViewer.setAction(PhotoViewerActivity.ACTION_VIEW_PHOTOLIST);
            photoViewer.putExtra(PhotoViewerActivity.KEY_PHOTO_LIST_FILE,
                    photoListFile);
            photoViewer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            photoViewer.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(photoViewer);
        } else if (intent.getAction().equals(ACTION_REFRESH)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "got action_refresh");
            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stack_view);
        }
        super.onReceive(context, intent);
    }

    /**
     * Called when first added or as requested by updatePeriodMillis
     *
     * appWidgetIds refers to each instance of the widget added to the
     * homescreen
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onUpdate: " + appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            /* Intent for creating the collection's views */
            Intent intent = new Intent(context, StackWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.stackview_widget_layout);
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            rv.setRemoteAdapter(R.id.stack_view, intent);

            /* Intent for clicking on an item */
            Intent photoViewerIntent = new Intent(context,
                    StackWidgetProvider.class);
            photoViewerIntent.setAction(
                    StackWidgetProvider.ACTION_START_VIEWER);
            photoViewerIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, photoViewerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stack_view, pendingIntent);

            /* Intent for clicking on the empty view to refresh */
            Intent refreshIntent = new Intent(context,
                    StackWidgetProvider.class);
            refreshIntent.setAction(ACTION_REFRESH);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetId);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
                    context, 0, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.empty_view, pendingIntent2);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
