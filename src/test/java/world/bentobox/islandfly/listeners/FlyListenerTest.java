package world.bentobox.islandfly.listeners;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.FlightValidationManager;
import world.bentobox.islandfly.managers.PlayerDataManager;

/**
 * @author tastybento
 * @author lukeskywlker19
 */
@ExtendWith(MockitoExtension.class)
public class FlyListenerTest {
    // Plugin Mocks
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandsManager islandsManager;
    // Addon related Mocks
    @Mock
    private IslandFlyAddon addon;
    @Mock
    private Settings settings;
    @Mock
    private PlayerDataManager playerDataManager;
    @Mock
    private BossBarManager bossBarManager;
    @Mock
    private FlightValidationManager flightValidationManager;
    // Server related Mocks
    @Mock
    private Server server;
    @Mock
    private BukkitScheduler scheduler;
    // User/Player Mocks
    private MockedStatic<User> mockedUserClass;
    @Mock
    private User user;
    @Mock
    private Player player;
    @Mock
    private UUID uuid;
    // Island Mocks
    @Mock
    private Island island;
    // Class being tested
    private FlyListener flyListener;

    /**
     * Prepare the necessary data for each test.
     */
    @BeforeEach
    public void setUp() {
        // Setup User class
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(uuid)).thenReturn(user);

