package com.bourke.glimmr.common;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.widget.TextView;

public class TextUtils {

    private static final String TAG = "Glimmr/TextUtils";

    public static final int FONT_SHADOWSINTOLIGHT = 0;
    public static final int FONT_ROBOTOREGULAR = 1;
    public static final int FONT_ROBOTOTHIN = 2;
    public static final int FONT_ROBOTOLIGHT = 3;
    public static final int FONT_ROBOTOBOLD = 4;

    private static final String FONT_PATH_SHADOWSINTOLIGHT =
        "fonts/ShadowsIntoLight.ttf";
    private static final String FONT_PATH_ROBOTOREGULAR =
        "fonts/Roboto-Regular.ttf";
    private static final String FONT_PATH_ROBOTOTHIN =
        "fonts/Roboto-Thin.ttf";
    private static final String FONT_PATH_ROBOTOLIGHT =
        "fonts/Roboto-Light.ttf";
    private static final String FONT_PATH_ROBOTOBOLD =
        "fonts/Roboto-Bold.ttf";

    private final Typeface mFontShadowsIntoLight;
    private final Typeface mFontRobotoRegular;
    private final Typeface mFontRobotoThin;
    private final Typeface mFontRobotoLight;
    private final Typeface mFontRobotoBold;

    public TextUtils(AssetManager assets) {
        mFontShadowsIntoLight = Typeface.createFromAsset(
                assets, FONT_PATH_SHADOWSINTOLIGHT);
        mFontRobotoRegular = Typeface.createFromAsset(
                assets, FONT_PATH_ROBOTOREGULAR);
        mFontRobotoThin = Typeface.createFromAsset(
                assets, FONT_PATH_ROBOTOTHIN);
        mFontRobotoLight = Typeface.createFromAsset(
                assets, FONT_PATH_ROBOTOLIGHT);
        mFontRobotoBold = Typeface.createFromAsset(
                assets, FONT_PATH_ROBOTOBOLD);
    }

    public void setFont(TextView textView, final int font) {
        textView.setTypeface(getFontTypeface(font));
    }

    public void colorTextViewSpan(TextView view, String fulltext,
            String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        try {
            str.setSpan(new ForegroundColorSpan(color), i, i+subtext.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "fulltext: " + fulltext);
            Log.d(TAG, "subtext: " + subtext);
        }
    }

    public void fontTextViewSpan(TextView view, String fulltext,
            String subtext, int font) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        try {
            str.setSpan(new CustomTypefaceSpan(null, getFontTypeface(font)), i,
                    i+subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "fulltext: " + fulltext);
            Log.d(TAG, "subtext: " + subtext);
        }
    }

    private Typeface getFontTypeface(int font) {
        switch (font) {
            case FONT_SHADOWSINTOLIGHT:
                return mFontShadowsIntoLight;

            case FONT_ROBOTOREGULAR:
                return mFontRobotoRegular;

            case FONT_ROBOTOTHIN:
                return mFontRobotoThin;

            case FONT_ROBOTOLIGHT:
                return mFontRobotoLight;

            case FONT_ROBOTOBOLD:
                return mFontRobotoBold;

            default:
                Log.e(TAG, "Unknown font code: " + font);
        }
        return null;
    }

    /**
     * Apply a Typeface to a range of text:
     * http://stackoverflow.com/a/4826885/663370
     */
    public class CustomTypefaceSpan extends TypefaceSpan {
        private final Typeface newType;

        public CustomTypefaceSpan(String family, Typeface type) {
            super(family);
            newType = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeface(ds, newType);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            applyCustomTypeface(paint, newType);
        }

        private void applyCustomTypeface(Paint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }
            paint.setTypeface(tf);
        }
    }
}
