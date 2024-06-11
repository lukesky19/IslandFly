package world.bentobox.islandfly.commands;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.List;

/**
 * This command allows players to view how much flight time they have.
 */
public class FlightTimePlayerCommand extends CompositeCommand {
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
     * @param parent Instance of CompositeCommand
     * @param addon Instance of IslandFlyAddon
     * @param flightTimeManager Instance of FlightTimeManager
     */
    public FlightTimePlayerCommand(CompositeCommand parent, IslandFlyAddon addon, FlightTimeManager flightTimeManager) {
        super(parent, "flighttime");
        this.addon = addon;
        this.flightTimeManager = flightTimeManager;
    }

    /**
     * Sets the permission and description for the command.
     * Sets the command as players only.
     */
    @Override
    public void setup() {
        this.setPermission("island.flighttime");
        this.setDescription("islandfly.commands.player.flighttime.description");
        this.setOnlyPlayer(true);
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
        // Get the user's flight data and verify it exists and is valid.
        // If not, just tell them it is 0 even though no data exists for them.
        IslandFlyPlayerData data = flightTimeManager.getPlayerFlightData(user.getUniqueId());
        if(data == null) {
            user.sendMessage("islandfly.commands.player.flighttime.flight-time", TextVariables.NUMBER, String.valueOf(0));
            return false;
        }

        // Send the user the amount of flight time they have.
        user.sendMessage("islandfly.commands.player.flighttime.flight-time", TextVariables.NUMBER, String.valueOf(data.getTimeSeconds()));
        return true;
    }
}
