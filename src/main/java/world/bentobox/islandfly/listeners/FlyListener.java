package world.bentobox.islandfly.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * This class manages players fly ability.
 */
public class FlyListener implements Listener {
    /**
     * Addon instance object.
     */
    final IslandFlyAddon islandFlyAddon;
    /**
     * Instance of FlightTimeManager
     */
    final FlightTimeManager flightTimeManager;

    /**
     * Constructor
     * @param islandFlyAddon Instance of IslandFlyAddon
     * @param flightTimeManager Instance of FlightTimeManager
     */
    public FlyListener(final IslandFlyAddon islandFlyAddon, final FlightTimeManager flightTimeManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.flightTimeManager = flightTimeManager;
    }

    /**
     * When flight is toggled, check if the user can fly, otherwise disable flight.
     * @param event Instance of PlayerToggleFlightEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onToggleFlight(final PlayerToggleFlightEvent event) {
        final User user = User.getInstance(event.getPlayer());
        if(checkUser(user)) {
            user.sendMessage("islandfly.not-allowed");
        }
    }

    /**
     * Don't disable flight if user is op, in creative or spectator, or has [gamemode].island.flybypass.
     * Otherwise, call {@link #removeFly(User)} for additional checks.
     * @param user The BentoBox User to check if they can fly.
     * @return <code>true</code> if fly was blocked, otherwise <code>false</code>
     */
    private boolean checkUser(User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        // Ignore ops
        if(user.isOp() || user.getPlayer().getGameMode().equals(GameMode.CREATIVE)
                || user.getPlayer().getGameMode().equals(GameMode.SPECTATOR)
                || user.hasPermission(permPrefix + "island.flybypass")) return false;
        return removeFly(user);
    }

    /**
     * When entering an island, check if the user can fly, otherwise disable flight.
     * @param event Instance of IslandEnterEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnterIsland(final IslandEnterEvent event) {
        final User user = User.getInstance(event.getPlayerUUID());
        // Wait until after arriving at the island
        Bukkit.getScheduler().runTask(this.islandFlyAddon.getPlugin(), () -> checkUser(user));
    }

    /**
     * When exiting an island, check if the user can fly, otherwise disable flight.
     * @param event instance of IslandEvent.IslandExitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExitIsland(final IslandExitEvent event) {
        final User user = User.getInstance(event.getPlayerUUID());
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        // Ignore ops
        if(user.isOp() || user.getPlayer().getGameMode().equals(GameMode.CREATIVE)
                || user.getPlayer().getGameMode().equals(GameMode.SPECTATOR)
                || user.hasPermission(permPrefix + "island.flybypass")
                || ((!user.hasPermission(permPrefix + "island.fly") && !user.hasPermission(permPrefix + "island.tempfly"))
                        && !user.hasPermission(permPrefix + "island.flyspawn"))) {
            return;
        }
        // Alert player fly will be disabled
        final int flyTimeout = this.islandFlyAddon.getSettings().getFlyTimeout();

        // If timeout is 0 or less disable fly immediately
        if(flyTimeout <= 0) {
            removeFly(user);
            return;
        }

        // Else disable fly with a delay
        if(user.getPlayer().isFlying()) {
            user.sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(flyTimeout));
        }

        Bukkit.getScheduler().runTaskLater(this.islandFlyAddon.getPlugin(), () -> removeFly(user), 20L* flyTimeout);
    }


    /**
     * Remove fly from a player if required.
     * @param user The BentoBox User to check.
     * @return <code>true</code> if fly is removed, otherwise <code>false</code>.
     */
    boolean removeFly(User user) {
        // Verify player is still online
        if (!user.isOnline()) return false;

        // Disable flight if the island is not on an island.
        Island island = islandFlyAddon.getIslands().getProtectedIslandAt(user.getLocation()).orElse(null);
        if(island == null) {
            disableFly(user);
            return true;
        }

        // Check if player is back on a spawn island
        if(island.isSpawn()) {
            if(this.islandFlyAddon.getPlugin().getIWM().getAddon(user.getWorld())
                    .map(a -> !user.hasPermission(a.getPermissionPrefix() + "island.flyspawn")).orElse(false)) {
                disableFly(user);
                return true;
            }
            return false;
        }

        // Disable flight if the island doesn't meet the minimum island level to fly.
        if(islandFlyAddon.getSettings().getFlyMinLevel() > 1 && islandFlyAddon.getLevelAddon() != null) {
            if(islandFlyAddon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner()) < islandFlyAddon.getSettings().getFlyMinLevel()) {
                disableFly(user);
                return false;
            }
        }

        // Check if player is allowed to fly on the island he is at that moment
        if(!island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
            disableFly(user);
            return true;
        }
        return false;
    }



    /**
     * Disable player fly and alert it.
     * @param user The BentoBox User to disable flight for.
     */
    private void disableFly(final User user) {
        final Player player = user.getPlayer();
        // If the user is flying, send a message that their flight is being disabled.
        if(player.isFlying())
            user.sendMessage("islandfly.disable-fly");

        // If player is using temporary flight, stop tracking their flight time.
        if(flightTimeManager.isPlayerFlightTimeTracked(user.getUniqueId())) {
            flightTimeManager.stopTrackingPlayerFlightTime(player);
        }

        // Disable flight
        player.setFlying(false);
        player.setAllowFlight(false);
    }
}
