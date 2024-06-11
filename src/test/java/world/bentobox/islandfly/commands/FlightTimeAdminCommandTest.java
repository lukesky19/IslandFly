package world.bentobox.islandfly.commands;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlightTimeAdminCommandTest {
    @Mock
    BentoBox plugin;
    @Mock
    IslandFlyAddon addon;
    @Mock
    Server server;
    @Mock
    User senderUser;
    @Mock
    User targetUser;
    @Mock
    CompositeCommand compositeCommand;
    @Mock
    Player player;
    UUID senderUuid;
    UUID targetUuid;
    @Mock
    FlightTimeManager flightTimeManager;
    FlightTimeAdminCommand flightTimeAdminCommand;

    MockedStatic<User> mockedUserClass;

    @BeforeEach
    public void setUp() {
        // Setup Plugin
        User.setPlugin(plugin);

        // CompositeCommand
        when(compositeCommand.getAddon()).thenReturn(addon);
        when(compositeCommand.getPermissionPrefix()).thenReturn("bskyblock.");

        // Sender
        senderUuid = UUID.randomUUID();

        // Target
        targetUuid = UUID.randomUUID();

        // User.class
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(player)).thenReturn(targetUser);

        flightTimeAdminCommand = new FlightTimeAdminCommand(compositeCommand, addon, flightTimeManager);
    }

    @AfterEach
    public void cleanUp() {
        mockedUserClass.close();
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.admin.flighttime", flightTimeAdminCommand.getPermission());
        assertEquals("islandfly.commands.admin.flighttime.description", flightTimeAdminCommand.getDescription());
    }

    @Test
    public void testSetFlightTimeCommand() {
        assertEquals("flighttime", flightTimeAdminCommand.getLabel());
    }

    @Test
    public void testAddPlayerFlightTimeInvalidPlayer() {
        when(addon.getServer()).thenReturn(server);

        List<String> args = Arrays.asList("add", null, String.valueOf(1));
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-player");
    }

    @Test
    public void testAddPlayerFlightTimeInvalidTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        //mockStatic(User.class).when(() -> User.getInstance(player)).thenReturn(targetUser);

        List<String> args = Arrays.asList("add", player.getName(), "egg");
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-time");
    }

    @Test
    public void testAddPlayerFlightTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        when(flightTimeManager.addPlayerFlightTime(player, 1)).thenReturn(70);
        List<String> args = Arrays.asList("add", player.getName(), String.valueOf(1));
        assertTrue(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.add-success", TextVariables.NUMBER, String.valueOf(70));
        verify(targetUser).sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(70));
    }

    @Test
    public void testSetPlayerFlightTimeInvalidPlayer() {
        when(addon.getServer()).thenReturn(server);

        List<String> args = Arrays.asList("set", null, String.valueOf(1));
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-player");
    }

    @Test
    public void testSetPlayerFlightTimeInvalidTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        List<String> args = Arrays.asList("set", player.getName(), "egg");
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-time");
    }

    @Test
    public void testSetPlayerFlightTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        when(flightTimeManager.setPlayerFlightTime(player, 1)).thenReturn(1);
        List<String> args = Arrays.asList("set", player.getName(), String.valueOf(1));
        assertTrue(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.set-success", TextVariables.NUMBER, String.valueOf(1));
        verify(targetUser).sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(1));
    }

    @Test
    public void testRemovePlayerFlightTimeInvalidPlayer() {
        when(addon.getServer()).thenReturn(server);

        List<String> args = Arrays.asList("remove", null, String.valueOf(1));
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-player");
    }

    @Test
    public void testRemovePlayerFlightTimeInvalidTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        List<String> args = Arrays.asList("remove", player.getName(), "egg");
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-time");
    }

    @Test
    public void testRemovePlayerFlightTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        when(flightTimeManager.removePlayerFlightTime(player, 1)).thenReturn(68);
        List<String> args = Arrays.asList("remove", player.getName(), String.valueOf(1));
        assertTrue(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.remove-success", TextVariables.NUMBER, String.valueOf(68));
        verify(targetUser).sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(68));
    }

    @Test
    public void testDeletePlayerFlightTimeInvalidPlayer() {
        when(addon.getServer()).thenReturn(server);

        List<String> args = Arrays.asList("delete", null);
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-player");
    }

    @Test
    public void testDeletePlayerFlightTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        when(flightTimeManager.getPlayerFlightData(player.getUniqueId())).thenReturn(new IslandFlyPlayerData(targetUuid.toString(), 69));

        when(flightTimeManager.deletePlayerFlightData(player)).thenReturn(0);
        List<String> args = Arrays.asList("delete", player.getName());
        assertTrue(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.delete-success");
        verify(targetUser).sendMessage("islandfly.flight-time-changed", TextVariables.NUMBER, String.valueOf(0));
    }

    @Test
    public void testGetPlayerFlightTimeInvalidPlayer() {
        when(addon.getServer()).thenReturn(server);

        List<String> args = Arrays.asList("get", null);
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-player");
    }

    @Test
    public void testGetPlayerFlightTimeNoFlightData() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        when(flightTimeManager.getPlayerFlightData(player.getUniqueId())).thenReturn(null);

        List<String> args = Arrays.asList("get", player.getName());
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.no-flight-data");
    }

    @Test
    public void testGetPlayerFlightTime() {
        when(addon.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("lukeskywlker19");
        when(addon.getServer().getPlayer(anyString())).thenReturn(player);

        when(flightTimeManager.getPlayerFlightData(player.getUniqueId())).thenReturn(new IslandFlyPlayerData(targetUuid.toString(), 69));

        List<String> args = Arrays.asList("get", player.getName());
        assertTrue(flightTimeAdminCommand.execute(senderUser, "flighttime", args));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.flight-time", TextVariables.NUMBER, String.valueOf(69));
    }

    @Test
    public void testInvalidCommandSyntax() {
        assertFalse(flightTimeAdminCommand.execute(senderUser, "flighttime", Collections.emptyList()));
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.invalid-command-syntax");
        verify(senderUser).sendMessage("islandfly.commands.admin.flighttime.syntax");
    }
}
