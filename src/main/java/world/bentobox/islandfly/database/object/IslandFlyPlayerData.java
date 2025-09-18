package world.bentobox.islandfly.database.object;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

import java.util.UUID;

/**
 * This class stores the player's flight time and whether their flight is enabled because of the addon.
 */
@Table(name = "IslandFlyPlayerData")
public class IslandFlyPlayerData implements DataObject {
    /**
     * The uuid of the player as a {@link String}
     */
    @Expose
    String uuid;
    /**
     * The flight time the player has in seconds.
     */
    @Expose
    int timeSeconds;
    /**
     * Whether the player has unlimited flight enabled.
     */
    boolean normalFlight = false;
    /**
     * Whether the player has timed flight enabled.
     */
    boolean timedFlight = false;

    /**
     *
     * @param uuid The {@link UUID} as a {@link String} for the player.
     * @param timeSeconds The time in seconds the player has.
     */
    public IslandFlyPlayerData(@NotNull String uuid, int timeSeconds) {
        this.uuid = uuid;
        this.timeSeconds = timeSeconds;
    }

    /**
     * Get the player's unique id as a {@link String}.
     * @return The player's unique id as a {@link String}.
     */
    @Override
    public String getUniqueId() {
        return uuid;
    }

    /**
     * Set the player's unique id.
     * @param uuid The player's unique id as a {@link String}.
     */
    @Override
    public void setUniqueId(@NotNull String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the player's flight time in seconds.
     * @return The player's flight time in seconds.
     */
    public int getTimeSeconds() {
        return timeSeconds;
    }

    /**
     * Set the player's flight time.
     * @param timeSeconds The flight time in seconds.
     */
    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    /**
     * Add to the player's flight time.
     * @param timeSeconds The flight time in seconds.
     */
    public void addTimeSeconds(int timeSeconds) {
        this.timeSeconds += timeSeconds;
    }

    /**
     * Remove from the player's flight time.
     * @param timeSeconds The flight time in seconds.
     */
    public void removeTimeSeconds(int timeSeconds) {
        this.timeSeconds = Math.max((this.timeSeconds - timeSeconds), 0);
    }

    /**
     * Is normal or unlimited flight enabled for the player?
     * @return true or false
     */
    public boolean isNormalFlightEnabled() {
        return normalFlight;
    }

    /**
     * Set whether normal or unlimited flight is enabled for the player.
     * @param normalFlight true or false
     */
    public void setNormalFlight(boolean normalFlight) {
        this.normalFlight = normalFlight;
    }

    /**
     * Is normal or timed flight enabled for the player?
     * @return true or false
     */
    public boolean isTimedFlightEnabled() {
        return timedFlight;
    }

    /**
     * Set whether timed flight is enabled for the player.
     * @param timedFlight true or false
     */
    public void setTimedFlight(boolean timedFlight) {
        this.timedFlight = timedFlight;
    }
}
