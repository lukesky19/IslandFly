package world.bentobox.islandfly.util;

import org.jetbrains.annotations.NotNull;

/**
 * A class containing utilities for converting seconds to readable time formats.
 */
public class TimeUtil {
    private static final int MINUTE = 60;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
    private static final int WEEK = 7 * DAY;
    private static final int MONTH = 30 * DAY;
    private static final int YEAR = 365 * DAY;

    /**
     * All methods in this class are static so this constructor will throw a runtime exception if used.
     * @throws RuntimeException if the constructor is used.
     */
    public TimeUtil() {
        throw new RuntimeException("This class cannot be instanced. Use the static references to methods instead.");
    }

    /**
     * Takes an int representing seconds and returns a {@link Time} Record that holds the years, months, weeks, days, hours, minutes, and seconds.
     * @param seconds The seconds to convert.
     * @return A {@link Time} Record that holds the years, months, weeks, days, hours, minutes, and seconds.
     */
    public static @NotNull Time secondsToTime(int seconds) {
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;

        if(seconds >= YEAR) {
            years = seconds / YEAR;
            seconds %= YEAR;
        }

        if(seconds >= MONTH) {
            months = seconds / MONTH;
            seconds %= MONTH;
        }

        if(seconds >= WEEK) {
            weeks = seconds / WEEK;
            seconds %= WEEK;
        }

        if(seconds >= DAY) {
            days = seconds / DAY;
            seconds %= DAY;
        }

        if(seconds >= HOUR) {
            hours = seconds / HOUR;
            seconds %= HOUR;
        }

        if(seconds >= MINUTE) {
            minutes = seconds / MINUTE;
            seconds %= MINUTE;
        }

        return new Time(years, months, weeks, days, hours, minutes, seconds);
    }
}