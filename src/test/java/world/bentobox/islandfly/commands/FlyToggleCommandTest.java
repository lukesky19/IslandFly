package world.bentobox.islandfly.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.eclipse.jdt.annotation.Nullable;
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

/**
 * @author tastybento
 */
@ExtendWith(MockitoExtension.class)
public class FlyToggleCommandTest {
    @Mock
    BentoBox plugin;
    @Mock
    private CompositeCommand compositeCommand;
    @Mock
    private User user;
    @Mock
    private IslandFlyAddon addon;
    @Mock
    private World world;
    @Mock
    private Player player;
    private FlyToggleCommand flyToggleCommand;
    @Mock
    private IslandsManager islandsManager;
    @Mock
    private @Nullable Location location;
    @Mock
    private Island island;
    private Settings settings;
    @Mock
    private BoundingBox boundingBox;

    MockedStatic<User> mockedUserClass;
    MockedStatic<Util> mockedUtilClass;

    @BeforeEach
    public void setUp() {
        // Set up plugin
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

        flyToggleCommand = new FlyToggleCommand(compositeCommand, addon);
    }

    @AfterEach
    public void cleanUp() {
        mockedUserClass.close();
        mockedUtilClass.close();
    }

    /**
     * Test method for {@link FlyToggleCommand#FlyToggleCommand(world.bentobox.bentobox.api.commands.CompositeCommand, IslandFlyAddon)}.
     */
    @Test
    public void testFlyToggleCommand() {
        assertEquals("fly", flyToggleCommand.getLabel());
    }

    /**
     * Test method for {@link FlyToggleCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("bskyblock.island.fly", flyToggleCommand.getPermission());
        assertEquals("islandfly.command.description", flyToggleCommand.getDescription());
        assertTrue(flyToggleCommand.isOnlyPlayer());
    }

    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteWrongWorld() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(Util.getWorld(user.getWorld())).thenReturn(mock(World.class));

        assertFalse(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.wrong-world");

    }
    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoIsland() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(any())).thenReturn(Optional.empty());

        assertFalse(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
    }
    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
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

        assertTrue(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
    }

    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
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

        assertFalse(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.not-allowed-fly");
    }
    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoFlagAllowedPermission() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(false);
        when(user.hasPermission(anyString())).thenReturn(true);

        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(true);

        assertTrue(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }
    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteFlagAllowed() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(island.getProtectionBoundingBox()).thenReturn(boundingBox);
        when(island.getProtectionBoundingBox().contains(user.getLocation().toVector())).thenReturn(true);

        assertTrue(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
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

        assertFalse(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user).sendMessage("islandfly.outside-protection-range");
    }

    /**
     * Test method for {@link FlyToggleCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOutsideProtectionRangeCommandAllowed() {
        when(compositeCommand.getWorld()).thenReturn(world);
        when(user.getLocation()).thenReturn(location);

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIslands()).thenReturn(islandsManager);
        Optional<Island> optionalIsland = Optional.of(island);
        when(islandsManager.getIslandAt(any())).thenReturn(optionalIsland);

        settings.setAllowCommandOutsideProtectionRange(true);
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        assertTrue(flyToggleCommand.canExecute(user, "fly", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    /**
     * Test method for {@link FlyToggleCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteDisableFlight() {
        when(user.getPlayer()).thenReturn(player);

        when(player.getAllowFlight()).thenReturn(true);
        flyToggleCommand.execute(user, "fly", Collections.emptyList());
        verify(player).setFlying(false);
        verify(player).setAllowFlight(false);
        verify(user).sendMessage("islandfly.disable-fly");
    }
    /**
     * Test method for {@link FlyToggleCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void textExecuteEnableFlight() {
        when(user.getPlayer()).thenReturn(player);

        flyToggleCommand.execute(user, "fly", Collections.emptyList());
        verify(player).setAllowFlight(true);
        verify(user).sendMessage("islandfly.enable-fly");
    }
}
