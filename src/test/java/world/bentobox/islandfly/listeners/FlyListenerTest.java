package world.bentobox.islandfly.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * @author tastybento
 *
 */
@ExtendWith(MockitoExtension.class)
public class FlyListenerTest {
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandFlyAddon addon;

    private FlyListener fl;
    @Mock
    private IslandsManager im;
    @Mock
    private User user;

    @Mock
    private Island island;
    @Mock
    private Island island2;
    private Optional<Island> opIsland2;
    @Mock
    private Player p;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private World world;
    @Mock
    private GameModeAddon gameMode;
    private UUID uuid;
    @Mock
    private BukkitScheduler sch;
    @Mock
    private Settings settings;
    @Mock
    FlightTimeManager flightTimeManager;

    MockedStatic<User> mockedUserClass;
    MockedStatic<Bukkit> mockedBukkitClass;

    @BeforeEach
    public void setUp() {
        // User
        uuid = UUID.randomUUID();
        when(p.getUniqueId()).thenReturn(uuid);
        User.setPlugin(plugin);
        User.getInstance(p);
        // Bukkit
        mockedBukkitClass = mockStatic(Bukkit.class);
        mockedBukkitClass.when(Bukkit::getScheduler).thenReturn(sch);
        // User
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(uuid)).thenReturn(user);

