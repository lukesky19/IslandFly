package world.bentobox.islandfly.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

import java.util.Optional;
import java.util.UUID;

/**
 * This class manages Death and Respawn options.
 */
public class FlyDeathListener implements Listener {
    private final @NotNull BentoBox bentoBox;
	private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;

	/**
	 * Constructor
	 * @param islandFlyAddon An {@link IslandFlyAddon} instance.
	 */
	public FlyDeathListener(
            @NotNull IslandFlyAddon islandFlyAddon,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull BossBarManager bossBarManager) {
        this.bentoBox = islandFlyAddon.getPlugin();
		this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

	/**
	 * Fired when player died. Removes fly ability in user world, if user does not have the fly bypass permission.
	 * @param playerDeathEvent A {@link PlayerDeathEvent}.
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent playerDeathEvent) {
	    // Get the BentoBox User based on the player from the PlayerDeathEvent.
	    User user = User.getInstance(playerDeathEvent.getEntity().getUniqueId());
        Player player = user.getPlayer();

		// Check if the player can fly on non-islands.
	    if(bentoBox.getIWM().getAddon(user.getWorld()).
			map(a -> user.hasPermission(a.getPermissionPrefix() + "island.flybypass")).
			orElse(false)) {
	    	return;
		}

		// Disable fly on death
        player.setFlying(false);
	}

	/**
	 * Enable fly mode if player had it before.
	 * If the player has both regular island fly and timed island fly, regular island fly will take priority.
	 * Otherwise, timed flight will be enabled if the player still has time left.
	 * @param event Instance of PlayerRespawnEvent
	 */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent event) {
	    Player player = event.getPlayer();
	    UUID uuid = player.getUniqueId();
	    Optional<Island> optionalIsland = bentoBox.getIslands().getIslandAt(player.getLocation());
		String permPrefix = bentoBox.getIWM().getPermissionPrefix(player.getWorld());

        if(optionalIsland.isEmpty()) return;
        if(!optionalIsland.get().getMemberSet().contains(uuid)) return;

        // Get the player's IslandFlyPlayerData
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(uuid);

        // Check if fly was enabled at the time of the death
        if(islandFlyPlayerData.isNormalFlightEnabled()) {
            if(player.hasPermission(permPrefix + "island.fly")) {
                player.setFlying(true);
            } else {
                // If the player is no longer able to fly, disable the internal flight setting.
                islandFlyPlayerData.setNormalFlight(false);

                // Remove any boss bar shown
                bossBarManager.removeBossBar(player);
            }
        } else if(islandFlyPlayerData.isTimedFlightEnabled()) {
            if(player.hasPermission(permPrefix + "island.timedfly")) {
                if(islandFlyPlayerData.getTimeSeconds() > 0) {
                    player.setFlying(true);
                } else {
                    // If the player is no longer able to fly, disable the internal flight setting.
                    islandFlyPlayerData.setTimedFlight(false);

                    // Save player flight data.
                    playerDataManager.savePlayerData(islandFlyPlayerData);

                    // Remove any boss bar shown
                    bossBarManager.removeBossBar(player);
                }
            } else {
                // If the player is no longer able to fly, disable the internal flight setting.
                islandFlyPlayerData.setTimedFlight(false);

                // Save player flight data.
                playerDataManager.savePlayerData(islandFlyPlayerData);

                // Remove any boss bar shown
                bossBarManager.removeBossBar(player);
            }
        }
	}
}
