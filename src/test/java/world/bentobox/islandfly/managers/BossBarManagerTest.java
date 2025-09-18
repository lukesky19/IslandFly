package world.bentobox.islandfly.managers;

import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.util.FormatUtil;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author lukeskywlker19
 */
@ExtendWith(MockitoExtension.class)
public class BossBarManagerTest {
    /**
     * The player used for tests.
     */
    @Mock
    private Player player;
    /**
     * The UUID of the player used for tests.
     */
    @Mock
    private UUID uuid;
    /**
     * The server used for tests.
     */
    @Mock
    private Server server;
    /**
     * The boss bar used for tests.
     */
    @Mock
    private BossBar bossBar;
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
    private BossBarManager bossBarManager;

    /**
     * Setup required data for each test.
     */
    @BeforeEach
    public void setUp() {
        bossBarManager = new BossBarManager(playerDataManager);
    }

    /**
     * Test adding the boss bar that displays flight time. This is for timed island fly.
     */
    @Test
    public void testAddFlightTimeBossBar() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getServer()).thenReturn(server);
        when(server.createBossBar(anyString(), any(), any())).thenReturn(bossBar);

        when(playerDataManager.getPlayerFlightData(any())).thenReturn(islandFlyPlayerData);
        when(islandFlyPlayerData.getTimeSeconds()).thenReturn(10);

        bossBarManager.addFlightTimeBossBar(player);

        verify(server).createBossBar("Flight Enabled | Time: " + FormatUtil.formatTimeSeconds(10), BarColor.BLUE, BarStyle.SOLID);
        verify(bossBar).addPlayer(player);
    }

    /**
     * Test adding the boss bar that displays flight time is infinite. This is for normal non-timed island fly.
     */
    @Test
    public void testAddIslandFlyBossBar() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getServer()).thenReturn(server);
        when(server.createBossBar(anyString(), any(), any())).thenReturn(bossBar);

        bossBarManager.addIslandFlyBossBar(player);

        verify(server).createBossBar("Flight Enabled | Time: ထ", BarColor.BLUE, BarStyle.SOLID);
        verify(bossBar).addPlayer(player);
    }

    /**
     * Test updating the boss bar test. This is for timed island fly.
     */
    @Test
    public void testUpdateBossBar() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(any())).thenReturn(islandFlyPlayerData);
        when(player.getServer()).thenReturn(server);
        when(server.createBossBar(anyString(), any(), any())).thenReturn(bossBar);
        when(islandFlyPlayerData.getTimeSeconds()).thenReturn(10);
        when(islandFlyPlayerData.isTimedFlightEnabled()).thenReturn(true);

        // Initialize boss bar
        bossBarManager.addFlightTimeBossBar(player);

        // Update boss bar
        bossBarManager.updateBossBar(player);

        verify(bossBar, atLeastOnce()).setTitle(anyString());
    }

    /**
     * Test updating a boss bar when no active boss bar is stored.
     */
    @Test
    public void testUpdateBossBarNoBossBarActive() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(any())).thenReturn(islandFlyPlayerData);
        when(islandFlyPlayerData.isTimedFlightEnabled()).thenReturn(true);

        // Update boss bar
        bossBarManager.updateBossBar(player);

        verify(bossBar, never()).setTitle(anyString());
    }

    /**
     * Test updating the boss bar when timed flight is disabled.
     */
    @Test
    public void testUpdateBossBarTimedFlightDisabled() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(any())).thenReturn(islandFlyPlayerData);

        bossBarManager.updateBossBar(player);

        verify(bossBar, never()).setTitle(anyString());
    }

    /**
     * Test removing an active boss bar.
     */
    @Test
    public void testRemoveBossBar() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(playerDataManager.getPlayerFlightData(any())).thenReturn(islandFlyPlayerData);
        when(player.getServer()).thenReturn(server);
        when(server.createBossBar(anyString(), any(), any())).thenReturn(bossBar);
        when(islandFlyPlayerData.getTimeSeconds()).thenReturn(10);

        // Initialize boss bar
        bossBarManager.addFlightTimeBossBar(player);

        // Remove boss bar
        bossBarManager.removeBossBar(player);

        verify(bossBar, atLeastOnce()).removePlayer(player);
    }

    /**
     * Test removing a boss bar when none is stored.
     */
    @Test
    public void testRemoveBossBarNoActiveBossBar() {
        // Remove boss bar
        bossBarManager.removeBossBar(player);

        verify(bossBar, never()).removePlayer(player);
    }
}
