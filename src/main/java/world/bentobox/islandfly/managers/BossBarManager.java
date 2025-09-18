package world.bentobox.islandfly.managers;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.util.FormatUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the showing of boss bars to the player.
 */
public class BossBarManager {
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull Map<UUID, BossBar> activeBossBars = new HashMap<>();

    /**
     * Constructor
     * @param playerDataManager A {@link PlayerDataManager}.
     */
    public BossBarManager(@NotNull PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    /**
     * Show a boss bar that the player has island flight enabled with the time limit.
     * @param player The {@link Player} to show the boss bar to.
     */
    public void addFlightTimeBossBar(@NotNull Player player) {
        removeBossBar(player);

        UUID uuid = player.getUniqueId();
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(uuid);

        BossBar bossBar = player.getServer().createBossBar("Flight Enabled | Time: " + FormatUtil.formatTimeSeconds(islandFlyPlayerData.getTimeSeconds()), BarColor.BLUE, BarStyle.SOLID);

        bossBar.addPlayer(player);

        activeBossBars.put(uuid, bossBar);
    }

    /**
     * Show a boss bar that the player has island flight enabled with no limit.
     * @param player The {@link Player} to show the boss bar to.
     */
    public void addIslandFlyBossBar(@NotNull Player player) {
        removeBossBar(player);

        UUID uuid = player.getUniqueId();

        BossBar bossBar = player.getServer().createBossBar("Flight Enabled | Time: ထ", BarColor.BLUE, BarStyle.SOLID);

        bossBar.addPlayer(player);

        activeBossBars.put(uuid, bossBar);
    }

    /**
     * Update the boss bar for the player with the updated flight time if using timed flight.
     * @param player The {@link Player} to update the boss bar for.
     */
    public void updateBossBar(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        IslandFlyPlayerData islandFlyPlayerData = playerDataManager.getPlayerFlightData(uuid);
        if(!islandFlyPlayerData.isTimedFlightEnabled()) return;
        @Nullable BossBar bossBar = activeBossBars.get(uuid);
        if(bossBar == null) return;

        bossBar.setTitle("Flight Enabled | Time: " + FormatUtil.formatTimeSeconds(islandFlyPlayerData.getTimeSeconds()));
    }

    /**
     * Remove the boss bar currently shown to the player if any.
     * @param player The {@link Player} to remove the boss bar for.
     */
    public void removeBossBar(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        @Nullable BossBar bossBar = activeBossBars.get(uuid);
        if(bossBar == null) return;

        bossBar.removePlayer(player);
        activeBossBars.remove(uuid);
    }
}
