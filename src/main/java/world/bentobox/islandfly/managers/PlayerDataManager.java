package world.bentobox.islandfly.managers;

import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.Database;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages the modification of and tracking of flight time.
 */
public class PlayerDataManager {
    /**
     * Database that stores player's flight time.
     */
    private final @NotNull Database<IslandFlyPlayerData> islandFlyPlayerDatabase;
    /**
     * Stores players flight time that are currently online.
     */
    private final @NotNull Map<@NotNull UUID, @NotNull IslandFlyPlayerData> playerDataMap = new HashMap<>();

    /**
     * Constructor
     * @param islandFlyPlayerDatabase A {@link Database} of type {@link IslandFlyPlayerData}.
     */
    public PlayerDataManager(@NotNull Database<IslandFlyPlayerData> islandFlyPlayerDatabase) {
        this.islandFlyPlayerDatabase = islandFlyPlayerDatabase;
    }

    /**
     * Get the {@link Map} mapping {@link UUID} to {@link IslandFlyPlayerData}.
     * @return The {@link Map} mapping {@link UUID} to {@link IslandFlyPlayerData}.
     */
    public @NotNull Map<@NotNull UUID, @NotNull IslandFlyPlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    /**
     * Get the {@link IslandFlyPlayerData} stored for the uuid provided.
     * @param uuid he {@link UUID} of the player.
     * @return The {@link IslandFlyPlayerData} for the player.
     */
    public @NotNull IslandFlyPlayerData getPlayerFlightData(@NotNull UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, playerId -> new IslandFlyPlayerData(playerId.toString(), 0));
    }

    /**
     * Load the {@link IslandFlyPlayerData} for the uuid provided.
     * @param uuid The {@link UUID} of the player.
     */
    public void loadPlayerData(@NotNull UUID uuid) {
        IslandFlyPlayerData islandFlyPlayerData = islandFlyPlayerDatabase.loadObject(uuid.toString());
        if(islandFlyPlayerData == null) islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);

        playerDataMap.put(uuid, islandFlyPlayerData);
    }

    /**
     * Save the {@link IslandFlyPlayerData} currently stored for uuid provided.
     * @param uuid The {@link UUID} of the player.
     */
    public void savePlayerData(@NotNull UUID uuid) {
        IslandFlyPlayerData islandFlyPlayerData = playerDataMap.get(uuid);
        if(islandFlyPlayerData == null) return;

        savePlayerData(islandFlyPlayerData);
    }

    /**
     * Save the {@link IslandFlyPlayerData} provided.
     * @param islandFlyPlayerData The {@link IslandFlyPlayerData} to save.
     */
    public void savePlayerData(@NotNull IslandFlyPlayerData islandFlyPlayerData) {
        islandFlyPlayerDatabase.saveObject(islandFlyPlayerData);
    }

    /**
     * Removes the {@link IslandFlyPlayerData} currently stored for uuid provided.
     * You should call {@link #savePlayerData(UUID)} before this method.
     * @param uuid The {@link UUID} of the player.
     */
    public void unloadPlayerData(@NotNull UUID uuid) {
        playerDataMap.remove(uuid);
    }
}
