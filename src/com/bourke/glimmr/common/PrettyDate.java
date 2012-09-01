package com.bourke.glimmr.common;

import android.content.Context;

import android.util.Log;

import com.bourke.glimmr.R;

import java.util.Date;

/**
 * Class for human-readable, pretty date formatting.
 * http://lea.verou.me/2009/04/java-pretty-dates/
 *
 * @author Lea Verou
 */
public class PrettyDate {
    private Date date;

    private static final String TAG = "Glimmr/PrettyDate";

    public PrettyDate() {
        this(new Date());
    }

    public PrettyDate(Date date) {
        this.date = date;
    }

    public String toString() {
        long current = (new Date()).getTime(),
            timestamp = date.getTime(),
            diff = (current - timestamp)/1000;
        int amount = 0;
        String what = "";

        /**
         * Second counts
         * 3600: hour
         * 86400: day
         * 604800: week
         * 2592000: month
         * 31536000: year
         */

        if(diff > 31536000) {
            amount = (int)(diff/31536000);
            what = "year";
        }
        else if(diff > 31536000) {
            amount = (int)(diff/31536000);
            what = "month";
        }
        else if(diff > 604800) {
            amount = (int)(diff/604800);
            what = "week";
        }
        else if(diff > 86400) {
            amount = (int)(diff/86400);
            what = "day";
        }
        else if(diff > 3600) {
            amount = (int)(diff/3600);
            what = "hour";
        }
        else if(diff > 60) {
            amount = (int)(diff/60);
            what = "minute";
        }
        else {
            amount = (int)diff;
            what = "second";
            if(amount < 6) {
                return "Just now";
            }
        }

        if(amount == 1) {
            if(what.equals("day")) {
                return "Yesterday";
            }
            else if(what.equals("week") || what.equals("month")
                    || what.equals("year")) {
                return "Last " + what;
            }
        } else {
            what += "s";
        }

        return amount + " " + what + " ago";
    }

    public String localisedPrettyDate(Context context) {
        String prettyDate = this.toString();
        if (prettyDate.equalsIgnoreCase("Just now")) {
            return context.getString(R.string.just_now);
        }
        if (prettyDate.equalsIgnoreCase("Yesterday")) {
            return context.getString(R.string.yesterday);
        }
        if (prettyDate.equalsIgnoreCase("Last week")) {
            return context.getString(R.string.last_week);
        }
        if (prettyDate.equalsIgnoreCase("Last month")) {
            return context.getString(R.string.last_month);
        }
        if (prettyDate.equalsIgnoreCase("Last year")) {
            return context.getString(R.string.last_year);
        }
        if (prettyDate.endsWith("days ago")) {
            return String.format("%s %s",
                    prettyDate.replace("days ago", "").trim(),
                    context.getString(R.string.n_days_ago));
        }
        if (prettyDate.endsWith("weeks ago")) {
            return String.format("%s %s",
                    prettyDate.replace("weeks ago", "").trim(),
                    context.getString(R.string.n_weeks_ago));
        }
        if (prettyDate.endsWith("months ago")) {
            return String.format("%s %s",
                    prettyDate.replace("months ago", "").trim(),
                    context.getString(R.string.n_months_ago));
        }
        if (prettyDate.endsWith("years ago")) {
            return String.format("%s %s",
                    prettyDate.replace("years ago", "").trim(),
                    context.getString(R.string.n_years_ago));
        }
        Log.e(TAG, "Unknown PrettyDate string: " + prettyDate);
        return prettyDate;
    }
}
