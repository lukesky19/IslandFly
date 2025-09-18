package world.bentobox.islandfly.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.level.Level;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author lukeskywlker19
 */
@ExtendWith(MockitoExtension.class)
public class FlightValidationManagerTest {
    /**
     * The IslandFlyAddon used for tests.
     */
    @Mock
    private IslandFlyAddon islandFlyAddon;
    /**
     * The player used for tests.
     */
    @Mock
    private Player player;
    /**
     * The User used for tests
     */
    @Mock
    private User user;
    /**
     * The UUID of the player used for tests.
     */
    @Mock
    private UUID uuid;
    /**
     * The location used for tests.
     */
    @Mock
    private Location location;
    /**
     * The BentoBox plugin used for tests.
     */
    @Mock
    private BentoBox bentoBox;
    /**
     * The level addon used for tests.
     */
    @Mock
    private Level levelAddon;
    /**
     * The GameModeAddon used for tests.
     */
    @Mock
    GameModeAddon gameModeAddon;
    /**
     * The islandfly settings used for tests.
     */
    @Mock
    private Settings settings;
    /**
     * The IslandWorldManager used for tests.
     */
    @Mock
    private IslandWorldManager islandWorldManager;
    /**
     * The IslandsManager used for tests.
     */
    @Mock
    private IslandsManager islandsManager;
    /**
     * The world used for tests.
     */
    @Mock
    private World world;
    /**
     * The island used for tests.
     */
    @Mock
    private Island island;
    /**
     * The IslandFlyPlayerData used for tests.
     */
    @Mock
    private IslandFlyPlayerData islandFlyPlayerData;
    /**
     * Mocked class that manages player data.
     */
    @Mock
    private PlayerDataManager playerDataManager;
    /**
     * This is the class being tested.
     */
    private FlightValidationManager flightValidationManager;

    /**
     * Setup required data for each test.
     */
    @BeforeEach
    public void setUp() {
        flightValidationManager = new FlightValidationManager(islandFlyAddon, playerDataManager);
    }

    /**
     * Test if the player has flight allowed.
     */
    @Test
    public void testPlayerFlightIsEnabled() {
        when(player.getAllowFlight()).thenReturn(true);

        assertTrue(flightValidationManager.isPlayerFlightEnabled(player));
    }

