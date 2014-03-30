package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IExifInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.tasks.LoadExifInfoTask;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public final class ExifInfoFragment extends BaseFragment
        implements IExifInfoReadyListener {

    private static final String TAG = "Glimmr/ExifInfoFragment";

    private Photo mPhoto = new Photo();
    private LoadExifInfoTask mTask;
    private TextView mTextViewErrorMessage;

    /* http://www.flickr.com/services/api/flickr.photos.getExif.html */
    private static final String ERR_PERMISSION_DENIED = "2";

    public static ExifInfoFragment newInstance(Photo photo) {
        ExifInfoFragment photoFragment = new ExifInfoFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    /** Can't retain fragments that are nested in other fragments */
    @Override
    protected boolean shouldRetainInstance() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(
                R.layout.exif_info_fragment, container, false);
        mTextViewErrorMessage =
            (TextView) mLayout.findViewById(R.id.textViewErrorMessage);

        setHasOptionsMenu(false);

        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "startTask()");
        mTask = new LoadExifInfoTask(this, mPhoto);
        mTask.execute(mOAuth);
    }

    /**
     * Creates a TableRow with two columns containing TextViews, and adds it to
     * the main TableView.
     */
    @SuppressWarnings("deprecation")
    private void addKeyValueRow(String key, String value) {
        TableLayout tl = (TableLayout)mLayout.findViewById(R.id.extraExifInfo);

        /* Create the TableRow */
        TableRow tr = new TableRow(mActivity);

        /* Create the left column for the key */
        TextView textViewKey =  new TextView(mActivity);
        textViewKey.setText(key);
        TableRow.LayoutParams leftColParams = new TableRow.LayoutParams(
                0, TableLayout.LayoutParams.WRAP_CONTENT, 1f);
        textViewKey.setLayoutParams(leftColParams);
        tr.addView(textViewKey);

        /* Create the right column for the value */
        TextView textViewValue =  new TextView(mActivity);
        textViewValue.setText(value);
        textViewValue.setTextColor(
                mActivity.getResources().getColor(R.color.flickr_pink));
        textViewValue.setMaxLines(1);
        textViewValue.setEllipsize(TextUtils.TruncateAt.END);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                0, TableLayout.LayoutParams.WRAP_CONTENT, 1f);
        /* left, top, right, bottom */
        lp.setMargins(8, 0, 0, 0);
        textViewValue.setLayoutParams(lp);
        tr.addView(textViewValue);

        /* Add the row to the table */
        tl.addView(tr);
    }

    public void onExifInfoReady(List<Exif> exifInfo, Exception exc) {
        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, exc)) {
            return;
        }

        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, exc)) {
            return;
        }

        /* Something went wrong, show message and return */
        if (exc != null) {
            mTextViewErrorMessage.setVisibility(View.VISIBLE);
            if (exc instanceof FlickrException) {
                String errCode = ((FlickrException) exc).getErrorCode();
                if (BuildConfig.DEBUG) Log.d(getLogTag(), "errCode: " + errCode);
                if (errCode != null && ERR_PERMISSION_DENIED.equals(errCode)) {
                    mTextViewErrorMessage.setText(
                            mActivity.getString(R.string.no_exif_permission));
                } else {
                    mTextViewErrorMessage.setText(
                            mActivity.getString(R.string.no_connection));
                }
            } else {
                mTextViewErrorMessage.setText(
                        mActivity.getString(R.string.no_connection));
            }
            return;
        }

        /* Populate table with exif info */
        for (Exif e : exifInfo) {
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

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
