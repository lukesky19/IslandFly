package world.bentobox.islandfly.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


/**
 * This class manages Death and Respawn options.
 */
public class FlyDeathListener implements Listener {

	/**
	 * BentoBox plugin instance.
	 */
    private final BentoBox plugin;
	/**
	 * Instance of FlightTimeManager
	 */
	final FlightTimeManager flightTimeManager;


	/**
	 * Default constructor.
	 * @param addon IslandFlyAddon instance
	 */
	public FlyDeathListener(final IslandFlyAddon addon, FlightTimeManager flightTimeManager) {
        this.plugin = addon.getPlugin();
		this.flightTimeManager = flightTimeManager;
    }

	/**
	 * Fired when player died. Removes fly ability in user world, if user does not have flybypass permission.
	 * @param event Instance of PlayerDeathEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onDeath(final PlayerDeathEvent event) {
	    // Get the BentoBox User based on the player from the PlayerDeathEvent.
	    final User user = User.getInstance(event.getEntity().getUniqueId());

		// Check if the player can fly on non-islands.
	    if(plugin.getIWM().getAddon(user.getWorld()).
			map(a -> user.hasPermission(a.getPermissionPrefix() + "island.flybypass")).
			orElse(false)) {
	    	return;
		}

		// Disable fly on death
	    disableFly(user);
	}


	/**
	 * Enable fly mode if player had it before.
	 * If the player has both regular island fly and temporary island fly, regular island fly will take priority.
	 * Otherwise, temporary fly will be enabled if the player still has time left.
	 * @param event Instance of PlayerRespawnEvent
	 */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent event) {
	    // If a player respawns on an island that he's added to, do nothing.
	    // Otherwise - disable Fly
	    final Player player = event.getPlayer();
	    final UUID playerUUID = player.getUniqueId();
	    Optional<Island> island = plugin.getIslands().getIslandAt(player.getLocation());
		final String permPrefix = plugin.getIWM().getPermissionPrefix(player.getWorld());

	    if (island.isPresent() && island.get().getMembers().containsKey(playerUUID)) {
			// Default to regular island fly if the player has both regular island fly and temporary island fly permissions.
			if(player.hasPermission(permPrefix + "island.fly")
					&& player.hasPermission(permPrefix + "island.tempfly")
					|| player.hasPermission(permPrefix + "island.fly")) {
				if(player.getAllowFlight()) {
					player.setFlying(true);
				}
			} else if(player.hasPermission(permPrefix + "island.tempfly")) {
				if(player.getAllowFlight()) {
					// Checks if the user has no flight data or flight time equal to 0.
					IslandFlyPlayerData data = flightTimeManager.getPlayerFlightData(player.getUniqueId());
					if(!Objects.equals(data, null) || data.getTimeSeconds() != 0) {
						// Enable temp flight if the player has time
						flightTimeManager.trackPlayerFlightTime(player);
						player.setFlying(true);
					}
				}
			}
	    }
	}


	/**
	 * This method disables fly mode for given User.
	 * @param user Which must lose its fly ability.
	 */
	private void disableFly(final User user) {
		// Stop tracking player flight time.
		if(flightTimeManager.isPlayerFlightTimeTracked(user.getUniqueId())) {
			flightTimeManager.stopTrackingPlayerFlightTime(user.getPlayer());
		}

		// Set player as not flying.
		user.getPlayer().setFlying(false);
    }
}
