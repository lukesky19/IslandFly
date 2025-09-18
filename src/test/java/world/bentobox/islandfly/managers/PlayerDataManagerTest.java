package world.bentobox.islandfly.managers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author lukeskywlker19<br>
 */
@ExtendWith(MockitoExtension.class)
public class PlayerDataManagerTest {
    /**
     * The {@link UUID} used in tests.
     */
    private UUID uuid;
    /**
     * This is the class being tested.
     */
    private static PlayerDataManager playerDataManager;

    /**
     * Setup data required for all tests. This only runs once.
     * @throws NoSuchFieldException Thrown if no field called "instance" exists in the BentoBox plugin.
     * @throws IllegalAccessException Thrown if the field is not accessible.
     */
    @BeforeAll
    public static void beforeClass() throws NoSuchFieldException, IllegalAccessException {
        IslandFlyAddon addon = mock(IslandFlyAddon.class);
        BentoBox plugin = mock(BentoBox.class);
        Settings settings = mock(Settings.class);

        // Set Up Plugin
        Field field = plugin.getClass().getDeclaredField("instance");
        field.setAccessible(true);
        field.set(plugin.getClass(), plugin);

        // Settings for Database
        when(plugin.getSettings()).thenReturn(settings);
        doReturn(DatabaseSetup.DatabaseType.JSON).when(settings).getDatabaseType();

        Database<IslandFlyPlayerData> islandFlyPlayerDatabase = new Database<>(addon, IslandFlyPlayerData.class);
        playerDataManager = new PlayerDataManager(islandFlyPlayerDatabase);
    }

    /**
     * Setup any data required for tests.
     */
    @BeforeEach
    public void setUp() {
        // UUID
        uuid = UUID.randomUUID();
    }

    /**
     * Delete any files in the database or database_backup directories generated from tests.
     */
    @AfterEach
    public void cleanUp() {
        deleteAll(new File("database"));
        deleteAll(new File("database_backup"));
    }

    /**
     * Stream through the directory as a {@link File} provided and delete any files.
     * @param file The directory to stream through as a {@link File} provided and delete any files for.
     */
    private void deleteAll(@NotNull File file) {
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

    /**
     * Test getting player data stored in memory, but none exists.
     */
    @Test
    public void testGetPlayerDataNoData() {
        // Check that no player data is stored
        assertTrue(playerDataManager.getPlayerDataMap().isEmpty());
        // Check that the player data loaded equals the default
        assertEquals(0, playerDataManager.getPlayerFlightData(uuid).getTimeSeconds());
    }

    /**
     * Test saving and loading player data.
     */
    @Test
    public void testSaveAndLoadPlayerData() {
        // Save player data
        playerDataManager.savePlayerData(new IslandFlyPlayerData(uuid.toString(), 10));

        // Load player data
        playerDataManager.loadPlayerData(uuid);

        // Check that player data was loaded
        assertFalse(playerDataManager.getPlayerDataMap().isEmpty());
        // Check that the player data loaded equals what was saved.
        assertEquals(10, playerDataManager.getPlayerFlightData(uuid).getTimeSeconds());

        // Clear the player data map of any player data.
        playerDataManager.getPlayerDataMap().clear();
    }

    /**
     * Test loading player data where none is stored in the database.
     */
    @Test
    public void testLoadingPlayerDataNoData() {
        // Load player data
        playerDataManager.loadPlayerData(uuid);

        // Check that player data was loaded
        assertFalse(playerDataManager.getPlayerDataMap().isEmpty());
        // Check that the player data loaded equals the default.
        assertEquals(0, playerDataManager.getPlayerFlightData(uuid).getTimeSeconds());

        // Clear the player data map of any player data.
        playerDataManager.getPlayerDataMap().clear();
    }

    /**
     * Test saving player data by a UUID that is stored in the player data map.
     */
    @Test
    public void testSavePlayerDataByUUID() {
        // Populate the player data map with data
        playerDataManager.getPlayerDataMap().put(uuid, new IslandFlyPlayerData(uuid.toString(), 10));

        // Save player data
        playerDataManager.savePlayerData(uuid);

        // Load player data
        playerDataManager.loadPlayerData(uuid);

        // Check that player data was loaded
        assertFalse(playerDataManager.getPlayerDataMap().isEmpty());
        // Check that the player data loaded equals what was saved.
        assertEquals(10, playerDataManager.getPlayerFlightData(uuid).getTimeSeconds());

        // Clear the player data map of any player data.
        playerDataManager.getPlayerDataMap().clear();
    }

    /**
     * Test saving player data by a UUID, but none is stored in the player data map.
     */
    @Test
    public void testSavePlayerDataByUUIDNoData() {
        // Save player data (none should be saved)
        playerDataManager.savePlayerData(uuid);

        // Load player data
        playerDataManager.loadPlayerData(uuid);

        // Check that player data was loaded
        assertFalse(playerDataManager.getPlayerDataMap().isEmpty());
        // Check that the player data loaded equals the default.
        assertEquals(0, playerDataManager.getPlayerFlightData(uuid).getTimeSeconds());

        // Clear the player data map of any player data.
        playerDataManager.getPlayerDataMap().clear();
    }

    /**
     * Test player data being removed from the player data map.
     */
    @Test
    public void testUnloadPlayerData() {
        // Populate the player data map with data
        playerDataManager.getPlayerDataMap().put(uuid, new IslandFlyPlayerData(uuid.toString(), 10));

        // Unload player data
        playerDataManager.unloadPlayerData(uuid);

        // Check that player data was unloaded
        assertTrue(playerDataManager.getPlayerDataMap().isEmpty());

        // Clear the player data map of any player data.
        playerDataManager.getPlayerDataMap().clear();
    }
}
