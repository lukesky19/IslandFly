package world.bentobox.islandfly.managers;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author lukeskywlker19<br>
 * DISCLAIMER:<br>
 * As far as I know, creating a BukkitTask/BukkitRunnable outside a live server environment is not possible.<br>
 * Because of this, I am unable to test all functions and use cases inside the FlightTimeManager.class.
 * This test class only has basic tests for setting, adding, removing, and deleting flight data.<br>
 */
@ExtendWith(MockitoExtension.class)
public class FlightTimeManagerTest {
    static BentoBox plugin;
    static Settings settings;
    static IslandFlyAddon addon;
    @Mock
    Player player;

    UUID uuid;
    static FlightTimeManager flightTimeManager;

    @BeforeAll
    public static void beforeClass() throws NoSuchFieldException, IllegalAccessException {
        addon = mock(IslandFlyAddon.class);
        plugin = mock(BentoBox.class);
        settings = mock(Settings.class);

        // Set Up Plugin
        Field field = plugin.getClass().getDeclaredField("instance");
        field.setAccessible(true);
        field.set(plugin.getClass(), plugin);

        // Settings for Database
        when(plugin.getSettings()).thenReturn(settings);
        doReturn(DatabaseSetup.DatabaseType.JSON).when(settings).getDatabaseType();

        Database<IslandFlyPlayerData> islandFlyPlayerDatabase = new Database<>(addon, IslandFlyPlayerData.class);
        flightTimeManager = new FlightTimeManager(addon, islandFlyPlayerDatabase);
    }

    @BeforeEach
    public void setUp() {
        // User/Player
        uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
    }

    @AfterEach
    public void cleanUp() {
        deleteAll(new File("database"));
        deleteAll(new File("database_backup"));
    }

    private void deleteAll(File file) {
        if(file.exists()) {
            try(Stream<Path> paths = Files.walk(file.toPath())) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testGetPlayerFlightData() {
        flightTimeManager.setPlayerFlightTime(player, 1);

        assertEquals(1, Objects.requireNonNull(flightTimeManager.getPlayerFlightData(uuid)).getTimeSeconds());
    }

    @Test
    public void testSetPlayerFlightTime_NotFlying() {
        assertEquals(2, flightTimeManager.setPlayerFlightTime(player, 2));
    }

    @Test
    public void testAddPlayerFlightData() {
        flightTimeManager.setPlayerFlightTime(player, 1);

        assertEquals(2, flightTimeManager.addPlayerFlightTime(player, 1));
    }

    @Test
    public void testRemovePlayerFlightData() {
        flightTimeManager.setPlayerFlightTime(player, 1);

        assertEquals(0, flightTimeManager.removePlayerFlightTime(player, 1));
    }

    @Test
    public void testDeletePlayerFlightData() {
        flightTimeManager.setPlayerFlightTime(player, 1);

        flightTimeManager.deletePlayerFlightData(player);
        File file = new File("database/IslandFlyPlayerData/" + uuid + ".json");
        assertFalse(file.exists());
    }
}
