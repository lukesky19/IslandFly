package world.bentobox.islandfly.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.PlayerDataManager;
import world.bentobox.islandfly.util.FormatUtil;

import java.util.List;

/**
 * This command allows admins to set, add, remove, delete, and get a player's flight time.
 */
public class FlightTimeAdminCommand extends CompositeCommand {
    private final IslandFlyAddon addon;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param parent Instance of CompositeCommand
     * @param addon Instance of IslandFlyAddon
     * @param playerDataManager Instance of PlayerDataManager
     */
    public FlightTimeAdminCommand(CompositeCommand parent, IslandFlyAddon addon, @NotNull PlayerDataManager playerDataManager) {
        super(parent, "flighttime");
        this.addon = addon;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Sets the permission and description for the command.
     */
    @Override
    public void setup() {
        this.setPermission("admin.flighttime");
        this.setDescription("islandfly.commands.admin.flighttime.description");
    }

    /**
     * Logic ran when the command is sent.
     * @param user The User who sent the command.
     * @param label The label for the command sent.
     * @param args The arguments sent with the command.
     * @return <code>true</code> if the command succeeds, <code>false</code> if not.
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Arg 1 is the subcommand
        // Arg 2 is a player's username
        // Arg 3 is a time
        if(args.size() == 3) {
            switch(args.get(0)) {
                case "add" -> {
                    // Verify player is valid.
                    Player player = addon.getServer().getPlayer(args.get(1));
                    if(player == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-player");
                        return false;
                    }

                    // Get BentoBox User for that player.
                    User targetUser = User.getInstance(player);

                    // Get the IslandFlyPlayerData for the player
                    IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

                    // Verify the time provided is valid.
                    Integer timeSeconds = parseInteger(args.get(2));
                    if(timeSeconds == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-time");
                        return false;
                    }

                    // Add the flight time to the player.
                    islandFlyPlayerData.addTimeSeconds(timeSeconds);
                    // Send the command sender that the flight time was added successfully.
                    user.sendMessage("islandfly.commands.admin.flighttime.add-success", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));
                    // Send the target user that their flight time was changed with the updated number.
                    targetUser.sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));

                    playerDataManager.savePlayerData(islandFlyPlayerData);

                    return true;
                }

                case "set" -> {
                    // Verify player is valid.
                    Player player = addon.getServer().getPlayer(args.get(1));
                    if(player == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-player");
                        return false;
                    }

                    // Get BentoBox User for that player.
                    User targetUser = User.getInstance(player);

                    // Get the IslandFlyPlayerData for the player
                    IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

                    // Verify the time provided is valid.
                    Integer timeSeconds = parseInteger(args.get(2));
                    if(timeSeconds == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-time");
                        return false;
                    }

                    // Set the flight time for the player.
                    islandFlyPlayerData.setTimeSeconds(timeSeconds);
                    // Send the command sender that the flight time was set successfully.
                    user.sendMessage("islandfly.commands.admin.flighttime.set-success", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));
                    // Send the target user that their flight time was changed with the updated number.
                    targetUser.sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));

                    playerDataManager.savePlayerData(islandFlyPlayerData);

                    return true;
                }

                case "remove" -> {
                    // Verify player is valid.
                    Player player = addon.getServer().getPlayer(args.get(1));
                    if(player == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-player");
                        return false;
                    }

                    // Get BentoBox User for that player.
                    User targetUser = User.getInstance(player);

                    // Get the IslandFlyPlayerData for the player
                    IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

                    // Verify the time provided is valid.
                    Integer timeSeconds = parseInteger(args.get(2));
                    if(timeSeconds == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-time");
                        return false;
                    }

                    // Remove the flight time from the player.
                    islandFlyPlayerData.removeTimeSeconds(timeSeconds);
                    // Send the command sender that the flight time was removed successfully.
                    user.sendMessage("islandfly.commands.admin.flighttime.remove-success", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));
                    // Send the target user that their flight time was changed with the updated number.
                    targetUser.sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));

                    playerDataManager.savePlayerData(islandFlyPlayerData);

                    return true;
                }
            }
        } else if(args.size() == 2) {
            switch(args.get(0)) {
                case "get" -> {
                    // Verify player is valid.
                    Player player = addon.getServer().getPlayer(args.get(1));
                    if(player == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-player");
                        return false;
                    }

                    // Verify flight data exists for the player.
                    IslandFlyPlayerData data = playerDataManager.getPlayerFlightData(player.getUniqueId());

                    // Send the command sender the flight time for the requested player.
                    user.sendMessage("islandfly.commands.admin.flighttime.flight-time", TextVariables.NUMBER, FormatUtil.formatTimeSeconds(data.getTimeSeconds()));
                    return true;
                }

                case "delete" -> {
                    // Verify player is valid.
                    Player player = addon.getServer().getPlayer(args.get(1));
                    if(player == null) {
                        user.sendMessage("islandfly.commands.admin.flighttime.invalid-player");
                        return false;
                    }

                    // Get BentoBox User for that player.
                    User targetUser = User.getInstance(player);

                    // Get the IslandFlyPlayerData for the player
                    IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(player.getUniqueId());

                    // Remove any flight time for the player.
                    islandFlyPlayerData.setTimeSeconds(0);
                    // Tell the command sender the flight time for the player was deleted.
                    user.sendMessage("islandfly.commands.admin.flighttime.delete-success");
                    // Send the target user that their flight time was changed with the updated number.
                    targetUser.sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));

                    playerDataManager.savePlayerData(islandFlyPlayerData);

                    return true;
                }
            }
        } else {
            // When the number of arguments is incorrect, send the sender the correct syntax to use.
            user.sendMessage("islandfly.commands.admin.flighttime.invalid-command-syntax");
            user.sendMessage("islandfly.commands.admin.flighttime.syntax");
            return false;
        }

        return false;
    }

    /**
     * Parses a String to return an Integer.
     * @param string The string containing a number to parse
     * @return an Integer or null
     */
    private @Nullable Integer parseInteger(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
