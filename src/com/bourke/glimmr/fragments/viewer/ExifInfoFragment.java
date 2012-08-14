package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.androidquery.AQuery;

import com.bourke.glimmr.event.Events.IExifInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadExifInfoTask;

import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;
import android.view.Gravity;

public final class ExifInfoFragment extends BaseFragment
        implements IExifInfoReadyListener {

    protected String TAG = "Glimmr/ExifInfoFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;
    private LoadExifInfoTask mTask;

    public static ExifInfoFragment newInstance(Photo photo) {
        ExifInfoFragment photoFragment = new ExifInfoFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(
                R.layout.exif_info_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        mAq.id(R.id.progressIndicator).visible();
        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        Log.d(getLogTag(), "startTask()");
        mTask = new LoadExifInfoTask(mActivity, this, mPhoto);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            Log.d(TAG, "onPause: cancelling task");
        }
    }

    /**
     * Creates a TableRow with two columns containing TextViews, and adds it to
     * the main TableView.
     */
    private void addKeyValueRow(String key, String value) {
        TableLayout tl = (TableLayout)
            mLayout.findViewById(R.id.extraExifInfo);
        /* Create the TableRow */
        TableRow tr = new TableRow(mActivity);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
        /* left, top, right, bottom */
        tableRowParams.setMargins(5, 5, 5, 5);
        tr.setLayoutParams(tableRowParams);

        TextView textViewKey =  new TextView(mActivity);
        textViewKey.setText(key);
        tr.addView(textViewKey);

        TextView textViewValue =  new TextView(mActivity);
        textViewValue.setText(value);
        textViewValue.setTextColor(
                mActivity.getResources().getColor(R.color.flickr_pink));
        textViewValue.setGravity(Gravity.RIGHT);
        tr.addView(textViewValue);

        /* Add the row to the table */
        tl.addView(tr);
    }

    public void onExifInfoReady(List<Exif> exifInfo) {
        Log.d(getLogTag(), "onExifInfoReady, exifInfo.size(): "
                + exifInfo.size());
        mAq.id(R.id.progressIndicator).gone();
        for (Exif e : exifInfo) {
            if (e.getTag().equals("ISO")) {
                mAq.id(R.id.textViewISOValue).text(e.getRaw());
            } else if (e.getTag().equals("ExposureTime")) {
                mAq.id(R.id.textViewShutterValue).text(e.getRaw());
            } else if (e.getTag().equals("FNumber")) {
                mAq.id(R.id.textViewApertureValue).text(e.getRaw());
            } else {
               /* Convert camel case key to space delimited:
                * http://stackoverflow.com/a/2560017/663370 */
                String rawTag = e.getTag();
                String tagConverted = rawTag.replaceAll(
                    String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"), " "
                    );
                addKeyValueRow(tagConverted, e.getRaw());
            }
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