        // Create class instance to test
        flyListener = new FlyListener(addon, playerDataManager, bossBarManager, flightValidationManager);
    }

    /**
     * Cleanup any data from the tests.
     */
    @AfterEach
    public void tearDown() {
        User.clearUsers();
        mockedUserClass.close();
    }

    /**
     * Test a user entering an island and a task being scheduled to check if flight should be enabled.
     */
    @Test
    public void testOnEnterIsland() {
        IslandEnterEvent islandEnterEvent = mock(IslandEnterEvent.class);

        // Addon Stubs
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        // Event stubs
        when(islandEnterEvent.getPlayerUUID()).thenReturn(uuid);
        when(islandEnterEvent.getIsland()).thenReturn(island);

        // Call event
        flyListener.onEnterIsland(islandEnterEvent);

        // Verify a task was scheduled through the scheduler.
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), eq(1L));
    }

    /**
     * Test a user exiting an island and a task being scheduled to check if flight should be removed.
     */
    @Test
    public void testOnExitIsland() {
        IslandExitEvent islandExitEvent = mock(IslandExitEvent.class);

        // Addon Stubs
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        // Event stubs
        when(islandExitEvent.getPlayerUUID()).thenReturn(uuid);

        // Call event
        flyListener.onExitIsland(islandExitEvent);

        // Verify a task was scheduled through the scheduler.
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), eq(1L));
    }

    /**
     * Test checking if flight should be enabled, but the player is already flying.
     */
    @Test
    public void testCheckEnableFlyPlayerFlying() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.isPlayerFlightEnabled(player)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertTrue(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled, but the player is not flying.
     */
    @Test
    public void testCheckEnableFlyPlayerNotFlying() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled for a player on a spawn island with the fly spawn permission.
     */
    @Test
    public void testCheckEnableFlySpawnIslandCanFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.isIslandSpawnIsland(island)).thenReturn(true);
        when(flightValidationManager.hasFlySpawnPermission(user)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertTrue(flightValidationManager.isIslandSpawnIsland(island));
        assertTrue(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled for a player on a spawn island without the fly spawn permission.
     */
    @Test
    public void testCheckEnableFlySpawnIslandCanNotFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.isIslandSpawnIsland(island)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertTrue(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled for a player on a non-spawn island with the fly spawn permission.
     */
    @Test
    public void testCheckEnableFlyNotSpawnIslandCanFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.hasFlySpawnPermission(user)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertTrue(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled for a player on a non-spawn island without the fly spawn permission.
     */
    @Test
    public void testCheckEnableFlyNotSpawnIslandCanNotFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled based on the island's island level.
     */
    @Test
    public void testCheckEnableFlyCanFlyIslandLevel() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should not be enabled based on the island's island level.
     */
    @Test
    public void testCheckEnableFlyCannotFlyIslandLevel() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled and the island settings allow the user to fly on the island.
     */
    @Test
    public void testCheckEnableFlyCanFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);
        when(flightValidationManager.isFlyAllowed(island, user)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertTrue(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled and the island settings do not allow the user to fly on the island.
     */
    @Test
    public void testCheckEnableFlyCannotFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled where the user has the island.fly permission.
     */
    @Test
    public void testCheckEnableFlyHasFlyPermission() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);
        when(flightValidationManager.isFlyAllowed(island, user)).thenReturn(true);
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);

        assertTrue(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertTrue(flightValidationManager.isFlyAllowed(island, user));
        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled where the user has the island.timedfly permission and has flight time.
     */
    @Test
    public void testCheckEnableFlyHasTimedFlyPermission() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);
        when(flightValidationManager.isFlyAllowed(island, user)).thenReturn(true);
        when(flightValidationManager.hasTimedFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.hasFlightTime(user)).thenReturn(true);

        assertTrue(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertTrue(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertTrue(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled where the user lacks the fly permission and the timedfly permission.
     */
    @Test
    public void testCheckEnableFlyLacksFlyPermissions() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(user.getPlayer()).thenReturn(player);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);
        when(flightValidationManager.isFlyAllowed(island, user)).thenReturn(true);

        assertFalse(flyListener.checkEnableFly(user, island));
        assertFalse(flightValidationManager.isPlayerFlightEnabled(player));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertTrue(flightValidationManager.isFlyAllowed(island, user));
        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be enabled where the user has the timed flight permission, but lacks flight time.
     */
    @Test
    public void testEnableFlightTimedFlightNoFlightTime() {
        UUID uuid = UUID.randomUUID();

        when(user.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        assertFalse(flyListener.enableFlight(user));
        assertFalse(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player, never()).setAllowFlight(true);
        verify(bossBarManager, never()).addFlightTimeBossBar(player);
        verify(user, never()).sendMessage("islandfly.enable-fly");
    }

    /**
     * Test checking if flight should be enabled where the user should have normal flight enabled.
     */
    @Test
    public void testEnableFlightNormalFlight() {
        when(user.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        assertTrue(flyListener.enableFlight(user));
        assertTrue(islandFlyPlayerData.isNormalFlightEnabled());
        verify(player).setAllowFlight(true);
        verify(bossBarManager).addIslandFlyBossBar(player);
        verify(user).sendMessage("islandfly.enable-fly");
    }

    /**
     * Test checking if flight should be enabled where the user should have timed flight enabled.
     */
    @Test
    public void testEnableFlightTimedFlight() {
        UUID uuid = UUID.randomUUID();

        when(user.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(flightValidationManager.hasTimedFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.hasFlightTime(user)).thenReturn(true);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 100);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        assertTrue(flyListener.enableFlight(user));
        assertTrue(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player).setAllowFlight(true);
        verify(bossBarManager).addFlightTimeBossBar(player);
        verify(user).sendMessage("islandfly.enable-fly");
    }

    /**
     * Test checking if flight should be removed, but the user is op.
     */
    @Test
    public void testCheckRemoveFlyUserOp() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(flightValidationManager.isUserOp(user)).thenReturn(true);

        assertFalse(flyListener.checkRemoveFly(user));
        assertTrue(flightValidationManager.isUserOp(user));
    }

    /**
     * Test checking if flight should be removed, but the user is in creative or spectator mode.
     */
    @Test
    public void testCheckRemoveFlyUserCreativeOrSpectator() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(flightValidationManager.isUserCreativeOrSpectator(user)).thenReturn(true);

        assertFalse(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertTrue(flightValidationManager.isUserCreativeOrSpectator(user));
    }

    /**
     * Test checking if flight should be removed, but the user has the fly bypass permission.
     */
    @Test
    public void testCheckRemoveFlyUserHasBypassFlyPermission() {
        when(User.getInstance(uuid)).thenReturn(user);
        when(flightValidationManager.hasFlyBypassPermission(user)).thenReturn(true);

        assertFalse(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertTrue(flightValidationManager.hasFlyBypassPermission(user));
    }

    /**
     * Test checking if flight should be removed, but the user is not on an island.
     */
    @Test
    public void testCheckRemoveFlyUserNotOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.empty());

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(islandsManager.getIslandAt(user.getLocation()).isEmpty());
    }

    /**
     * Test checking if flight should be removed, but the user lacks the island.fly or island.timedfly permission.
     */
    @Test
    public void testCheckRemoveFlyLacksFlyPermissions() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertFalse(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be removed, but the user has the island.fly permission.
     */
    @Test
    public void testCheckRemoveFlyHasFlyPermission() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));

        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be removed, but the user has the island.timedfly permission and flight time.
     */
    @Test
    public void testCheckRemoveFlyHasTimedFlyPermissionAndFlightTime() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasTimedFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.hasFlightTime(user)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be removed, but the user has the island.timedfly permission and flight time.
     */
    @Test
    public void testCheckRemoveFlyHasTimedFlyPermissionAndNoFlightTime() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasTimedFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.hasFlightTime(user)).thenReturn(false);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
        assertTrue(flightValidationManager.hasTimedFlyPermission(user));
        assertFalse(flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test checking if flight should be removed, but the user is on a spawn island and lacks the fly spawn permission.
     */
    @Test
    public void testCheckRemoveFlySpawnIslandAndLacksFlySpawnPermission() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.isIslandSpawnIsland(island)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
        assertTrue(flightValidationManager.isIslandSpawnIsland(island));
        assertFalse(flightValidationManager.hasFlySpawnPermission(user));
    }

    /**
     * Test checking if flight should be removed, but the user is on a spawn island and has the fly spawn permission.
     */
    @Test
    public void testCheckRemoveFlySpawnIslandAndHasFlySpawnPermission() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.isIslandSpawnIsland(island)).thenReturn(true);
        when(flightValidationManager.hasFlySpawnPermission(user)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
        assertTrue(flightValidationManager.isIslandSpawnIsland(island));
        assertTrue(flightValidationManager.hasFlySpawnPermission(user));
    }

    /**
     * Test checking if flight should be removed, but the user is not on a spawn island and has the fly spawn permission.
     */
    @Test
    public void testCheckRemoveFlyNotSpawnIslandCanFlySpawn() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.hasFlySpawnPermission(user)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
        assertFalse(flightValidationManager.isIslandSpawnIsland(island));
        assertTrue(flightValidationManager.hasFlySpawnPermission(user));
    }

    /**
     * Test checking if flight should be removed, but the user cannot fly on the island because the island doesn't meet the required island level.
     */
    @Test
    public void testCheckRemoveFlyUserCannotFlyIslandLevel() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.isUserOnIsland(user)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(flightValidationManager.isUserOnIsland(user));
        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
        assertFalse(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
    }

    /**
     * Test checking if flight should be removed, but the user is not allowed to fly according to the island's settings.
     */
    @Test
    public void testCheckRemoveFlyUserFlyOnIslandNotAllowed() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.isUserOnIsland(user)).thenReturn(true);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);

        assertTrue(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(flightValidationManager.isUserOnIsland(user));
        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertFalse(flightValidationManager.isFlyAllowed(island, user));
    }

    /**
     * Test checking if flight should be removed, but the user is allowed to fly according to the island's settings.
     */
    @Test
    public void testCheckRemoveFlyUserCanFlyOnIsland() {
        when(User.getInstance(uuid)).thenReturn(user);

        when(addon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getIslandAt(user.getLocation())).thenReturn(Optional.of(island));
        when(flightValidationManager.hasFlyPermission(user)).thenReturn(true);
        when(flightValidationManager.isUserOnIsland(user)).thenReturn(true);
        when(flightValidationManager.isIslandSpawnIsland(island)).thenReturn(false);
        when(flightValidationManager.canFlyIslandLevel(island)).thenReturn(true);
        when(flightValidationManager.isFlyAllowed(island, user)).thenReturn(true);

        assertFalse(flyListener.checkRemoveFly(user));
        assertFalse(flightValidationManager.isUserOp(user));
        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
        assertFalse(flightValidationManager.hasFlyBypassPermission(user));

        assertTrue(flightValidationManager.isUserOnIsland(user));
        assertTrue(flightValidationManager.hasFlyPermission(user));
        assertFalse(flightValidationManager.hasTimedFlyPermission(user) && flightValidationManager.hasFlightTime(user));
        assertTrue(flightValidationManager.canFlyIslandLevel(island));
        assertTrue(flightValidationManager.isFlyAllowed(island, user));
    }

    /**
     * Test checking if flight should be removed, but the user is in a BentoBox GameMode world and is flying.
     */
    @Test
    public void testRemoveFlyPlayerInGameModeWorldFlying() {
        when(user.getPlayer()).thenReturn(player);
        when(player.isFlying()).thenReturn(true);

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(5);
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        when(flightValidationManager.isUserInGameModeWorld(user)).thenReturn(true);

        flyListener.removeFly(user);

        verify(user).sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(5));
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
    }

    /**
     * Test checking if flight should be removed, but the user is in a BentoBox GameMode world and is not flying.
     */
    @Test
    public void testRemoveFlyPlayerInGameModeWorldNotFlying() {
        when(user.getPlayer()).thenReturn(player);

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(5);
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        when(flightValidationManager.isUserInGameModeWorld(user)).thenReturn(true);

        flyListener.removeFly(user);

        verify(user, never()).sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(5));
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
    }

    /**
     * Test checking if flight should be removed, but the user is not in a BentoBox GameMode world.
     */
    @Test
    public void testRemoveFlyPlayerNotInGameModeWorld() {
        when(user.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);
        when(user.isOnline()).thenReturn(true);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        flyListener.removeFly(user);

        verify(user, never()).sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(5));
        verify(scheduler, never()).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));

        verify(user, never()).sendMessage("islandfly.disable-fly");
        assertFalse(islandFlyPlayerData.isNormalFlightEnabled());
        assertFalse(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player).setFlying(false);
        verify(player).setAllowFlight(false);
    }

    /**
     * Test disabling normal fly while the user is online and flying.
     */
    @Test
    public void testDisableFlyNormalFlyUserOnlineFlying() {
        UUID uuid = UUID.randomUUID();

        when(user.isOnline()).thenReturn(true);
        when(user.getPlayer()).thenReturn(player);
        when(player.isFlying()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(uuid);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        flyListener.disableFly(user);

        verify(user).sendMessage("islandfly.disable-fly");
        assertFalse(islandFlyPlayerData.isNormalFlightEnabled());
        assertFalse(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player).setFlying(false);
        verify(player).setAllowFlight(false);
    }

    /**
     * Test disabling normal fly while the user is online and not flying.
     */
    @Test
    public void testDisableFlyNormalFlyUserOnlineNotFlying() {
        UUID uuid = UUID.randomUUID();

        when(user.isOnline()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(player);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        flyListener.disableFly(user);

        verify(user, never()).sendMessage("islandfly.disable-fly");
        assertFalse(islandFlyPlayerData.isNormalFlightEnabled());
        assertFalse(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player).setFlying(false);
        verify(player).setAllowFlight(false);
    }

    /**
     * Test disabling timed flight while the user is online and flying.
     */
    @Test
    public void testDisableFlyTimedFlyUserOnlineFlying() {
        UUID uuid = UUID.randomUUID();

        when(user.isOnline()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(player);
        when(player.isFlying()).thenReturn(true);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        flyListener.disableFly(user);

        verify(user).sendMessage("islandfly.disable-fly");
        assertFalse(islandFlyPlayerData.isNormalFlightEnabled());
        assertFalse(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player).setFlying(false);
        verify(player).setAllowFlight(false);
    }

    /**
     * Test disabling timed flight while the user is online and not flying.
     */
    @Test
    public void testDisableFlyTimedFlyUserOnlineNotFlying() {
        UUID uuid = UUID.randomUUID();

        when(user.isOnline()).thenReturn(true);
        when(user.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);

        IslandFlyPlayerData islandFlyPlayerData = new IslandFlyPlayerData(uuid.toString(), 0);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        flyListener.disableFly(user);

        verify(user, never()).sendMessage("islandfly.disable-fly");
        assertFalse(islandFlyPlayerData.isNormalFlightEnabled());
        assertFalse(islandFlyPlayerData.isTimedFlightEnabled());
        verify(player).setFlying(false);
        verify(player).setAllowFlight(false);
    }

    /**
     * Test disabling timed flight while the user is offline.
     */
    @Test
    public void testDisableFlyUserOffline() {
        flyListener.disableFly(user);

        verify(user, never()).sendMessage("islandfly.disable-fly");
        verify(player, never()).setFlying(false);
        verify(player, never()).setAllowFlight(false);
    }
}
