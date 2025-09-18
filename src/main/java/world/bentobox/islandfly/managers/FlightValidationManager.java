package world.bentobox.islandfly.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.level.Level;

import java.util.UUID;

/**
 * This class contains methods used to validate flight should be enabled or disabled.
 */
public class FlightValidationManager {
    private final @NotNull IslandFlyAddon islandFlyAddon;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param islandFlyAddon An {@link IslandFlyAddon} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public FlightValidationManager(@NotNull IslandFlyAddon islandFlyAddon, @NotNull PlayerDataManager playerDataManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Checks if the player is allowed to fly in general.
     * @param player The {@link Player} to check.
     * @return true if the player is allowed to fly or false.
     */
    public boolean isPlayerFlightEnabled(@NotNull Player player) {
        // Check if the player has flight enabled in general.
        return player.getAllowFlight();
    }

    /**
     * Check if the player's flight is managed by the addon.
     * @param player The {@link Player} to check.
     * @return true if the player's flight is managed by the addon.
     */
    public boolean isFlightAddonManaged(@NotNull Player player) {
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        return islandFlyPlayerData.isNormalFlightEnabled() || islandFlyPlayerData.isTimedFlightEnabled();
    }

    /**
     * Check if the user is op.
     * @param user The {@link User} to check.
     * @return true if the user is op, or false.
     */
    public boolean isUserOp(@NotNull User user) {
        return user.isOp();
    }

    /**
     * Check if the user is in the gamemode creative or spectator.
     * @param user The {@link User} to check.
     * @return true if in gamemode creative or spectator, or false.
     */
    public boolean isUserCreativeOrSpectator(@NotNull User user) {
        GameMode gameMode = user.getPlayer().getGameMode();
        return gameMode.equals(GameMode.CREATIVE) || gameMode.equals(GameMode.SPECTATOR);
    }

    /**
     * Checks if the user is in a world managed by BentoBox.
     * @param user The {@link User} to check.
     * @return true if the user is in a game mode world, or false.
     */
    public boolean isUserInGameModeWorld(@NotNull User user) {
        return islandFlyAddon.getPlugin().getIWM().getAddon(user.getWorld()).isPresent();
    }

    /**
     * Checks if the user is on an island.
     * @param user The {@link User} to check.
     * @return true if on an island, or false.
     */
    public boolean isUserOnIsland(@NotNull User user) {
        return islandFlyAddon.getIslands().getProtectedIslandAt(user.getLocation()).isPresent();
    }

    /**
     * Checks if the island is a spawn island.
     * @param island The {@link Island} to check.
     * @return true if the island is a spawn island, or false.
     */
    public boolean isIslandSpawnIsland(@NotNull Island island) {
        return island.isSpawn();
    }

    /**
     * Checks if the user has the fly bypass permission.
     * @param user The {@link User} to check.
     * @return true if the user has the permission, or false.
     */
    public boolean hasFlyBypassPermission(@NotNull User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.flybypass");
    }

    /**
     * Checks if the user has the fly spawn permission.
     * @param user The {@link User} to check.
     * @return true if the user has the permission, or false.
     */
    public boolean hasFlySpawnPermission(@NotNull User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.flyspawn");
    }

    /**
     * Checks if the user has the island fly permission.
     * @param user The {@link User} to check.
     * @return true if the user has the permission, or false.
     */
    public boolean hasFlyPermission(@NotNull User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.fly");
    }

    /**
     * Checks if the user has the island timed fly permission.
     * @param user The {@link User} to check.
     * @return true if the user has the permission, or false.
     */
    public boolean hasTimedFlyPermission(User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.timedfly");
    }

    /**
     * Checks if the user has flight time.
     * @param user The {@link User} to check.
     * @return true if the user has flight time, or false.
     */
    public boolean hasFlightTime(@NotNull User user) {
        UUID uuid = user.getUniqueId();
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(uuid);

        return islandFlyPlayerData.getTimeSeconds() > 0;
    }

    /**
     * Checks if flight is allowed on the island based on the island's island level and the min fly level.
     * @param island The {@link Island} to check.
     * @return true if the user can fly based on the island level, or false.
     */
    public boolean canFlyIslandLevel(@NotNull Island island) {
        Level levelAddon = islandFlyAddon.getLevelAddon();
        if(levelAddon == null) return true;
        if(islandFlyAddon.getSettings().getFlyMinLevel() <= 0) return true;
        return levelAddon.getIslandLevel(island.getWorld(), island.getOwner()) >= islandFlyAddon.getSettings().getFlyMinLevel();
    }

    /**
     * Checks if the user is allowed to fly according to the island's settings.
     * @param island The {@link Island} to check.
     * @param user The {@link User} to check.
     * @return true if the island allows the user to fly, or false.
     */
    public boolean isFlyAllowed(@NotNull Island island, @NotNull User user) {
        return island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION);
    }
}