    /**
     * Test if player flight is managed by the addon where normal flight is enabled.
     */
    @Test
    public void testIsFlightAddonManagedNormalFlight() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);
        when(islandFlyPlayerData.isNormalFlightEnabled()).thenReturn(true);

        assertTrue(flightValidationManager.isFlightAddonManaged(player));
    }

    /**
     * Test if player flight is managed by the addon where timed flight is enabled.
     */
    @Test
    public void testIsFlightAddonManagedTimedFlight() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);
        when(islandFlyPlayerData.isTimedFlightEnabled()).thenReturn(true);

        assertTrue(flightValidationManager.isFlightAddonManaged(player));
    }

    /**
     * Test if player flight is managed by the addon where timed flight is enabled.
     */
    @Test
    public void testIsFlightAddonManagedNotAddonManaged() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);

        assertFalse(flightValidationManager.isFlightAddonManaged(player));
    }

    /**
     * Test if the user is op.
     */
    @Test
    public void testIsUserOp() {
        when(user.isOp()).thenReturn(true);

        assertTrue(flightValidationManager.isUserOp(user));
    }

    /**
     * Test if the user is in creative mode.
     */
    @Test
    public void testIsUserCreative() {
        when(user.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);

        assertTrue(flightValidationManager.isUserCreativeOrSpectator(user));
    }

    /**
     * Test if the user is in spectator mode.
     */
    @Test
    public void testIsUserSpectator() {
        when(user.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);

        assertTrue(flightValidationManager.isUserCreativeOrSpectator(user));
    }

    /**
     * Test if the user is not in creative or spectator mode.
     */
    @Test
    public void testIsUserNotCreativeOrSpectator() {
        when(user.getPlayer()).thenReturn(player);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);

        assertFalse(flightValidationManager.isUserCreativeOrSpectator(user));
    }

    /**
     * Test if the user is in a GameMode world.
     */
    @Test
    public void testIsUserInGameModeWorld() {
        when(user.getWorld()).thenReturn(world);
        when(islandFlyAddon.getPlugin()).thenReturn(bentoBox);
        when(bentoBox.getIWM()).thenReturn(islandWorldManager);
        when(islandWorldManager.getAddon(world)).thenReturn(Optional.of(gameModeAddon));

        assertTrue(flightValidationManager.isUserInGameModeWorld(user));
    }

    /**
     * Test if a user is on an island.
     */
    @Test
    public void testIsUserOnIsland() {
        when(user.getLocation()).thenReturn(location);
        when(islandFlyAddon.getIslands()).thenReturn(islandsManager);
        when(islandsManager.getProtectedIslandAt(location)).thenReturn(Optional.of(island));

        assertTrue(flightValidationManager.isUserOnIsland(user));
    }

    /**
     * Test if an island is a spawn island.
     */
    @Test
    public void testIsIslandSpawnIsland() {
        when(island.isSpawn()).thenReturn(true);

        assertTrue(flightValidationManager.isIslandSpawnIsland(island));
    }

    /**
     * Test if the user has the fly bypass permission.
     */
    @Test
    public void testHasFlyBypassPermission() {
        when(user.getWorld()).thenReturn(world);
        when(islandFlyAddon.getPlugin()).thenReturn(bentoBox);
        when(bentoBox.getIWM()).thenReturn(islandWorldManager);
        when(islandWorldManager.getPermissionPrefix(world)).thenReturn("bskyblock");
        when(user.hasPermission(any())).thenReturn(true);

        assertTrue(flightValidationManager.hasFlyBypassPermission(user));
    }

    /**
     * Test if the user has the fly spawn permission.
     */
    @Test
    public void testHasFlySpawnPermission() {
        when(user.getWorld()).thenReturn(world);
        when(islandFlyAddon.getPlugin()).thenReturn(bentoBox);
        when(bentoBox.getIWM()).thenReturn(islandWorldManager);
        when(islandWorldManager.getPermissionPrefix(world)).thenReturn("bskyblock");
        when(user.hasPermission(any())).thenReturn(true);

        assertTrue(flightValidationManager.hasFlySpawnPermission(user));
    }

    /**
     * Test if the user has the fly permission.
     */
    @Test
    public void testHasFlyPermission() {
        when(user.getWorld()).thenReturn(world);
        when(islandFlyAddon.getPlugin()).thenReturn(bentoBox);
        when(bentoBox.getIWM()).thenReturn(islandWorldManager);
        when(islandWorldManager.getPermissionPrefix(world)).thenReturn("bskyblock");
        when(user.hasPermission(any())).thenReturn(true);

        assertTrue(flightValidationManager.hasFlyPermission(user));
    }

    /**
     * Test if the user has the timed fly permission.
     */
    @Test
    public void testHasTimedFlyPermission() {
        when(user.getWorld()).thenReturn(world);
        when(islandFlyAddon.getPlugin()).thenReturn(bentoBox);
        when(bentoBox.getIWM()).thenReturn(islandWorldManager);
        when(islandWorldManager.getPermissionPrefix(world)).thenReturn("bskyblock");
        when(user.hasPermission(any())).thenReturn(true);

        assertTrue(flightValidationManager.hasTimedFlyPermission(user));
    }

    /**
     * Test if the user has flight time.
     */
    @Test
    public void testHasFlightTimeWithTime() {
        when(user.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);
        when(islandFlyPlayerData.getTimeSeconds()).thenReturn(100);

        assertTrue(flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test if the user has no flight time.
     */
    @Test
    public void testHasFlightTimeWithNoTime() {
        when(user.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(uuid)).thenReturn(islandFlyPlayerData);
        when(islandFlyPlayerData.getTimeSeconds()).thenReturn(0);

        assertFalse(flightValidationManager.hasFlightTime(user));
    }

    /**
     * Test if fly is allowed based on the island level, the fly min level is 5, and the island level is 10.
     */
    @Test
    public void testCanFlyIslandLevel() {
        when(island.getOwner()).thenReturn(uuid);
        when(island.getWorld()).thenReturn(world);
        when(islandFlyAddon.getLevelAddon()).thenReturn(levelAddon);
        when(islandFlyAddon.getSettings()).thenReturn(settings);
        when(settings.getFlyMinLevel()).thenReturn(5L);
        when(levelAddon.getIslandLevel(world, uuid)).thenReturn(10L);

        assertTrue(flightValidationManager.canFlyIslandLevel(island));
    }

    /**
     * Test if fly is not allowed based on the island level, the fly min level is 5, and the island level is 0.
     */
    @Test
    public void testCanFlyIslandLevelBelowMinFlyLevel() {
        when(island.getOwner()).thenReturn(uuid);
        when(island.getWorld()).thenReturn(world);
        when(islandFlyAddon.getLevelAddon()).thenReturn(levelAddon);
        when(islandFlyAddon.getSettings()).thenReturn(settings);
        when(settings.getFlyMinLevel()).thenReturn(5L);
        when(levelAddon.getIslandLevel(world, uuid)).thenReturn(0L);

        assertFalse(flightValidationManager.canFlyIslandLevel(island));
    }

    /**
     * Test if fly is allowed based on an island level and the level addon is null.
     */
    @Test
    public void testCanFlyIslandLevelNoLevelAddon() {
        when(islandFlyAddon.getLevelAddon()).thenReturn(null);

        assertTrue(flightValidationManager.canFlyIslandLevel(island));
    }

    /**
     * Test if fly is allowed based on an island level and the min fly level is 0.
     */
    @Test
    public void testCanFlyIslandLevelMinFlyLevelZero() {
        when(islandFlyAddon.getLevelAddon()).thenReturn(levelAddon);
        when(islandFlyAddon.getSettings()).thenReturn(settings);
        when(settings.getFlyMinLevel()).thenReturn(0L);

        assertTrue(flightValidationManager.canFlyIslandLevel(island));
    }

    /**
     * Test if fly is allowed for the user based on the island's settings.
     */
    @Test
    public void testIsFlyAllowed() {
        when(island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION)).thenReturn(true);

        assertTrue(flightValidationManager.isFlyAllowed(island, user));
    }
}
