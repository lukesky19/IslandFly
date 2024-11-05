package world.bentobox.islandfly.listeners;

import net.ess3.api.events.FlyStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.managers.FlightCheckManager;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * This class manages players fly ability.
 */
public class FlyListener implements Listener {
    /**
     * Addon instance object.
     */
    private final IslandFlyAddon islandFlyAddon;
    /**
     * Instance of FlightTimeManager
     */
    private final FlightTimeManager flightTimeManager;
    private final FlightCheckManager flightCheckManager;

    /**
     * Constructor
     *
     * @param islandFlyAddon    Instance of IslandFlyAddon
     * @param flightTimeManager Instance of FlightTimeManager
     */
    public FlyListener(
            final IslandFlyAddon islandFlyAddon,
            final FlightTimeManager flightTimeManager,
            final FlightCheckManager flightCheckManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.flightTimeManager = flightTimeManager;
        this.flightCheckManager = flightCheckManager;
    }

    /**
     * Event to handle cases of other fly plugins toggling flight
     * Mostly ensures temporary flight tracking is disabled in such cases.
     * @param event A PlayerToggleFlightEvent
     */
    @EventHandler
    public void onFlyToggle(FlyStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        System.out.println("FlyStatusChangeEvent");

        if(!event.getValue()) {
            System.out.println("Player Not Allowed Fly | FlyStatusChangeEvent");
            if(flightTimeManager.isPlayerFlightTimeTracked(player.getUniqueId())) {
                System.out.println("Stopping Flight Tracking | FlyStatusChangeEvent");
                flightTimeManager.stopTrackingPlayerFlightTime(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnterIsland(final IslandEnterEvent event) {
        final User user = User.getInstance(event.getPlayerUUID());
        final Island island = event.getIsland();

        // Wait until player is on the Island
        islandFlyAddon.getServer().getScheduler().runTaskLater(islandFlyAddon.getPlugin(), () -> {
            if(checkEnableFly(user, island)) {
                enableFlight(user);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExitIsland(final IslandExitEvent event) {
        final User user = User.getInstance(event.getPlayerUUID());

        // Wait until player is on the Island
        islandFlyAddon.getServer().getScheduler().runTaskLater(islandFlyAddon.getPlugin(), () -> {
            if(checkRemoveFly(user)) {
                removeFly(user);
            }
        }, 1L);
    }

    public boolean checkEnableFly(User user, Island island) {
        if (flightCheckManager.isPlayerAllowedFlight(user.getPlayer())) return false;

        if (flightCheckManager.isIslandSpawnIsland(island) && !flightCheckManager.canUserFlySpawn(user)) return false;

        if (!flightCheckManager.canUserFlyIslandLevel(island)) return false;

        if (!flightCheckManager.canUserFlyOnIsland(island, user)) return false;

        return flightCheckManager.canUserUseFly(user) || flightCheckManager.canUserUseTempFly(user);
    }

    public boolean checkRemoveFly(User user) {
        // Ignore if conditions are met
        if (flightCheckManager.isUserOp(user)) return false;

        if (flightCheckManager.isUserCreativeOrSpectator(user)) return false;

        if (flightCheckManager.canUserBypassFly(user)) return false;

        // Remove fly if conditions are met
        Island island = flightCheckManager.getIslandUserIsOn(user);
        if (island == null) return true;

        if (!flightCheckManager.canUserUseFly(user) && !flightCheckManager.canUserUseTempFly(user)) return true;

        if (flightCheckManager.isIslandSpawnIsland(island) && !flightCheckManager.canUserFlySpawn(user)) return true;

        if (!flightCheckManager.canUserFlyIslandLevel(island)) return true;

        return !flightCheckManager.canUserFlyOnIsland(island, user);
    }

    // Only do timeout if in a bentobox gamemode world, otherwise disable immediately.
    public void removeFly(User user) {
        if(flightCheckManager.isUserWorldGamemodeWorld(user)) {
            // Alert player fly will be disabled
            final int flyTimeout = this.islandFlyAddon.getSettings().getFlyTimeout();

            // Else disable fly with a delay
            if (user.getPlayer().isFlying()) {
                user.sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(flyTimeout));
            }

            islandFlyAddon.getServer().getScheduler().runTaskLater(this.islandFlyAddon.getPlugin(), () -> disableFly(user), 20L * flyTimeout);
        } else {
            // Else disable fly immediately.
            disableFly(user);
        }
    }

    /**
     * Disable player fly and alert it.
     *
     * @param user The BentoBox User to disable flight for.
     */
    public void disableFly(final User user) {
        if (!user.isOnline()) return;

        final Player player = user.getPlayer();
        // If the user is flying, send a message that their flight is being disabled.
        if (player.isFlying())
            user.sendMessage("islandfly.disable-fly");

        // If player is using temporary flight, stop tracking their flight time.
        if (flightTimeManager.isPlayerFlightTimeTracked(user.getUniqueId())) {
            flightTimeManager.stopTrackingPlayerFlightTime(player);
        }

        // Disable flight
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    public boolean enableFlight(User user) {
        final Player player = user.getPlayer();

        if(flightCheckManager.canUserUseFly(user)) {
            System.out.println("Regular Flight | enableFlight");
            player.setAllowFlight(true);
            user.sendMessage("islandfly.enable-fly");
            return true;
        } else if(flightCheckManager.canUserUseTempFly(user)) {
            System.out.println("Temp Flight | enableFlight");
            flightTimeManager.trackPlayerFlightTime(player);
            player.setAllowFlight(true);
            user.sendMessage("islandfly.enable-fly");
            return true;
        }

        return false;
    }
}