package world.bentobox.islandfly;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.islandfly.listeners.FlyDeathListener;
import world.bentobox.islandfly.listeners.FlyFlagListener;
import world.bentobox.islandfly.listeners.FlyListener;
import world.bentobox.islandfly.listeners.FlyLogoutListener;

/**
 * @author tastybento
 *
 */
@ExtendWith(MockitoExtension.class)
public class IslandFlyAddonTest {
    @Mock
    BentoBox plugin;
    @Mock
    Settings settings;
    @Mock
    Logger logger;
    @Mock
    private AddonsManager am;
    @Mock
    private GameModeAddon gameMode;
    @Mock
    private FlagsManager fm;
    @Spy
    IslandFlyAddon addon;

    @BeforeEach
    public void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        Field field = plugin.getClass().getDeclaredField("instance");
        field.setAccessible(true);
        field.set(plugin.getClass(), plugin);

        File jFile = new File("addon.jar");
        Path original = Paths.get("src/main/resources/config.yml");
        Path path = Paths.get("config.yml");
        Files.copy(original, path);
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            //Added the new files to the jar.
            try (FileInputStream fis = new FileInputStream(path.toFile())) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                JarEntry entry = new JarEntry(path.toString());
                tempJarOutputStream.putNextEntry(entry);
                while((bytesRead = fis.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        File dataFolder = new File("addons/IslandFlyAddon");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "island fly addon", "1.3").description("test").authors("BONNe").build();
        addon.setDescription(desc);
    }

    @AfterEach
    public void tearDown() throws Exception {
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
    }

    private static void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    /**
     * Test method for {@link world.bentobox.islandfly.IslandFlyAddon#onEnable()}.
     */
    @Test
    public void testOnEnable() {
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getAddonsManager()).thenReturn(am);

        // Player command
        CompositeCommand cmd = mock(CompositeCommand.class);
        @NonNull
        Optional<CompositeCommand> opCmd = Optional.of(cmd);
        when(gameMode.getPlayerCommand()).thenReturn(opCmd);
        // Admin Command
        CompositeCommand aCmd = mock(CompositeCommand.class);
        @NonNull
        Optional<CompositeCommand> opACmd = Optional.of(aCmd);
        when(gameMode.getAdminCommand()).thenReturn(opACmd);

        when(addon.getPlugin().getFlagsManager()).thenReturn(fm);
        when(addon.getPlugin().getAddonsManager()).thenReturn(am);
        when(addon.getPlugin().getAddonsManager().getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));

        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);

        addon.onLoad();
        addon.onEnable();

        verify(addon).registerFlag(any());
        verify(addon).registerListener(any(FlyListener.class));
        verify(addon).registerListener(any(FlyDeathListener.class));
        verify(addon).registerListener(any(FlyLogoutListener.class));
        verify(addon).registerListener(any(FlyLogoutListener.class));
        verify(addon).registerListener(any(FlyFlagListener.class));
    }

    /**
     * Test method for {@link world.bentobox.islandfly.IslandFlyAddon#onEnable()}.
     */
    @Test
    public void testOnEnableNoHook() {
        // Settings for Database
        when(plugin.getSettings()).thenReturn(settings);
        doReturn(DatabaseSetup.DatabaseType.JSON).when(settings).getDatabaseType();

        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getAddonsManager()).thenReturn(am);
        when(addon.getPlugin().getAddonsManager().getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));

        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);

        addon.onLoad();
        when(addon.getSettings()).thenReturn(mock(world.bentobox.islandfly.config.Settings.class));
        when(addon.getSettings().getDisabledGameModes()).thenReturn(Collections.singleton("BSkyBlock"));
        addon.onEnable();

        verify(addon, never()).registerFlag(any());
        verify(addon, never()).registerListener(any(FlyListener.class));
        verify(addon, never()).registerListener(any(FlyDeathListener.class));
        verify(addon, never()).registerListener(any(FlyLogoutListener.class));
        verify(addon, never()).registerListener(any(FlyFlagListener.class));
    }

    /**
     * Test method for {@link world.bentobox.islandfly.IslandFlyAddon#onReload()}.
     */
    @Test
    public void testOnReloadHooked() {
        when(addon.getPlugin()).thenReturn(plugin);

        testOnEnable();
        addon.onReload();
        verify(plugin).log("[island fly addon] IslandFly addon reloaded.");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.IslandFlyAddon#onReload()}.
     */
    @Test
    public void testOnReloadNotHooked() {
        addon.onReload();
        verify(plugin, never()).log(anyString());
    }

    /**
     * Test method for {@link world.bentobox.islandfly.IslandFlyAddon#getSettings()}.
     */
    @Test
    public void testGetSettings() {
        assertNull(addon.getSettings());
    }

}
