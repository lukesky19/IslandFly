package world.bentobox.islandfly.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

import java.util.UUID;

/**
 * This class disables flight, removes the boss bar, and then saves and unloads player data if the player quits server.
 */
public class FlyLogoutListener implements Listener {
    private final @NotNull IslandFlyAddon islandFlyAddon;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;

    /**
     * Constructor.
     * @param islandFlyAddon An {@link IslandFlyAddon} instance.
     * @param playerDataManager  A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     */
    public FlyLogoutListener(
            @NotNull IslandFlyAddon islandFlyAddon,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull BossBarManager bossBarManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

    /**
     * Disables flight, removes the boss bar, and then saves and unloads player data if the player quits server.
     * @param playerQuitEvent A {@link PlayerQuitEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLogout(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        // Get player data
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        if(player.getAllowFlight()
                && (islandFlyPlayerData.isNormalFlightEnabled() || islandFlyPlayerData.isTimedFlightEnabled())
                && this.islandFlyAddon.getSettings().isFlyDisableOnLogout()) {
            islandFlyAddon.logWarning("Disabling flight");

            // Disable fly
            islandFlyPlayerData.setNormalFlight(false);
            islandFlyPlayerData.setTimedFlight(false);
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        // Remove Boss Bar
        bossBarManager.removeBossBar(player);

        // Unload player data
        playerDataManager.unloadPlayerData(uuid);
    }
}