        fl = new FlyListener(addon, flightTimeManager);
    }

    @AfterEach
    public void tearDown() {
        User.clearUsers();
        mockedUserClass.close();
        mockedBukkitClass.close();
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandGraceTime() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);

        when(user.isOp()).thenReturn(false);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);

        when(user.hasPermission(anyString())).thenAnswer(invocation -> {
            String permission = invocation.getArgument(0, String.class);
            if(permission.equals("bskyblock.island.fly")
                    || permission.equals("bskyblock.island.flyspawn")) {
                return true;
            }
            return false;
        });

        when(settings.getFlyTimeout()).thenReturn(5);
        when(addon.getSettings()).thenReturn(settings);

        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);
        fl.onExitIsland(event);
        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
        verify(user).sendMessage("islandfly.fly-outside-alert", "[number]", "5");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandGraceTimeOp() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        // User/Player
        when(user.isOp()).thenReturn(true);

        // IslandExitEvent
        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);

        // Actual test
        fl.onExitIsland(event);
        verify(sch, never()).runTaskLater(eq(plugin), any(Runnable.class), any(Long.class));
        verify(p, never()).sendMessage(anyString());
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandGraceTimePermission() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        // User/Player
        when(user.isOp()).thenReturn(false);
        when(user.getPlayer()).thenReturn(p);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);

        // IslandExitEvent
        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);

        fl.onExitIsland(event);
        verify(sch, never()).runTaskLater(eq(plugin), any(Runnable.class), any(Long.class));
        verify(p, never()).sendMessage(anyString());
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandGraceTimeNotFlying() {
        when(user.getPlayer()).thenReturn(p);

        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);

        when(user.isOp()).thenReturn(false);

        when(user.hasPermission(anyString())).thenAnswer(invocation -> {
            String permission = invocation.getArgument(0, String.class);
            if(permission.equals("bskyblock.island.fly")
                    || permission.equals("bskyblock.island.flyspawn")) {
                return true;
            }
            return false;
        });

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(5);

        when(p.isFlying()).thenReturn(false);
        when(user.getWorld()).thenReturn(world);

        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);
        fl.onExitIsland(event);


        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
        verify(p, never()).sendMessage("islandfly.fly-outside-alert");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandNoGraceTime() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        // Island
        when(addon.getIslands()).thenReturn(im);
        when(im.getProtectedIslandAt(any())).thenReturn(Optional.empty());

        // User/Player
        when(user.isOnline()).thenReturn(true);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(p.isFlying()).thenReturn(true);

        when(user.hasPermission(anyString())).thenAnswer(invocation -> {
            String permission = invocation.getArgument(0, String.class);
            if(permission.equals("bskyblock.island.fly")
                    || permission.equals("bskyblock.island.flyspawn")) {
                return true;
            }
            return false;
        });

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(0);

        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);


        fl.onExitIsland(event);
        verify(sch, never()).runTaskLater(eq(plugin), any(Runnable.class), any(Long.class));
        verify(user).sendMessage("islandfly.disable-fly");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandNoPermission() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        // User/Player
        when(user.getPlayer()).thenReturn(p);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);

        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);

        fl.onExitIsland(event);
        verify(sch, never()).runTaskLater(eq(plugin), any(Runnable.class), any(Long.class));
        verify(p, never()).sendMessage("islandfly.disable-fly");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#onExitIsland(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnExitIslandNoGraceTimeCreativeOrSpectator() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        when(user.getPlayer()).thenReturn(p);
        when(p.getGameMode()).thenReturn(GameMode.CREATIVE);

        IslandExitEvent event = mock(IslandExitEvent.class);
        when(event.getPlayerUUID()).thenReturn(uuid);
        fl.onExitIsland(event);
        // Spectator
        when(p.getGameMode()).thenReturn(GameMode.SPECTATOR);
        fl.onExitIsland(event);
        verify(sch, never()).runTaskLater(eq(plugin), any(Runnable.class), any(Long.class));
        verify(p, never()).sendMessage("islandfly.disable-fly");
    }


    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingNotOnline() {
        when(user.isOnline()).thenReturn(false);
        assertFalse(fl.removeFly(user));

    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingOutsideProtectedIsland() {
        // User/Player
        when(user.isOnline()).thenReturn(true);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);

        // Island
        // If a player is flying outside an island into unowned space, then they should have their fly removed
        when(addon.getIslands()).thenReturn(im);
        when(im.getProtectedIslandAt(any())).thenReturn(Optional.empty());

        assertTrue(fl.removeFly(user));
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
        verify(user).sendMessage("islandfly.disable-fly");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingBackInProtectedAreaOfIsland() {
        assertFalse(fl.removeFly(user));
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingInOtherIslandNotAllowed() {
        // Island
        when(addon.getIslands()).thenReturn(im);
        opIsland2 = Optional.of(island2);
        when(im.getProtectedIslandAt(any())).thenReturn(opIsland2);

        // User/Player
        when(user.getPlayer()).thenReturn(p);
        when(user.isOnline()).thenReturn(true);
        when(p.isFlying()).thenReturn(true);

        // Settings
        when(addon.getSettings()).thenReturn(settings);

        assertTrue(fl.removeFly(user));
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
        verify(user).sendMessage("islandfly.disable-fly");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingInOtherProtectedIslandAllowed() {
        // Island
        when(addon.getIslands()).thenReturn(im);
        opIsland2 = Optional.of(island2);
        when(im.getProtectedIslandAt(any())).thenReturn(opIsland2);
        when(island2.isAllowed(any(), any())).thenReturn(true);

        // Settings
        when(addon.getSettings()).thenReturn(settings);

        // User
        when(user.isOnline()).thenReturn(true);

        assertFalse(fl.removeFly(user));
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingInOwnProtectedIslandNotAllowed() {
        // User/Player
        when(user.isOnline()).thenReturn(true);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);

        // Island
        when(addon.getIslands()).thenReturn(im);
        Optional<Island> opIsland = Optional.of(island);
        when(im.getProtectedIslandAt(any())).thenReturn(opIsland);

        // Settings
        when(addon.getSettings()).thenReturn(settings);

        assertTrue(fl.removeFly(user));
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
        verify(user).sendMessage("islandfly.disable-fly");
    }


    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingInSpawnAllowed() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        Optional<GameModeAddon> opGm = Optional.of(gameMode);
        when(gameMode.getPermissionPrefix()).thenReturn("bskyblock.");
        when(iwm.getAddon(any())).thenReturn(opGm);

        // User/Player
        when(user.isOnline()).thenReturn(true);
        when(user.hasPermission(anyString())).thenReturn(true);

        // Island
        when(addon.getIslands()).thenReturn(im);
        Optional<Island> opIsland = Optional.of(island);
        when(im.getProtectedIslandAt(any())).thenReturn(opIsland);
        when(island.isSpawn()).thenReturn(true);


        assertFalse(fl.removeFly(user));
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyListener#removeFly(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveFlyUserFlyingInSpawnNotAllowed() {
        // IWM
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        Optional<GameModeAddon> opGm = Optional.of(gameMode);
        when(gameMode.getPermissionPrefix()).thenReturn("bskyblock.");
        when(iwm.getAddon(any())).thenReturn(opGm);

        // Island
        when(addon.getIslands()).thenReturn(im);
        Optional<Island> opIsland = Optional.of(island);
        when(im.getProtectedIslandAt(any())).thenReturn(opIsland);
        when(island.isSpawn()).thenReturn(true);

        // User/Player
        when(user.isOnline()).thenReturn(true);
        when(user.hasPermission("bskyblock.island.flyspawn")).thenReturn(false);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);


        assertTrue(fl.removeFly(user));
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
        verify(user).sendMessage("islandfly.disable-fly");
    }

}
