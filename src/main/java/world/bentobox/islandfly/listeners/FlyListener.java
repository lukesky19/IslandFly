package world.bentobox.islandfly.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.FlightValidationManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

import java.util.Optional;

/**
 * This class listens to a variety of events to check if flight should be enabled or disabled.
 */
public class FlyListener implements Listener {
    private final @NotNull IslandFlyAddon islandFlyAddon;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;
    private final @NotNull FlightValidationManager flightValidationManager;

    /**
     * Constructor
     * @param islandFlyAddon An {@link IslandFlyAddon} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     * @param flightValidationManager A {@link FlightValidationManager} instance.
     */
    public FlyListener(
            @NotNull IslandFlyAddon islandFlyAddon,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull BossBarManager bossBarManager,
            @NotNull FlightValidationManager flightValidationManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
        this.flightValidationManager = flightValidationManager;
    }

    /**
     * Listens for when a player enters an island and checks if flight should be automatically enabled.
     * @param islandEnterEvent An {@link IslandEnterEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnterIsland(IslandEnterEvent islandEnterEvent) {
        User user = User.getInstance(islandEnterEvent.getPlayerUUID());
        Island island = islandEnterEvent.getIsland();

        // Wait until player is on the Island
        islandFlyAddon.getServer().getScheduler().runTaskLater(islandFlyAddon.getPlugin(), () -> {
            if(checkEnableFly(user, island)) {
                enableFlight(user);
            }
        }, 1L);
    }

    /**
     * Listens for when a player exits an island and checks if flight should be automatically enabled.
     * @param islandExitEvent An {@link IslandExitEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExitIsland(IslandExitEvent islandExitEvent) {
        User user = User.getInstance(islandExitEvent.getPlayerUUID());

        // Wait until player is on the Island
        islandFlyAddon.getServer().getScheduler().runTaskLater(islandFlyAddon.getPlugin(), () -> {
            if(checkRemoveFly(user)) {
                removeFly(user);
            }
        }, 1L);
    }

    /**
     * Checks if flight can be enabled for the user.
     * @param user The {@link User} to check.
     * @param island The {@link Island} the user is on.
     * @return true if flight can be enabled, otherwise false.
     */
    public boolean checkEnableFly(@NotNull User user, @NotNull Island island) {
        // Check if the player already has flight enabled in general
        if(flightValidationManager.isPlayerFlightEnabled(user.getPlayer())) {
            return false;
        }

        // Check if the user can fly on the island if it is a spawn island
        if(flightValidationManager.isIslandSpawnIsland(island) && !flightValidationManager.hasFlySpawnPermission(user)) {
            return false;
        }

        // Check if the user can fly on the island based on the island level.
        if(!flightValidationManager.canFlyIslandLevel(island)) {
            return false;
        }

        // Check if the user can fly on the island due to island settings/flags.
        if(!flightValidationManager.isFlyAllowed(island, user)) {
            return false;
        }

        // Check if the user has permission to fly or has permission for timed flight with flight time
        return flightValidationManager.hasFlyPermission(user)
                || (flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Checks if flight should be removed for the user.
     * @param user The {@link User} to check.
     * @return true if flight should be removed, otherwise false.
     */
    public boolean checkRemoveFly(@NotNull User user) {
        // Ignore users that are op
        if(flightValidationManager.isUserOp(user)) {
            return false;
        }

        // Ignore users in creative or spectator mode
        if(flightValidationManager.isUserCreativeOrSpectator(user)) {
            return false;
        }

        // Ignore users that have the fly bypass permission
        if(flightValidationManager.hasFlyBypassPermission(user)) {
            return false;
        }

        // Fly should be removed if the user isn't on an island
        Optional<Island> optionalIsland = islandFlyAddon.getIslands().getIslandAt(user.getLocation());
        if(optionalIsland.isEmpty()) return true;
        Island island = optionalIsland.get();

        // Fly should be removed if the island is a spawn island and the user doesn't have the fly spawn permission
        if(flightValidationManager.isIslandSpawnIsland(island) && !flightValidationManager.hasFlySpawnPermission(user)) {
            return true;
        }

        // Fly should be removed if the island doesn't meet the minimum island levels to fly.
        if(!flightValidationManager.canFlyIslandLevel(island)) {
            return true;
        }

        // Fly should be removed if the user can't fly on the island due to island settings/flags.
        if(!flightValidationManager.isFlyAllowed(island, user)) {
            return true;
        }

        // Fly should be removed if the user doesn't have the fly permission, doesn't have the timed fly permissions, or has no flight time.
        return !flightValidationManager.hasFlyPermission(user)
                && (!flightValidationManager.hasTimedFlyPermission(user) || !flightValidationManager.hasFlightTime(user));
    }

    /**
     * Disable fly with a delay if in a BentoBox managed world, otherwise disable immediately.
     * @param user The {@link User} to remove fly for.
     */
    public void removeFly(User user) {
        if(flightValidationManager.isUserInGameModeWorld(user)) {
            int flyTimeout = this.islandFlyAddon.getSettings().getFlyTimeout();

            // Notify the user that their flight will be disabled after the delay
            if(user.getPlayer().isFlying()) {
                user.sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(flyTimeout));
            }

            // Queue the disabling of the user's flight
            islandFlyAddon.getServer().getScheduler().runTaskLater(this.islandFlyAddon.getPlugin(), () -> disableFly(user), 20L * flyTimeout);
        } else {
            // Disable fly immediately.
            disableFly(user);
        }
    }

    /**
     * Disable the user's flight and notify them if they are actively flying.
     * @param user {@link User} to disable flight for.
     */
    public void disableFly(@NotNull User user) {
        // If the user is no longer online, do nothing
        if(!user.isOnline()) return;
        // Get the Player from the User
        Player player = user.getPlayer();
        // Get the player's IslandFlyPlayerData
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        // If the user is flying, send a message that their flight is being disabled.
        if(player.isFlying()) {
            user.sendMessage("islandfly.disable-fly");
        }

        // Disable flight booleans in the player's island fly player data
        islandFlyPlayerData.setNormalFlight(false);
        if(islandFlyPlayerData.isTimedFlightEnabled()) {
            islandFlyPlayerData.setTimedFlight(false);

            // Save flight time if timed flight was enabled
            playerDataManager.savePlayerData(islandFlyPlayerData);
        }

        // Remove the boss bar
        bossBarManager.removeBossBar(player);

        // Disable flight
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    /**
     * Enable the user's flight and notify them of the change.
     * @param user The {@link User} to enable flight for.
     * @return true if flight is enabled, or false if not.
     */
    public boolean enableFlight(User user) {
        // Get the Player from the User
        Player player = user.getPlayer();
        // Get the player's IslandFlyPlayerData
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        if(flightValidationManager.hasFlyPermission(user)) {
            // Enable Normal Flight
            islandFlyPlayerData.setNormalFlight(true);
            player.setAllowFlight(true);

            // Disable timed flight, save player data, and remove the boss bar if timed flight is enabled
            if(islandFlyPlayerData.isTimedFlightEnabled()) {
                islandFlyPlayerData.setTimedFlight(false);

                // Save flight time if timed flight was enabled
                playerDataManager.savePlayerData(islandFlyPlayerData);

                // Remove the boss bar
                bossBarManager.removeBossBar(player);
            }

            // Show Boss Bar
            bossBarManager.addIslandFlyBossBar(player);

            // Notify Player
            user.sendMessage("islandfly.enable-fly");

            return true;
        } else if(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user)) {
            // Disable normal flight
            islandFlyPlayerData.setNormalFlight(false);

            // Enable Timed Flight
            islandFlyPlayerData.setTimedFlight(true);
            player.setAllowFlight(true);

            // Show Boss Bar
            bossBarManager.addFlightTimeBossBar(player);

            // Notify Player
            user.sendMessage("islandfly.enable-fly");

            return true;
        }

        return false;
    }
}