package world.bentobox.islandfly.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player joins the server then loads player data and checks if flight should be enabled and enables it if necessary.
 */
public class FlyLoginListener implements Listener {
    private final @NotNull IslandFlyAddon islandFlyAddon;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;

    /**
     * Constructor.
     * @param islandFlyAddon An {@link IslandFlyAddon} instance.
     * @param playerDataManager  A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     */
    public FlyLoginListener(
            @NotNull IslandFlyAddon islandFlyAddon,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull BossBarManager bossBarManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

    /**
     * Loads player data and checks if flight should be enabled and enables it if necessary.
     * @param playerJoinEvent Instance of PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLogin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        User user = User.getInstance(player);
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(player.getWorld());

        // Load player data
        playerDataManager.loadPlayerData(uuid);
        // Get player data
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

        // Only enable flight on login if the player is in a gamemode world.
        if(!islandFlyAddon.getPlugin().getIWM().getWorlds().contains(user.getWorld())) return;

        if(player.hasPermission(permPrefix + "island.fly")) {
            if(islandFlyAddon.getSettings().isFlyDisableOnLogout()
                    || !isInAir(player)
                    || !islandFlyAddon.getIslands().userIsOnIsland(user.getWorld(), user)) return;

            Optional<Island> optionalIsland = islandFlyAddon.getIslands().getIslandAt(user.getLocation());
            if(optionalIsland.isEmpty()) return;
            Island island = optionalIsland.get();

            if(islandFlyAddon.getSettings().getFlyMinLevel() > 1 && islandFlyAddon.getLevelAddon() != null) {
                if(islandFlyAddon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner()) < islandFlyAddon.getSettings().getFlyMinLevel()) {
                    user.sendMessage("islandfly.fly-min-level-alert", TextVariables.NUMBER, String.valueOf(islandFlyAddon.getSettings().getFlyMinLevel()));
                    return;
                }
            }

            if(island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
                // Enable flight
                player.setFallDistance(0);
                islandFlyPlayerData.setNormalFlight(true);
                player.setAllowFlight(true);
                player.setFlying(true);

                // Show Boss Bar
                bossBarManager.addIslandFlyBossBar(player);

                // Notify Player
                user.sendMessage("islandfly.enable-fly");

                return;
            }

            user.sendMessage("islandfly.not-allowed-fly");
        } else if(player.hasPermission(permPrefix + "island.timedfly")) {
            if(islandFlyAddon.getSettings().isFlyDisableOnLogout()
                    || !isInAir(player)
                    || !islandFlyAddon.getIslands().userIsOnIsland(user.getWorld(), user)) return;

            // No Flight time
            if(islandFlyPlayerData.getTimeSeconds() <= 0) return;

            Optional<Island> optionalIsland = islandFlyAddon.getIslands().getIslandAt(user.getLocation());
            if(optionalIsland.isEmpty()) return;
            Island island = optionalIsland.get();

            if(islandFlyAddon.getSettings().getFlyMinLevel() > 1 && islandFlyAddon.getLevelAddon() != null) {
                if(islandFlyAddon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner()) < islandFlyAddon.getSettings().getFlyMinLevel()) {
                    user.sendMessage("islandfly.fly-min-level-alert", TextVariables.NUMBER, String.valueOf(islandFlyAddon.getSettings().getFlyMinLevel()));
                    return;
                }
            }

            if(island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
                // Enable flight
                player.setFallDistance(0);
                islandFlyPlayerData.setTimedFlight(true);
                player.setAllowFlight(true);
                player.setFlying(true);

                // Show Boss Bar
                bossBarManager.addFlightTimeBossBar(player);

                // Notify Player
                user.sendMessage("islandfly.enable-fly");

                return;
            }

            user.sendMessage("islandfly.not-allowed-fly");
        } else {
            user.sendMessage("islandfly.not-allowed-fly");
        }
    }

    /**
     * Check if the player is in the air.
     * @param player The {@link Player} to check.
     * @return true if the player is in the air, or false.
     */
    public boolean isInAir(Player player) {
        Block block = player.getLocation().getBlock();
        return player.getLocation().getBlockY() > (player.getWorld().getMinHeight() + 1) && block.getRelative(BlockFace.DOWN).isEmpty() && block.getRelative(BlockFace.DOWN, 2).isEmpty();
    }
}
