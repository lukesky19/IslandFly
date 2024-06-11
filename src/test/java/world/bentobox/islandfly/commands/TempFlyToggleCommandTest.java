package world.bentobox.islandfly.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
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
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TempFlyToggleCommandTest {
    @Mock
    BentoBox plugin;
    @Mock
    CompositeCommand compositeCommand;
    @Mock
    User user;
    @Mock
    IslandFlyAddon addon;
    @Mock
    World world;
    @Mock
    Player player;
    TempFlyToggleCommand tempFlyToggleCommand;
    @Mock
    IslandsManager islandsManager;
    @Mock
    Location location;
    @Mock
    Island island;
    Settings settings;
    @Mock
    BoundingBox boundingBox;
    @Mock
    FlightTimeManager flightTimeManager;

    MockedStatic<User> mockedUserClass;
    MockedStatic<Util> mockedUtilClass;

    UUID uuid;

    @BeforeEach
    public void setUp() {
        // Set Up Plugin
        User.setPlugin(plugin);

        // Addon
        when(compositeCommand.getPermissionPrefix()).thenReturn("bskyblock.");

        // User.class
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(player)).thenReturn(user);

        // Util.class
        mockedUtilClass = mockStatic(Util.class);
        mockedUtilClass.when(() -> Util.getWorld(user.getWorld())).thenReturn(world);

        // Settings
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        tempFlyToggleCommand = new TempFlyToggleCommand(compositeCommand, addon, flightTimeManager);
    }

    @AfterEach
    public void cleanUp() {
        mockedUserClass.close();
        mockedUtilClass.close();
    }

    @Test
    public void testTempFlyToggleCommand() {
        assertEquals("tempfly", tempFlyToggleCommand.getLabel());
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.island.tempfly", tempFlyToggleCommand.getPermission());
        assertEquals("islandfly.commands.player.tempfly.description", tempFlyToggleCommand.getDescription());
        assertTrue(tempFlyToggleCommand.isOnlyPlayer());
    }

    @Test
    public void testCanExecuteWrongWorld() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(Util.getWorld(user.getWorld())).thenReturn(mock(World.class));

        assertFalse(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.wrong-world");
    }

    @Test
    public void textCanExecuteNoIsland() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(any())).thenReturn(Optional.empty());

        assertFalse(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
    }

    @Test
    public void testCanExecuteSpawn() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isSpawn()).thenReturn(true);
        when(user.hasPermission(eq("bskyblock.island.flyspawn"))).thenReturn(true);

        assertTrue(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
    }

    @Test
    public void testCanExecuteNotAllowedFlagNoPermission() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(false);
        when(user.hasPermission(anyString())).thenReturn(false);

        assertFalse(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.island-not-allowed-fly");
    }

    @Test
    public void testCanExecuteNoFlagAllowedPermission() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(false);
        when(user.hasPermission(anyString())).thenReturn(true);

        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(true);

        when(flightTimeManager.getPlayerFlightData(uuid)).thenReturn(new IslandFlyPlayerData(uuid.toString(), 1));

        assertTrue(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    @Test
    public void testCanExecuteFlagAllowed() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(true);

        when(flightTimeManager.getPlayerFlightData(uuid)).thenReturn(new IslandFlyPlayerData(uuid.toString(), 1));

        assertTrue(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    @Test
    public void testCanExecuteOutsideProtectionRange() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(true);
        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(false);

        assertFalse(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.outside-protection-range");
    }

    @Test
    public void testCanExecuteOutsideProtectionRangeCommandAllowed() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        settings.setAllowCommandOutsideProtectionRange(true);
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(flightTimeManager.getPlayerFlightData(uuid)).thenReturn(new IslandFlyPlayerData(uuid.toString(), 1));

        assertTrue(tempFlyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    @Test
    public void testExecutePlayerHasNoFlightTime() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(true);

        when(flightTimeManager.getPlayerFlightData(uuid)).thenReturn(new IslandFlyPlayerData(uuid.toString(), 0));

        assertFalse(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.no-time-left");
    }

    @Test
    public void testExecutePlayerHasFlightTime() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(true);

        when(flightTimeManager.getPlayerFlightData(uuid)).thenReturn(new IslandFlyPlayerData(uuid.toString(), 1));

        assertTrue(tempFlyToggleCommand.canExecute(user, "tempfly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    @Test
    public void testExecuteDisableFlight() {
        when(user.getPlayer()).thenReturn(player);

        when(flightTimeManager.isPlayerFlightTimeTracked(any())).thenReturn(true);
        tempFlyToggleCommand.execute(user, "tempfly", Collections.emptyList());
        verify(player).setAllowFlight(false);
        verify(player).setFlying(false);
        verify(user).sendMessage("islandfly.disable-fly");
    }

    @Test
    public void testExecuteEnableFlight() {
        when(user.getPlayer()).thenReturn(player);

        when(flightTimeManager.isPlayerFlightTimeTracked(any())).thenReturn(false);
        tempFlyToggleCommand.execute(user, "tempfly", Collections.emptyList());
        verify(player).setAllowFlight(true);
        verify(user).sendMessage("islandfly.enable-fly");
    }

}
