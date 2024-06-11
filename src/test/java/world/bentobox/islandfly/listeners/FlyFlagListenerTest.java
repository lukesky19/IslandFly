package world.bentobox.islandfly.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.flags.FlagProtectionChangeEvent;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.managers.FlightTimeManager;

/**
 * @author tastybento
 */
@ExtendWith(MockitoExtension.class)
public class FlyFlagListenerTest {
    
    private FlyFlagListener ffl;
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandFlyAddon addon;
    @Mock
    private Settings settings;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private FlagProtectionChangeEvent e;
    @Mock
    private Player p1;
    @Mock
    private Player p2;
    @Mock
    private Player p3;
    @Mock
    private Player op;
    @Mock
    private Island island;
    @Mock
    FlightTimeManager flightTimeManager;

    MockedStatic<Bukkit> mockedBukkitClass;

    @BeforeEach
    public void setUp() {
        User.setPlugin(plugin);

        // Bukkit
        mockedBukkitClass = mockStatic(Bukkit.class);
        mockedBukkitClass.when(Bukkit::getScheduler).thenReturn(scheduler);

        // Players/Users
        when(p1.getUniqueId()).thenReturn(UUID.randomUUID());
        User.getInstance(p1);
        when(p2.getUniqueId()).thenReturn(UUID.randomUUID());
        User.getInstance(p2);
        when(p3.getUniqueId()).thenReturn(UUID.randomUUID());
        User.getInstance(p3);
        when(op.getUniqueId()).thenReturn(UUID.randomUUID());
        User.getInstance(op);
        
        ffl = new FlyFlagListener(addon,flightTimeManager);
    }

    @AfterEach
    public void tearDown() {
        User.clearUsers();
        mockedBukkitClass.close();
    }
    
    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyFlagListener#onFlagChange(world.bentobox.bentobox.api.events.flags.FlagProtectionChangeEvent)}.
     */
    @Test
    public void testOnFlagChangeOtherFlag() {
        FlagProtectionChangeEvent e = mock(FlagProtectionChangeEvent.class);
        Flag flag = mock(Flag.class);
        when(e.getEditedFlag()).thenReturn(flag);

        ffl.onFlagChange(e);
        verify(e, never()).getIsland();
    }
    
    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyFlagListener#onFlagChange(world.bentobox.bentobox.api.events.flags.FlagProtectionChangeEvent)}.
     */
    @Test
    public void testOnFlagChange() {
        // Plugin
        when(addon.getPlugin()).thenReturn(plugin);

        // FlagProtectionChangeEvent
        when(e.getEditedFlag()).thenReturn(IslandFlyAddon.ISLAND_FLY_PROTECTION);
        when(e.getIsland()).thenReturn(island);

        // List of Players
        @NonNull
        List<Player> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(op);
        when(island.getPlayersOnIsland()).thenReturn(list);

        // Player 2
        when(p2.isFlying()).thenReturn(true);

        // Settings
        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(5);

        // lm
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        // phm
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        ffl.onFlagChange(e);
        verify(p1, never()).sendMessage(anyString());
        verify(p2).sendMessage(eq("islandfly.fly-turning-off-alert"));
        verify(p3, never()).sendMessage(anyString());
        verify(op, never()).sendMessage(anyString());
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), eq(100L));
    }
    
    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyFlagListener#onFlagChange(world.bentobox.bentobox.api.events.flags.FlagProtectionChangeEvent)}.
     */
    @Test
    public void testOnFlagChangeZeroTime() {
        // FlagProtectionChangeEvent
        when(e.getEditedFlag()).thenReturn(IslandFlyAddon.ISLAND_FLY_PROTECTION);
        when(e.getIsland()).thenReturn(island);

        // List of Players
        @NonNull
        List<Player> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(op);
        when(island.getPlayersOnIsland()).thenReturn(list);

        // Player 2
        when(p2.isFlying()).thenReturn(true);

        // Settings
        when(addon.getSettings()).thenReturn(settings);
        when(settings.getFlyTimeout()).thenReturn(0);

        // lm
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        // phm
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        ffl.onFlagChange(e);
        verify(p1, never()).sendMessage(anyString());
        verify(p2).sendMessage(eq("islandfly.fly-turning-off-alert"));
        verify(p3, never()).sendMessage(anyString());
        verify(op, never()).sendMessage(anyString());
        
        verify(p2).setFlying(false);
        verify(p2).setAllowFlight(false);
        verify(p2).sendMessage("islandfly.disable-fly");
    }

    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyFlagListener#disable(Player, User, Island)}.
     */
    @Test
    public void testDisableAllowedAgain() {
        when(p2.isOnline()).thenReturn(true);

        // lm
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        // phm
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        when(island.isAllowed(any(), any())).thenReturn(true);
        ffl.disable(p2, User.getInstance(p2), island);
        verify(p2).sendMessage(eq("islandfly.reallowed-fly"));
    }
    
    /**
     * Test method for {@link world.bentobox.islandfly.listeners.FlyFlagListener#disable(Player, User, Island)}.
     */
    @Test
    public void testDisable() {
        when(p2.isOnline()).thenReturn(true);
        when(island.isAllowed(any(), any())).thenReturn(false);
        when(island.onIsland(p2.getLocation())).thenReturn(true);

        // lm
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        // phm
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        ffl.disable(p2, User.getInstance(p2), island);
        verify(p2).sendMessage(eq("islandfly.disable-fly"));
    }
}
