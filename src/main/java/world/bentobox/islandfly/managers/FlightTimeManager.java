package world.bentobox.islandfly.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * This class manages the modification of and tracking of flight time.
 */
public class FlightTimeManager {
    /**
     * IslandFlyAddon Instance.
     */
    final IslandFlyAddon islandFlyAddon;
    /**
     * Database that stores player's flight time.
     */
    final Database<IslandFlyPlayerData> islandFlyPlayerDatabase;
    /**
     * Stores all the BukkitTasks that tracks player's flight time.
     */
    final HashMap<UUID, BukkitTask> tasks = new HashMap<>();
    /**
     * Stores the flight time for players in memory when they are using temporary flight.
     */
    final HashMap<UUID, Integer> activePlayerFlightTime = new HashMap<>();

    /**
     * Constructor
     * @param islandFlyAddon Instance of IslandFlyAddon
     */
    public FlightTimeManager(IslandFlyAddon islandFlyAddon) {
        this.islandFlyAddon = islandFlyAddon;
        this.islandFlyPlayerDatabase = new Database<>(islandFlyAddon, IslandFlyPlayerData.class);
    }

    /**
     * Method to set the player's flight time and disable flight if necessary.
     * @param player The player to update the flight time for.
     * @param time The flight time in seconds.
     * @return The updated flight time for the player.
     */
    public Integer setPlayerFlightTime(Player player, int time) {
        UUID uuid = player.getUniqueId();
        String uuidString = uuid.toString();

        if(isPlayerFlightTimeTracked(uuid)) {
            if(time > 0) {
                stopTrackingPlayerFlightTime(player);
                islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, time));
                trackPlayerFlightTime(player);
            } else {
                stopTrackingPlayerFlightTime(player);
                islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, time));
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage("islandfly.flight-time-ended");
            }
        } else {
            islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, time));
        }

        return time;
    }

    /**
     * Method to add to the player's flight time and if the player has no flight data, use
     * {@link FlightTimeManager#setPlayerFlightTime(Player, int)} as a fallback.
     * @param player The player to update the flight time for.
     * @param time The flight time in seconds.
     * @return The updated flight time for the player.
     */
    public Integer addPlayerFlightTime(Player player, int time) {
        UUID uuid = player.getUniqueId();
        String uuidString = uuid.toString();
        int updatedTime;

        if(isPlayerFlightTimeTracked(uuid)) {
            stopTrackingPlayerFlightTime(player);

            // Use #setPlayerFlightTime as a fallback if the player has no flight data yet.
            IslandFlyPlayerData data = getPlayerFlightData(uuid);
            if(data == null) {
                return setPlayerFlightTime(player, time);
            }

            updatedTime = data.getTimeSeconds() + time;

            islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, updatedTime));
            trackPlayerFlightTime(player);
        } else {
            // Use #setPlayerFlightTime as a fallback if the player has no flight data yet.
            IslandFlyPlayerData data = getPlayerFlightData(uuid);
            if(data == null) {
                return setPlayerFlightTime(player, time);
            }

            updatedTime = data.getTimeSeconds() + time;
            islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, updatedTime));
        }

        return updatedTime;
    }

    /**
     * Method to remove the player's flight time and if the player has no flight data, use
     * {@link FlightTimeManager#setPlayerFlightTime(Player, int)} as a fallback.
     * @param player The player to update the flight time for.
     * @param time The flight time in seconds.
     * @return The updated flight time for the player.
     */
    public Integer removePlayerFlightTime(Player player, int time) {
        UUID uuid = player.getUniqueId();
        String uuidString = uuid.toString();
        int updatedTime;

        if(isPlayerFlightTimeTracked(uuid)) {
            stopTrackingPlayerFlightTime(player);

            // Use #setPlayerFlightTime as a fallback if the player has no flight data yet.
            IslandFlyPlayerData data = getPlayerFlightData(uuid);
            if(data == null) {
                return setPlayerFlightTime(player, time);
            }

            updatedTime = data.getTimeSeconds() - time;

            // Use #setPlayerFlightTime as a fallback updated time will be <= 0.
            if(updatedTime <= 0) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage("islandfly.flight-time-ended");
                return setPlayerFlightTime(player, 0);
            }

            islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, updatedTime));
            trackPlayerFlightTime(player);
        } else {
            // Use #setPlayerFlightTime as a fallback if the player has no flight data yet.
            IslandFlyPlayerData data = getPlayerFlightData(uuid);
            if(data == null) {
                return setPlayerFlightTime(player, time);
            }

            updatedTime = data.getTimeSeconds() - time;

            // Use #setPlayerFlightTime as a fallback since updated time will be <= 0.
            if(updatedTime <= 0) {
                return setPlayerFlightTime(player, 0);
            }

            islandFlyPlayerDatabase.saveObject(new IslandFlyPlayerData(uuidString, updatedTime));
        }

        return updatedTime;
    }

    /**
     * Method to wipe a player's flight data from the database.
     * @param player The player to update the flight time for.
     * @return The updated flight time for the player.
     */
    public Integer deletePlayerFlightData(Player player) {
        UUID uuid = player.getUniqueId();

        if(isPlayerFlightTimeTracked(uuid)) {
            stopTrackingPlayerFlightTime(player);
            islandFlyPlayerDatabase.deleteObject(getPlayerFlightData(uuid));
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage("islandfly.flight-time-ended");
        } else {
            islandFlyPlayerDatabase.deleteObject(getPlayerFlightData(uuid));
        }

        return 0;
    }

    /**
     * Method to retrieve a player's flight data from the database.
     * @param uuid The UUID of the player to retrieve flight data for.
     * @return The player's flight data or null if no data exists.
     */
    @Nullable
    public IslandFlyPlayerData getPlayerFlightData(UUID uuid) {
        if(islandFlyPlayerDatabase.objectExists(uuid.toString())) {
            return islandFlyPlayerDatabase.loadObject(uuid.toString());
        } else {
            return null;
        }
    }

    /**
     * Method to start tracking the player's flight time.
     * @param player The player whose flight time will be tracked.
     */
    public void trackPlayerFlightTime(Player player) {
        UUID uuid = player.getUniqueId();
        User user = User.getInstance(player);

        activePlayerFlightTime.put(uuid, Objects.requireNonNull(getPlayerFlightData(uuid)).getTimeSeconds());
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if(player.getAllowFlight() && player.isFlying()) {
                    int time = activePlayerFlightTime.get(uuid);
                    switch(time) {
                        case 30, 15, 10, 5, 4, 3, 2, 1 -> {
                            user.sendMessage("islandfly.flight-time-warning", TextVariables.NUMBER, String.valueOf(time));
                            activePlayerFlightTime.put(uuid, time - 1);
                        }
                        default -> {
                            if(time == 0) {
                                user.sendMessage("islandfly.flight-time-ended");
                                stopTrackingPlayerFlightTime(player);
                                player.setFlying(false);
                                player.setAllowFlight(false);
                            } else {
                                activePlayerFlightTime.put(uuid, time - 1);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(islandFlyAddon.getPlugin(), 1, 20L);

        tasks.put(uuid, task);
    }

    /**
     * Method to stop tracking the player's flight time.
     * @param player The player whose flight time that will stop being tracked.
     */
    public void stopTrackingPlayerFlightTime(Player player) {
        UUID uuid = player.getUniqueId();
        tasks.get(uuid).cancel();
        tasks.remove(uuid);
        setPlayerFlightTime(player, activePlayerFlightTime.get(uuid));
        activePlayerFlightTime.remove(uuid);
    }

    /**
     * Method to check if a player's flight time is tracked or not.
     * @param uuid The UUID of the player to check if their flight time is tracked or not.
     * @return <code>true</code> if the player's flight time is tracked, <code>false</code> if not.
     */
    public boolean isPlayerFlightTimeTracked(UUID uuid) {
        return tasks.containsKey(uuid) && activePlayerFlightTime.containsKey(uuid) && tasks.get(uuid) != null && activePlayerFlightTime.get(uuid) != null;
    }
}
