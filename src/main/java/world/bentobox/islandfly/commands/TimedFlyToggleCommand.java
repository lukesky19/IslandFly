package world.bentobox.islandfly.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

import java.util.List;
import java.util.UUID;

/**
 * This command allows to enable and disable the temporary flight mode.
 */
public class TimedFlyToggleCommand extends CompositeCommand {
    /**
     * Instance of IslandFlyAddon
     */
    final IslandFlyAddon addon;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;
    /**
     * Instance of IslandFly settings.
     */
    final Settings settings;

    /**
     * Constructor
     * @param parent Instance of CompositeCommand
     * @param addon Instance of IslandFlyAddon
     * @param playerDataManager Instance of PlayerDataManager
     */
    public TimedFlyToggleCommand(CompositeCommand parent, IslandFlyAddon addon, @NotNull PlayerDataManager playerDataManager, @NotNull BossBarManager bossBarManager) {
        super(parent, "timedfly", "tempfly");
        this.addon = addon;
        this.settings = addon.getSettings();
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

    /**
     * Sets the permission and description for the command.
     * Sets the command as players only.
     */
    @Override
    public void setup() {
        this.setPermission("island.timedfly");
        this.setDescription("islandfly.commands.player.timedfly.description");
        this.setOnlyPlayer(true);
    }

    /**
     * Logic to test if the command can be executed.
     * @param user The User who sent the command.
     * @param label The label for the command sent.
     * @param args The arguments sent with the command.
     * @return <code>true</code> if the command can be executed, <code>false</code> if not.
     */
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Checks world from corresponding gamemode command with the world player is executing in
        if (this.getWorld() != Util.getWorld(user.getWorld())) {
            user.sendMessage("islandfly.wrong-world");
            return false;
        }

        // Gets the island at User's location and check if it is null.
        Island island = addon.getPlugin().getIslands().getIslandAt(user.getLocation()).orElse(null);
        if(island == null) return false;

        // Enable fly if island is a spawn and user has permission for it.
        if(island.isSpawn() && user.hasPermission(this.getPermissionPrefix() + "island.flyspawn")) {
            return true;
        }

        // Checks if the User cannot use island fly based on island settings and
        // that they don't have the permission [gamemode].island.flybypass
        if(!island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)
                && !user.hasPermission(this.getPermissionPrefix() + "island.flybypass")) {
            user.sendMessage("islandfly.island-not-allowed-fly");
            return false;
        }

        // Checks the addon's settings to see if a User can use commands outside the Island
        // protection range and if the User is inside the protection range.
        if(!this.settings.isAllowCommandOutsideProtectionRange()
                && !island.getProtectionBoundingBox().contains(user.getLocation().toVector())) {
            user.sendMessage("islandfly.outside-protection-range");
            return false;
        }

        // Checks if the min fly level is greater than 1 and the level addon is used.
        if(addon.getSettings().getFlyMinLevel() > 1 && addon.getLevelAddon() != null) {
            // Checks if the User's island level is less than the min fly level setting.
            if(addon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner()) < addon.getSettings().getFlyMinLevel()) {
                user.sendMessage("islandfly.fly-min-level-alert", TextVariables.NUMBER, String.valueOf(addon.getSettings().getFlyMinLevel()));
                return false;
            }
        }

        // Checks if the user has flight time equal to 0.
        IslandFlyPlayerData data = playerDataManager.getPlayerFlightData(user.getUniqueId());
        if(data.getTimeSeconds() == 0) {
            user.sendMessage("islandfly.no-time-left");
            return false;
        }

        return true;
    }

    /**
     * Logic ran when the command is executed.
     * @param user The User who sent the command.
     * @param label The label for the command sent.
     * @param args The arguments sent with the command.
     * @return <code>true</code> if the command succeeds, <code>false</code> if not.
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        final Player player = user.getPlayer();
        UUID uuid = player.getUniqueId();
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(uuid);

        if(islandFlyPlayerData.isTimedFlightEnabled()) {
            // Disable Fly
            islandFlyPlayerData.setNormalFlight(false);
            islandFlyPlayerData.setTimedFlight(false);
            player.setAllowFlight(false);
            player.setFlying(false);

            // Remove Boss Bar
            bossBarManager.removeBossBar(player);

            // Notify Player
            user.sendMessage("islandfly.disable-fly");
        } else {
            // Enable Fly
            islandFlyPlayerData.setNormalFlight(false);
            islandFlyPlayerData.setTimedFlight(true);
            player.setAllowFlight(true);

            // Show Boss Bar
            bossBarManager.addFlightTimeBossBar(player);

            // Notify Player
            user.sendMessage("islandfly.enable-fly");
        }

        return true;
    }
}
