package world.bentobox.islandfly.commands;

import org.bukkit.entity.Player;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.List;
import java.util.Objects;

/**
 * This command allows to enable and disable the temporary flight mode.
 */
public class TempFlyToggleCommand extends CompositeCommand {
    /**
     * Instance of IslandFlyAddon
     */
    final IslandFlyAddon addon;
    /**
     * Instance of FlightTimeManager
     */
    final FlightTimeManager flightTimeManager;
    /**
     * Instance of IslandFly settings.
     */
    final Settings settings;

    /**
     * Constructor
     * @param parent Instance of CompositeCommand
     * @param addon Instance of IslandFlyAddon
     * @param flightTimeManager Instance of FlightTimeManager
     */
    public TempFlyToggleCommand(CompositeCommand parent, IslandFlyAddon addon, FlightTimeManager flightTimeManager) {
        super(parent, "tempfly");
        this.addon = addon;
        this.settings = addon.getSettings();
        this.flightTimeManager = flightTimeManager;
    }

    /**
     * Sets the permission and description for the command.
     * Sets the command as players only.
     */
    @Override
    public void setup() {
        this.setPermission("island.tempfly");
        this.setDescription("islandfly.commands.player.tempfly.description");
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

        // Checks if the user has no flight data or flight time equal to 0.
        IslandFlyPlayerData data = flightTimeManager.getPlayerFlightData(user.getUniqueId());
        if(Objects.equals(data, null)
                || data.getTimeSeconds() == 0) {
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

        if(flightTimeManager.isPlayerFlightTimeTracked(player.getUniqueId())) {
            flightTimeManager.stopTrackingPlayerFlightTime(player);
            player.setAllowFlight(false);
            player.setFlying(false);
            user.sendMessage("islandfly.disable-fly");
        } else {
            flightTimeManager.trackPlayerFlightTime(player);
            player.setAllowFlight(true);
            user.sendMessage("islandfly.enable-fly");
        }
        return true;
    }
}
