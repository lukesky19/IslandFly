package world.bentobox.islandfly.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.managers.FlightTimeManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author lukeskywlker19
 */
@ExtendWith(MockitoExtension.class)
public class FlyLoginListenerTest {
    @Mock
    private IslandFlyAddon addon;
    @Mock
    private FlightTimeManager flightTimeManager;
    @Mock
    private World world;
    @Mock
    private Location location;
    @Mock
    private Block block;
    @Mock
    private Block relBlock1;
    @Mock
    private Block relBlock2;
    @Mock
    private Player player;
    @Mock
    User user;
    @Mock
    UUID uuid;
    private FlyLoginListener flyLoginListener;

    MockedStatic<User> mockedUserClass;

    @BeforeEach
    public void setUp() {
        flyLoginListener = new FlyLoginListener(addon, flightTimeManager);
        // User
        mockedUserClass = mockStatic(User.class);
        mockedUserClass.when(() -> User.getInstance(uuid)).thenReturn(user);
    }

    @AfterEach
    public void tearDown() {
        User.clearUsers();
        mockedUserClass.close();
    }


    @Test
    public void testIsInAirPlayerInAir() {
        when(player.getWorld()).thenReturn(world);
        when(world.getMinHeight()).thenReturn(1);
        when(player.getLocation()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);
        when(location.getBlockY()).thenReturn(3);
        when(block.getRelative(BlockFace.DOWN)).thenReturn(relBlock1);
        when(relBlock1.isEmpty()).thenReturn(true);
        when(block.getRelative(BlockFace.DOWN, 2)).thenReturn(relBlock2);
        when(relBlock2.isEmpty()).thenReturn(true);

        assertTrue(flyLoginListener.isInAir(player));
    }

    @Test
    public void testIsInAirPlayerLessThanWorldMinHeight() {
        when(player.getWorld()).thenReturn(world);
        when(world.getMinHeight()).thenReturn(3);
        when(player.getLocation()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);
        when(location.getBlockY()).thenReturn(1);

        assertFalse(flyLoginListener.isInAir(player));
    }

    @Test
    public void testIsInAirFirstRelBlockNotEmpty() {
        when(player.getWorld()).thenReturn(world);
        when(world.getMinHeight()).thenReturn(1);
        when(player.getLocation()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);
        when(location.getBlockY()).thenReturn(3);
        when(block.getRelative(BlockFace.DOWN)).thenReturn(relBlock1);
        when(relBlock1.isEmpty()).thenReturn(false);

        assertFalse(flyLoginListener.isInAir(player));
    }

    @Test
    public void testIsInAirSecondRelBlockNotEmpty() {
        when(player.getWorld()).thenReturn(world);
        when(world.getMinHeight()).thenReturn(1);
        when(player.getLocation()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);
        when(location.getBlockY()).thenReturn(3);
        when(block.getRelative(BlockFace.DOWN)).thenReturn(relBlock1);
        when(relBlock1.isEmpty()).thenReturn(true);
        when(block.getRelative(BlockFace.DOWN, 2)).thenReturn(relBlock2);
        when(relBlock2.isEmpty()).thenReturn(false);

        assertFalse(flyLoginListener.isInAir(player));
    }
}
