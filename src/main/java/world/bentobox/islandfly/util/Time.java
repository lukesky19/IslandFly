package world.bentobox.islandfly.util;

/**
 * Stores the years, months, weeks, days, hours, minutes, and seconds, for a point in time.
 * See {@link TimeUtil#secondsToTime(int)}
 * @param years The years as an int
 * @param months The months as an int
 * @param weeks The weeks as an int
 * @param days The days as an int
 * @param hours The hours as an int
 * @param minutes The minutes as an int
 * @param seconds The seconds as an int
 */
public record Time(
        int years,
        int months,
        int weeks,
        int days,
        int hours,
        int minutes,
        int seconds) {}
