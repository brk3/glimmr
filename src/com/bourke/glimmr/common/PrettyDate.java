package com.bourke.glimmr.common;

import android.content.Context;

import android.util.Log;

import com.bourke.glimmr.R;

import java.util.Date;

/**
 * Class for human-readable, pretty date formatting.
 * Original code from http://lea.verou.me/2009/04/java-pretty-dates/
 *
 * @author Lea Verou
 * @author Paul Bourke
 */
public class PrettyDate {
    private Date date;

    private static final String TAG = "Glimmr/PrettyDate";

    private static final int MINUTE = 60;
    private static final int HOUR = 3600;
    private static final int DAY = 86400;
    private static final int WEEK = 604800;
    private static final int MONTH = 2592000;
    private static final int YEAR = 31536000;

    public PrettyDate() {
        this(new Date());
    }

    public PrettyDate(Date date) {
        this.date = date;
    }

    public String toString() {
        long current = (new Date()).getTime();
        long timestamp = date.getTime();
        long diff = (current - timestamp)/1000;
        int amount = 0;
        String what = "";

        if(diff > YEAR) {
            amount = (int)(diff/YEAR);
            what = "year";
        }
        else if(diff > MONTH) {
            amount = (int)(diff/MONTH);
            what = "month";
        }
        else if(diff > WEEK) {
            amount = (int)(diff/WEEK);
            what = "week";
        }
        else if(diff > DAY) {
            amount = (int)(diff/DAY);
            what = "day";
        }
        else if(diff > HOUR) {
            amount = (int)(diff/HOUR);
            what = "hour";
        }
        else if(diff > MINUTE) {
            amount = (int)(diff/MINUTE);
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
        if (prettyDate.endsWith("minutes ago")) {
            return String.format("%s %s",
                    prettyDate.replace("minutes ago", "").trim(),
                    context.getString(R.string.n_minutes_ago));
        }
        if (prettyDate.endsWith("minute ago")) {
            return String.format("%s %s",
                    prettyDate.replace("minute ago", "").trim(),
                    context.getString(R.string.one_minute_ago));
        }
        if (prettyDate.endsWith("hours ago")) {
            return String.format("%s %s",
                    prettyDate.replace("hours ago", "").trim(),
                    context.getString(R.string.n_hours_ago));
        }
        if (prettyDate.endsWith("hour ago")) {
            return String.format("%s %s",
                    prettyDate.replace("hour ago", "").trim(),
                    context.getString(R.string.one_hour_ago));
        }
        if (prettyDate.endsWith("days ago")) {
            return String.format("%s %s",
                    prettyDate.replace("days ago", "").trim(),
                    context.getString(R.string.n_days_ago));
        }
        if (prettyDate.endsWith("day ago")) {
            return String.format("%s %s",
                    prettyDate.replace("day ago", "").trim(),
                    context.getString(R.string.one_day_ago));
        }
        if (prettyDate.endsWith("weeks ago")) {
            return String.format("%s %s",
                    prettyDate.replace("weeks ago", "").trim(),
                    context.getString(R.string.n_weeks_ago));
        }
        if (prettyDate.endsWith("week ago")) {
            return String.format("%s %s",
                    prettyDate.replace("week ago", "").trim(),
                    context.getString(R.string.one_week_ago));
        }
        if (prettyDate.endsWith("months ago")) {
            return String.format("%s %s",
                    prettyDate.replace("months ago", "").trim(),
                    context.getString(R.string.n_months_ago));
        }
        if (prettyDate.endsWith("month ago")) {
            return String.format("%s %s",
                    prettyDate.replace("month ago", "").trim(),
                    context.getString(R.string.one_month_ago));
        }
        if (prettyDate.endsWith("years ago")) {
            return String.format("%s %s",
                    prettyDate.replace("years ago", "").trim(),
                    context.getString(R.string.n_years_ago));
        }
        if (prettyDate.endsWith("year ago")) {
            return String.format("%s %s",
                    prettyDate.replace("year ago", "").trim(),
                    context.getString(R.string.one_year_ago));
        }
        Log.e(TAG, "Unknown PrettyDate string: " + prettyDate);
        return prettyDate;
    }
}
