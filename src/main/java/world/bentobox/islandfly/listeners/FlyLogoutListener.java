package world.bentobox.islandfly.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * This class disables fly mode if player quits server.
 */
public class FlyLogoutListener implements Listener {
    /**
     * IslandFlyAddon instance.
     */
    private final IslandFlyAddon islandFlyAddon;
    /**
     * FlightTimeManager instance.
     */
    final FlightTimeManager flightTimeManager;

    /**
     * Constructor.
     * @param islandFlyAddon Instance of IslandFlyAddon
     * @param flightTimeManager Instance of FlightTimeManager
     */
    public FlyLogoutListener(IslandFlyAddon islandFlyAddon, FlightTimeManager flightTimeManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.flightTimeManager = flightTimeManager;
    }

    /**
     * Disable player fly mode on logout
     * @param event Instance of PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLogout(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // Stop tracking flight time if temporary flight is used.
        if(flightTimeManager.isPlayerFlightTimeTracked(player.getUniqueId())) {
            flightTimeManager.stopTrackingPlayerFlightTime(player);
        }

        if (player.getAllowFlight() && this.islandFlyAddon.getSettings().isFlyDisableOnLogout()) {
            islandFlyAddon.logWarning("Disabling flight");
            // Disable fly
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }
}
