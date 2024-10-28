package world.bentobox.islandfly.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * This class disables fly mode if player quits server.
 */
public class FlyLoginListener implements Listener {
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
    public FlyLoginListener(IslandFlyAddon islandFlyAddon, FlightTimeManager flightTimeManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.flightTimeManager = flightTimeManager;
    }

    /**
     * Enable player fly mode on login
     * @param event Instance of PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLogin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final User user = User.getInstance(player);
        final String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(player.getWorld());

        // Only enable flight on login if the player is in a gamemode world.
        if(!islandFlyAddon.getPlugin().getIWM().getWorlds().contains(user.getWorld())) return;

        if (player.hasPermission(permPrefix + "island.fly")
                && player.hasPermission(permPrefix + "island.tempfly")
                || player.hasPermission(permPrefix + "island.fly")) {
            if (!this.islandFlyAddon.getSettings().isFlyDisableOnLogout()
                    && isInAir(player)
                    && islandFlyAddon.getIslands().userIsOnIsland(user.getWorld(), user)
                    && !islandFlyAddon.getIslands().getIslandAt(user.getLocation()).map(i -> {
                        if (islandFlyAddon.getSettings().getFlyMinLevel() > 1 && islandFlyAddon.getLevelAddon() != null) {
                            if (islandFlyAddon.getLevelAddon().getIslandLevel(i.getWorld(), i.getOwner()) < islandFlyAddon.getSettings().getFlyMinLevel()) {
                                user.sendMessage("islandfly.fly-min-level-alert", TextVariables.NUMBER, String.valueOf(islandFlyAddon.getSettings().getFlyMinLevel()));
                                return false;
                            }
                        }
                        if (i.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
                            // Enable fly
                            player.setFallDistance(0);
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            user.sendMessage("islandfly.enable-fly");
                            return true;
                        }
                        return false;
                    }).orElse(false)) {
                user.sendMessage("islandfly.not-allowed-fly");
            }
        } else if(player.hasPermission(permPrefix + "island.tempfly")) {
            if (!this.islandFlyAddon.getSettings().isFlyDisableOnLogout()
                    && isInAir(player)
                    && islandFlyAddon.getIslands().userIsOnIsland(user.getWorld(), user)
                    && !islandFlyAddon.getIslands().getIslandAt(user.getLocation()).map(i -> {
                        if (islandFlyAddon.getSettings().getFlyMinLevel() > 1 && islandFlyAddon.getLevelAddon() != null) {
                            if (islandFlyAddon.getLevelAddon().getIslandLevel(i.getWorld(), i.getOwner()) < islandFlyAddon.getSettings().getFlyMinLevel()) {
                                user.sendMessage("islandfly.fly-min-level-alert", TextVariables.NUMBER, String.valueOf(islandFlyAddon.getSettings().getFlyMinLevel()));
                                return false;
                            }
                        }
                        if (i.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)) {
                            // Enable fly
                            flightTimeManager.trackPlayerFlightTime(player);
                            player.setFallDistance(0);
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            user.sendMessage("islandfly.enable-fly");
                            return true;
                        }
                        return false;
                    }).orElse(false)) {
                user.sendMessage("islandfly.not-allowed-fly");
            }
        } else {
            user.sendMessage("islandfly.not-allowed-fly");
        }
    }

    public boolean isInAir(Player player) {
        Block b = player.getLocation().getBlock();
        return player.getLocation().getBlockY() > (player.getWorld().getMinHeight() + 1) && b.getRelative(BlockFace.DOWN).isEmpty() && b.getRelative(BlockFace.DOWN, 2).isEmpty();
    }
}
