package world.bentobox.islandfly.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.events.flags.FlagProtectionChangeEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

/**
 * This class disables flight when the island fly flag is changed.
 */
public class FlyFlagListener implements Listener {
    private final @NotNull IslandFlyAddon islandFlyAddon;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;

    /**
     * Constructor.
     * @param islandFlyAddon An {@link IslandFlyAddon} instance.
     * @param playerDataManager  A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     */
    public FlyFlagListener(
            @NotNull IslandFlyAddon islandFlyAddon,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull BossBarManager bossBarManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

    /**
     * When Island Fly Protection flag is changed, disable any players flight if necessary.
     * @param flagProtectionChangeEvent A {@link FlagProtectionChangeEvent}.
     */
    @EventHandler
    public void onFlagChange(FlagProtectionChangeEvent flagProtectionChangeEvent) {
        // Only continue if the flag changed is for island fly.
        if(!flagProtectionChangeEvent.getEditedFlag().equals(IslandFlyAddon.ISLAND_FLY_PROTECTION)) return;

        // Get the island that the flag was changed for.
        Island island = flagProtectionChangeEvent.getIsland();

        // Stream through users that can fly that are no longer allowed to fly
        // and warn them that their fly is about to turn off
        island.getPlayersOnIsland()
                .stream()
                //.parallelStream()
                .filter(Player::getAllowFlight)
                .filter(player -> {
                    IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());
                    return player.isFlying() || (islandFlyPlayerData.isNormalFlightEnabled() || islandFlyPlayerData.isTimedFlightEnabled());
                })
                .filter(p -> !p.isOp())
                .filter(p -> !(island.isAllowed(User.getInstance(p), IslandFlyAddon.ISLAND_FLY_PROTECTION)))
                .forEach(player -> startDisabling(player, island));
    }

    /**
     * Tells the player that their flight will be disabled based on the
     * fly timeout setting and schedules their flight to be disabled.
     * @param player The player to disable flight for.
     * @param island The island the player is on.
     */
    private void startDisabling(Player player, Island island) {
        int flyTimeout = this.islandFlyAddon.getSettings().getFlyTimeout();
        User user = User.getInstance(player);
        // Get the player's IslandFlyPlayerData
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        // Alert player fly will be disabled.
        user.sendMessage("islandfly.fly-turning-off-alert", TextVariables.NUMBER, String.valueOf(flyTimeout));

        // If timeout is 0 or less, disable fly immediately.
        if(flyTimeout <= 0) {
            // Disable player's flight.
            islandFlyPlayerData.setNormalFlight(false);
            islandFlyPlayerData.setTimedFlight(false);
            player.setFlying(false);
            player.setAllowFlight(false);

            // Remove the boss bar
            bossBarManager.removeBossBar(player);

            // Send a message to the user that their flight was disabled.
            user.sendMessage("islandfly.disable-fly");
            return;
        }

        // Else disable fly with a delay
        Bukkit.getScheduler().runTaskLater(this.islandFlyAddon.getPlugin(), () -> disable(player, user, island), 20L* flyTimeout);
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
        // Get the player's IslandFlyPlayerData
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        // Check if user was reallowed to fly in the meantime
        if(!island.isAllowed(user,IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
            // Silent cancel fly if player changed island in the meantime
            // It will be the job of Enter/Exit island event to turn fly off if required
            if(!island.onIsland(player.getLocation())) return;

            // Disable player's flight.
            islandFlyPlayerData.setNormalFlight(false);
            islandFlyPlayerData.setTimedFlight(false);
            player.setFlying(false);
            player.setAllowFlight(false);

            // Remove the boss bar
            bossBarManager.removeBossBar(player);

            // Send a message to the user that their flight was disabled.
            user.sendMessage("islandfly.disable-fly");
        }
        else {
            // Send a message to the user that they were reallowed flight.
            user.sendMessage("islandfly.reallowed-fly");
        }
    }
}