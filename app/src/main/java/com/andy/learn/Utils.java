
package com.andy.learn;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;

import java.util.Set;

public class Utils {

    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;

    public static final int DECLINED_EVENT_ALPHA = 0x66;

    // The name of the shared preferences file. This name must be maintained for
    // historical
    // reasons, as it's what PreferenceManager assigned the first time the file
    // was created.
    static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";

    public static final String KEY_HIDE_DECLINED = "preferences_hide_declined";

    /**
     * Key to SharePreference for default detail view (CalendarController.ViewType)
     * Typically used by widget
     */
    public static final String KEY_DETAILED_VIEW = "preferred_detailedView";
    // Default preference values
    public static final int DEFAULT_START_VIEW = CalendarController.ViewType.WEEK;
    public static final int DEFAULT_DETAILED_VIEW = CalendarController.ViewType.DAY;

    private static final CalendarUtils.TimeZoneUtils mTZUtils = new CalendarUtils.TimeZoneUtils(SHARED_PREFS_NAME);

    /**
     * Gets the time zone that Calendar should be displayed in This is a helper
     * method to get the appropriate time zone for Calendar. If this is the
     * first time this method has been called it will initiate an asynchronous
     * query to verify that the data in preferences is correct. The callback
     * supplied will only be called if this query returns a value other than
     * what is stored in preferences and should cause the calling activity to
     * refresh anything that depends on calling this method.
     *
     * @param context  The calling activity
     * @param callback The runnable that should execute if a query returns new
     *                 values
     * @return The string value representing the time zone Calendar should
     * display
     */
    public static String getTimeZone(Context context, Runnable callback) {
        return mTZUtils.getTimeZone(context, callback);
    }

    // This takes a color and computes what it would look like blended with
    // white. The result is the color that should be used for declined events.
    public static int getDeclinedColorFromColor(int color) {
        int bg = 0xffffffff;
        int a = DECLINED_EVENT_ALPHA;
        int r = (((color & 0x00ff0000) * a) + ((bg & 0x00ff0000) * (0xff - a))) & 0xff000000;
        int g = (((color & 0x0000ff00) * a) + ((bg & 0x0000ff00) * (0xff - a))) & 0x00ff0000;
        int b = (((color & 0x000000ff) * a) + ((bg & 0x000000ff) * (0xff - a))) & 0x0000ff00;
        return (0xff000000) | ((r | g | b) >> 8);
    }

    /**
     * For devices with Jellybean or later, darkens the given color to ensure that white text is
     * clearly visible on top of it.  For devices prior to Jellybean, does nothing, as the
     * sync adapter handles the color change.
     *
     * @param color
     */
    public static int getDisplayColorFromColor(int color) {
        if (!isJellybeanOrLater()) {
            return color;
        }

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f);
        hsv[2] = hsv[2] * INTENSITY_ADJUST;
        return Color.HSVToColor(hsv);
    }

    /**
     * Returns whether the SDK is the Jellybean release or later.
     */
    public static boolean isJellybeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Formats a day of the week string. This is either just the name of the day
     * or a combination of yesterday/today/tomorrow and the day of the week.
     *
     * @param julianDay      The julian day to get the string for
     * @param todayJulianDay The julian day for today's date
     * @param millis         A utc millis since epoch time that falls on julian day
     * @param context        The calling context, used to get the timezone and do the
     *                       formatting
     * @return
     */
    public static String getDayOfWeekString(int julianDay, int todayJulianDay, long millis,
                                            Context context) {
        getTimeZone(context, null);
        int flags = DateUtils.FORMAT_SHOW_WEEKDAY;
        String dayViewText;
        if (julianDay == todayJulianDay) {
            dayViewText = context.getString(R.string.agenda_today,
                    mTZUtils.formatDateRange(context, millis, millis, flags).toString());
        } else if (julianDay == todayJulianDay - 1) {
            dayViewText = context.getString(R.string.agenda_yesterday,
                    mTZUtils.formatDateRange(context, millis, millis, flags).toString());
        } else if (julianDay == todayJulianDay + 1) {
            dayViewText = context.getString(R.string.agenda_tomorrow,
                    mTZUtils.formatDateRange(context, millis, millis, flags).toString());
        } else {
            dayViewText = mTZUtils.formatDateRange(context, millis, millis, flags).toString();
        }
        dayViewText = dayViewText.toUpperCase();
        return dayViewText;
    }

    // Calculate the time until midnight + 1 second and set the handler to
    // do run the runnable
    public static void setMidnightUpdater(Handler h, Runnable r, String timezone) {
        if (h == null || r == null || timezone == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Time time = new Time(timezone);
        time.set(now);
        long runInMillis = (24 * 3600 - time.hour * 3600 - time.minute * 60 -
                time.second + 1) * 1000;
        h.removeCallbacks(r);
        h.postDelayed(r, runInMillis);
    }

    /**
     * Finds and returns the next midnight after "theTime" in milliseconds UTC
     *
     * @param recycle - Time object to recycle, otherwise null.
     * @param theTime - Time used for calculations (in UTC)
     * @param tz The time zone to convert this time to.
     */
    public static long getNextMidnight(Time recycle, long theTime, String tz) {
        if (recycle == null) {
            recycle = new Time();
        }
        recycle.timezone = tz;
        recycle.set(theTime);
        recycle.monthDay ++;
        recycle.hour = 0;
        recycle.minute = 0;
        recycle.second = 0;
        return recycle.normalize(true);
    }

    public static boolean getConfigBool(Context c, int key) {
        return c.getResources().getBoolean(key);
    }

    public static long convertAlldayLocalToUTC(Time recycle, long localTime, String tz) {
        if (recycle == null) {
            recycle = new Time();
        }
        recycle.timezone = tz;
        recycle.set(localTime);
        recycle.timezone = Time.TIMEZONE_UTC;
        return recycle.normalize(true);
    }

    /**
     * Convert given UTC time into current local time. This assumes it is for an
     * allday event and will adjust the time to be on a midnight boundary.
     *
     * @param recycle Time object to recycle, otherwise null.
     * @param utcTime Time to convert, in UTC.
     * @param tz The time zone to convert this time to.
     */
    public static long convertAlldayUtcToLocal(Time recycle, long utcTime, String tz) {
        if (recycle == null) {
            recycle = new Time();
        }
        recycle.timezone = Time.TIMEZONE_UTC;
        recycle.set(utcTime);
        recycle.timezone = tz;
        return recycle.normalize(true);
    }

    public static int getSharedPreference(Context context, String key, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key, defaultValue);
    }

    public static boolean getAllowWeekForDetailView() {
        return false;
    }

    // Stop the midnight update thread
    public static void resetMidnightUpdater(Handler h, Runnable r) {
        if (h == null || r == null) {
            return;
        }
        h.removeCallbacks(r);
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}