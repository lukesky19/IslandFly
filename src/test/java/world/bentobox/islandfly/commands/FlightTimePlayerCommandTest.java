package world.bentobox.islandfly.commands;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlightTimePlayerCommandTest {
    @Mock
    BentoBox plugin;
    @Mock
    IslandFlyAddon addon;
    @Mock
    User user;
    @Mock
    CompositeCommand compositeCommand;
    @Mock
    Player player;
    @Mock
    FlightTimeManager flightTimeManager;

    MockedStatic<User> mockedUserClass;
    UUID uuid;
    FlightTimePlayerCommand flightTimePlayerCommand;

    @BeforeEach
    public void setUp() {
        // Setup Plugin
        User.setPlugin(plugin);

        // CompositeCommand
        when(compositeCommand.getAddon()).thenReturn(addon);
        when(compositeCommand.getPermissionPrefix()).thenReturn("bskyblock.");

        // User
        uuid = UUID.randomUUID();

        // User.class
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(player)).thenReturn(user);

        flightTimePlayerCommand = new FlightTimePlayerCommand(compositeCommand, addon, flightTimeManager);
    }

    @AfterEach
    public void cleanUp() {
        mockedUserClass.close();
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.island.flighttime", flightTimePlayerCommand.getPermission());
        assertEquals("islandfly.commands.player.flighttime.description", flightTimePlayerCommand.getDescription());
        assertTrue(flightTimePlayerCommand.isOnlyPlayer());
    }

    @Test
    public void testExecuteNoFlightTime() {
        when(user.getUniqueId()).thenReturn(uuid);
        when(flightTimeManager.getPlayerFlightData(user.getUniqueId())).thenReturn(null);

        flightTimePlayerCommand.execute(user, "flighttime", Collections.emptyList());
        verify(user).sendMessage("islandfly.commands.player.flighttime.flight-time", "[number]", "0");
    }

    @Test
    public void testExecuteHasFlightTime() {
        when(user.getUniqueId()).thenReturn(uuid);
        when(flightTimeManager.getPlayerFlightData(user.getUniqueId())).thenReturn(new IslandFlyPlayerData(uuid.toString(), 5));

        flightTimePlayerCommand.execute(user, "flighttime", Collections.emptyList());
        verify(user).sendMessage("islandfly.commands.player.flighttime.flight-time", "[number]", "5");
    }

}
