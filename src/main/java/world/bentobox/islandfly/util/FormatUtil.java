package world.bentobox.islandfly.util;

import org.jetbrains.annotations.NotNull;

/**
 * A class containing utilities formatting time in seconds to a {@link String} containing a readable format.
 */
public class FormatUtil {
    /**
     * All methods in this class are static so this constructor will throw a runtime exception if used.
     * @throws RuntimeException if the constructor is used.
     */
    public FormatUtil() {
        throw new RuntimeException("This class cannot be instanced. Use the static references to methods instead.");
    }

    /**
     * Formats the number of seconds to a formatted message to display.
     * If any value is 0, it won't be shown unless all values are 0.
     * @return The time in seconds formatted to a {@link String}.
     */
    public static @NotNull String formatTimeSeconds(int timeSeconds) {
        boolean firstUnit = true;
        Time timeRecord = TimeUtil.secondsToTime(timeSeconds);
        StringBuilder messageBuilder = new StringBuilder();

        if(timeRecord.years() > 0) {
            messageBuilder.append(timeRecord.years()).append(" year(s)");
            firstUnit = false;
        }

        if(timeRecord.months() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(timeRecord.months()).append(" months(s)");
            firstUnit = false;
        }

        if(timeRecord.weeks() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(timeRecord.weeks()).append(" week(s)");
            firstUnit = false;
        }

        if(timeRecord.days() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(timeRecord.days()).append(" day(s)");
            firstUnit = false;
        }

        if(timeRecord.hours() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(timeRecord.hours()).append(" hour(s)");
            firstUnit = false;
        }

        if(timeRecord.minutes() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(timeRecord.minutes()).append(" minute(s)");
            firstUnit = false;
        }

        if(timeRecord.seconds() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(timeRecord.seconds()).append(" second(s)");
            firstUnit = false;
        }

        if(firstUnit) {
            messageBuilder.append(timeRecord.seconds()).append(" second(s)");
        }

        return messageBuilder.toString();
    }
}
