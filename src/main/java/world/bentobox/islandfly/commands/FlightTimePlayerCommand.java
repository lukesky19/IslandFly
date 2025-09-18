package world.bentobox.islandfly.commands;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.PlayerDataManager;
import world.bentobox.islandfly.util.FormatUtil;

import java.util.List;
import java.util.UUID;

/**
 * This command allows players to view how much flight time they have.
 */
public class FlightTimePlayerCommand extends CompositeCommand {
    /**
     * Instance of FlightTimeManager
     */
    private final PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param parent Instance of CompositeCommand
     * @param playerDataManager Instance of FlightTimeManager
     */
    public FlightTimePlayerCommand(CompositeCommand parent, PlayerDataManager playerDataManager) {
        super(parent, "flighttime");
        this.playerDataManager = playerDataManager;
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
        UUID uuid = user.getUniqueId();
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(uuid);

        // Send the user the amount of flight time they have.
        user.sendMessage("islandfly.commands.player.flighttime.flight-time", TextVariables.NUMBER, FormatUtil.formatTimeSeconds(islandFlyPlayerData.getTimeSeconds()));

        return true;
    }
}
