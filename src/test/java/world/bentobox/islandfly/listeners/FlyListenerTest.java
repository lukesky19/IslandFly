package world.bentobox.islandfly.listeners;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.managers.FlightCheckManager;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * @author tastybento
 * @author lukeskywlker19
 */
@ExtendWith(MockitoExtension.class)
public class FlyListenerTest {
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandFlyAddon addon;

    private FlyListener fl;
    @Mock
    private User user;

    @Mock
    private Island island;
    @Mock
    private Player p;
    @Mock
    private UUID uuid;
    @Mock
    Server server;
    @Mock
    private BukkitScheduler sch;
    @Mock
    private Settings settings;
    @Mock
    private FlightTimeManager flightTimeManager;
    @Mock
    private FlightCheckManager flightCheckManager;

    MockedStatic<User> mockedUserClass;

    @BeforeEach
    public void setUp() {
        // User
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(uuid)).thenReturn(user);

        fl = new FlyListener(addon, flightTimeManager, flightCheckManager);
    }

    @AfterEach
    public void tearDown() {
        User.clearUsers();
        mockedUserClass.close();
    }

    @Test
    public void testOnEnterIsland() {
        IslandEnterEvent event = mock(IslandEnterEvent.class);

        when(event.getPlayerUUID()).thenReturn(uuid);
        when(event.getIsland()).thenReturn(island);

        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(sch);

        fl.onEnterIsland(event);

        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), eq(1L));
    }

    @Test
    public void testOnExitIsland() {
        IslandExitEvent event = mock(IslandExitEvent.class);

        when(event.getPlayerUUID()).thenReturn(uuid);

        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(sch);

        fl.onExitIsland(event);

        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), eq(1L));
    }

    @Test
    public void testCheckEnableFlyPlayerFlying() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.isPlayerAllowedFlight(p)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertTrue(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyPlayerNotFlying() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlySpawnIslandCanFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(true);
        when(flightCheckManager.canUserFlySpawn(user)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertTrue(flightCheckManager.isIslandSpawnIsland(island));
        assertTrue(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));

    }

    @Test
    public void testCheckEnableFlySpawnIslandCanNotFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertTrue(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyNotSpawnIslandCanFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlySpawn(user)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertTrue(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyNotSpawnIslandCanNotFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyCanFlyIslandLevel() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyCannotFlyIslandLevel() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyCanFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);
        when(flightCheckManager.canUserFlyOnIsland(island, user)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertTrue(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyCannotFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyUseFlyAndTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);
        when(flightCheckManager.canUserFlyOnIsland(island, user)).thenReturn(true);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.canUserUseTempFly(user)).thenReturn(true);

        assertTrue(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertTrue(flightCheckManager.canUserFlyOnIsland(island, user));
        assertTrue(flightCheckManager.canUserUseFly(user));
        assertTrue(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyUseFly() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);
        when(flightCheckManager.canUserFlyOnIsland(island, user)).thenReturn(true);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);

        assertTrue(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertTrue(flightCheckManager.canUserFlyOnIsland(island, user));
        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyUseTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);
        when(flightCheckManager.canUserFlyOnIsland(island, user)).thenReturn(true);
        when(flightCheckManager.canUserUseTempFly(user)).thenReturn(true);

        assertTrue(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertTrue(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertTrue(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckEnableFlyCannotUseFlyAndCannotUseTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);
        when(flightCheckManager.canUserFlyOnIsland(island, user)).thenReturn(true);

        assertFalse(fl.checkEnableFly(user, island));
        assertFalse(flightCheckManager.isPlayerAllowedFlight(p));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertTrue(flightCheckManager.canUserFlyOnIsland(island, user));
        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testEnableFlightFly() {
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);

        assertTrue(fl.enableFlight(user));
        verify(p).setAllowFlight(true);
        verify(user).sendMessage("islandfly.enable-fly");
    }

    @Test
    public void testEnableFlightTempFly() {
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserUseTempFly(user)).thenReturn(true);

        assertTrue(fl.enableFlight(user));
        verify(p).setAllowFlight(true);
        verify(user).sendMessage("islandfly.enable-fly");
        verify(flightTimeManager).trackPlayerFlightTime(p);
    }

    @Test
    public void testEnableFlightFlyAndTempFly() {
        when(user.getPlayer()).thenReturn(p);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.canUserUseTempFly(user)).thenReturn(true);

        assertTrue(fl.enableFlight(user));
        verify(p).setAllowFlight(true);
        verify(user).sendMessage("islandfly.enable-fly");
        verify(flightTimeManager, never()).trackPlayerFlightTime(p);
    }

    @Test
    public void testEnableFlightNoFly() {
        when(user.getPlayer()).thenReturn(p);

        assertFalse(fl.enableFlight(user));
        verify(p, never()).setAllowFlight(true);
        verify(user, never()).sendMessage("islandfly.enable-fly");
        verify(flightTimeManager, never()).trackPlayerFlightTime(p);
    }

    @Test
    public void testCheckRemoveFlyUserOp() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(flightCheckManager.isUserOp(user)).thenReturn(true);

        assertFalse(fl.checkRemoveFly(user));
        assertTrue(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));
    }

    @Test
    public void testCheckRemoveFlyUserCreativeOrSpectator() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(flightCheckManager.isUserCreativeOrSpectator(user)).thenReturn(true);

        assertFalse(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertTrue(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));
    }

    @Test
    public void testCheckRemoveFlyUserCanBypassFly() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(flightCheckManager.canUserBypassFly(user)).thenReturn(true);

        assertFalse(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertTrue(flightCheckManager.canUserBypassFly(user));
    }

    @Test
    public void testCheckRemoveFlyNotOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(null);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertNull(flightCheckManager.getIslandUserIsOn(user));
    }

    @Test
    public void testCheckRemoveFlyCannotFlyAndCannotTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertFalse(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckRemoveFlyCanFlyAndCannotTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckRemoveFlyCannotFlyAndCanTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseTempFly(user)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertFalse(flightCheckManager.canUserUseFly(user));
        assertTrue(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckRemoveFlyCanFlyAndCanTempFly() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.canUserUseTempFly(user)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertTrue(flightCheckManager.canUserUseFly(user));
        assertTrue(flightCheckManager.canUserUseTempFly(user));
    }

    @Test
    public void testCheckRemoveFlySpawnIslandCannotFly() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
        assertTrue(flightCheckManager.isIslandSpawnIsland(island));
        assertFalse(flightCheckManager.canUserFlySpawn(user));
    }

    @Test
    public void testCheckRemoveFlySpawnIslandCanFly() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(true);
        when(flightCheckManager.canUserFlySpawn(user)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
        assertTrue(flightCheckManager.isIslandSpawnIsland(island));
        assertTrue(flightCheckManager.canUserFlySpawn(user));
    }

    @Test
    public void testCheckRemoveFlyNotSpawnIslandCanFly() {
        when(User.getInstance(uuid)).thenReturn(user);
        
        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.canUserFlySpawn(user)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
        assertFalse(flightCheckManager.isIslandSpawnIsland(island));
        assertTrue(flightCheckManager.canUserFlySpawn(user));
    }

    @Test
    public void testCheckRemoveFlyUserCannotFlyIslandLevel() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(false);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertNotNull(flightCheckManager.getIslandUserIsOn(user));
        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
        assertFalse(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
    }

    @Test
    public void testCheckRemoveFlyUserCannotFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(false);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);

        assertTrue(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertNotNull(flightCheckManager.getIslandUserIsOn(user));
        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertFalse(flightCheckManager.canUserFlyOnIsland(island, user));
    }

    @Test
    public void testCheckRemoveFlyUserCanFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(flightCheckManager.getIslandUserIsOn(user)).thenReturn(island);
        when(flightCheckManager.canUserUseFly(user)).thenReturn(true);
        when(flightCheckManager.isIslandSpawnIsland(island)).thenReturn(false);
        when(flightCheckManager.canUserFlyIslandLevel(island)).thenReturn(true);
        when(flightCheckManager.canUserFlyOnIsland(island, user)).thenReturn(true);

        assertFalse(fl.checkRemoveFly(user));
        assertFalse(flightCheckManager.isUserOp(user));
        assertFalse(flightCheckManager.isUserCreativeOrSpectator(user));
        assertFalse(flightCheckManager.canUserBypassFly(user));

        assertNotNull(flightCheckManager.getIslandUserIsOn(user));
        assertTrue(flightCheckManager.canUserUseFly(user));
        assertFalse(flightCheckManager.canUserUseTempFly(user));
        assertTrue(flightCheckManager.canUserFlyIslandLevel(island));
        assertTrue(flightCheckManager.canUserFlyOnIsland(island, user));
    }

    @Test
    public void testRemoveFlyPlayerInGamemodeWorldFlying() {
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(5);
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(sch);

        when(flightCheckManager.isUserWorldGamemodeWorld(user)).thenReturn(true);

        fl.removeFly(user);

        verify(user).sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(5));
        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
    }

    @Test
    public void testRemoveFlyPlayerInGamemodeWorldNotFlying() {
        when(user.getPlayer()).thenReturn(p);

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(5);
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(sch);

        when(flightCheckManager.isUserWorldGamemodeWorld(user)).thenReturn(true);

        fl.removeFly(user);

        verify(user, never()).sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(5));
        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
    }

    @Test
    public void testRemoveFlyPlayerNotInGamemodeWorld() {
        when(user.getPlayer()).thenReturn(p);
        when(user.isOnline()).thenReturn(true);

        fl.removeFly(user);

        verify(user, never()).sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(5));
        verify(sch, never()).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));

        verify(user, never()).sendMessage("islandfly.disable-fly");
        assertFalse(flightTimeManager.isPlayerFlightTimeTracked(uuid));
        verify(flightTimeManager, never()).stopTrackingPlayerFlightTime(p);
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
    }

    @Test
    public void testDisableFlyNormalFlyUserOnlineFlying() {
        when(user.isOnline()).thenReturn(true);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);

        fl.disableFly(user);

        verify(user).sendMessage("islandfly.disable-fly");
        verify(flightTimeManager, never()).stopTrackingPlayerFlightTime(p);
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
    }

    @Test
    public void testDisableFlyNormalFlyUserOnlineNotFlying() {
        when(user.isOnline()).thenReturn(true);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);

        fl.disableFly(user);

        verify(user, never()).sendMessage("islandfly.disable-fly");
        verify(flightTimeManager, never()).stopTrackingPlayerFlightTime(p);
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
    }

    @Test
    public void testDisableFlyTempFlyUserOnlineFlying() {
        when(user.isOnline()).thenReturn(true);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(p.isFlying()).thenReturn(true);

        when(flightTimeManager.isPlayerFlightTimeTracked(uuid)).thenReturn(true);

        fl.disableFly(user);

        verify(user).sendMessage("islandfly.disable-fly");
        verify(flightTimeManager).stopTrackingPlayerFlightTime(p);
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
    }

    @Test
    public void testDisableFlyTempFlyUserOnlineNotFlying() {
        when(user.isOnline()).thenReturn(true);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);

        when(flightTimeManager.isPlayerFlightTimeTracked(uuid)).thenReturn(true);

        fl.disableFly(user);

        verify(user, never()).sendMessage("islandfly.disable-fly");
        verify(flightTimeManager).stopTrackingPlayerFlightTime(p);
        verify(p).setFlying(false);
        verify(p).setAllowFlight(false);
    }

    @Test
    public void testDisableFlyUserOffline() {
        fl.disableFly(user);

        verify(user, never()).sendMessage("islandfly.disable-fly");
        verify(flightTimeManager, never()).isPlayerFlightTimeTracked(uuid);
        verify(flightTimeManager, never()).stopTrackingPlayerFlightTime(p);
        verify(p, never()).setFlying(false);
        verify(p, never()).setAllowFlight(false);
    }
}
