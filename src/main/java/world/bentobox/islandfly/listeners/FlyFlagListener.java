package world.bentobox.islandfly.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.flags.FlagProtectionChangeEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * This class disables flight when the island fly flag is changed.
 */
public class FlyFlagListener implements Listener {
    /**
     * Instance of IslandFlyAddon
     */
    final IslandFlyAddon addon;
    /**
     * Instance of FlightTimeManager
     */
    final FlightTimeManager flightTimeManager;

    /**
     * Constructor
     * @param addon Instance of IslandFlyAddon
     * @param flightTimeManager Instance of FlightTimeManager
     */
    public FlyFlagListener(IslandFlyAddon addon, FlightTimeManager flightTimeManager) {
        this.addon = addon;
        this.flightTimeManager = flightTimeManager;
    }

    /**
     * Finds all players who are flying when the Island Fly Protection flag is changed and
     * disables their flight if necessary.
     * @param e Instance of FlagProtectionChangeEvent.
     */
    @EventHandler
    public void onFlagChange(FlagProtectionChangeEvent e) {
        // Only continue if the flag changed is for island fly.
        if(!e.getEditedFlag().equals(IslandFlyAddon.ISLAND_FLY_PROTECTION)) return;

        // Get the island that the flag was changed for.
        Island island = e.getIsland();

        // Stream through all of the flying and not allowed users at
        // the moment and warn them that their fly is about to turn off
        island.getPlayersOnIsland()
        .stream()
        //.parallelStream()
        .filter(Player::isFlying)
        .filter(p -> !p.isOp())
        .filter(p -> !(island.isAllowed(User.getInstance(p), IslandFlyAddon.ISLAND_FLY_PROTECTION)))
        .forEach(p -> startDisabling(p, island));
    }

    /**
     * Tells the player that their flight will be disabled based on the
     * fly timeout setting and schedules their flight to be disabled.
     * @param p The player to disable flight for.
     * @param island The island the player is on.
     */
    private void startDisabling(Player p, Island island) {
        int flyTimeout = this.addon.getSettings().getFlyTimeout();
        User user = User.getInstance(p);

        // Alert player fly will be disabled.
        user.sendMessage("islandfly.fly-turning-off-alert", TextVariables.NUMBER, String.valueOf(flyTimeout));

        // If timeout is 0 or less, disable fly immediately.
        if (flyTimeout <= 0) {
            // Stop tracking flight time.
            if(flightTimeManager.isPlayerFlightTimeTracked(user.getUniqueId())) {
                flightTimeManager.stopTrackingPlayerFlightTime(p);
            }

            // Disable player's flight.
            p.setFlying(false);
            p.setAllowFlight(false);
            // Send message to the user that their flight was disabled.
            user.sendMessage("islandfly.disable-fly");
            return;
        }

        // Else disable fly with a delay
        Bukkit.getScheduler().runTaskLater(this.addon.getPlugin(), () -> disable(p, user, island), 20L* flyTimeout);
    }

    /**
     * Disable flight if the player hasn't logged out, changed islands, and hasn't been allowed to fly again.
     * @param player The player to disable flight for.
     * @param user The BentoBox User associated with the Player.
     * @param island The island the player is on that the island fly flag was changed on.
     */
    void disable(Player player, User user, Island island) {
        // Verify that player is still online
        if(!user.isOnline()) return;

        // Check if user was reallowed to fly in the meantime
        if(!island.isAllowed(user,IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
            // Silent cancel fly if player changed island in the meantime
            // It will be the job of Enter/Exit island event to turn fly off if required
            if(!island.onIsland(player.getLocation())) return;

            // Stop tracking flight time.
            if(flightTimeManager.isPlayerFlightTimeTracked(user.getUniqueId())) {
                flightTimeManager.stopTrackingPlayerFlightTime(player);
            }

            // Disable player's flight.
            player.setFlying(false);
            player.setAllowFlight(false);
            // Send a message to the user that their flight was disabled.
            user.sendMessage("islandfly.disable-fly");
        }
        else {
            // Send a message to the user that they were reallowed flight.
            user.sendMessage("islandfly.reallowed-fly");
        }
    }
}