package world.bentobox.islandfly.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;

import java.util.Map;
import java.util.UUID;

/**
 * This class manages the tasks that track flight time and the saving of player data.
 */
public class TaskManager {
    private final @NotNull IslandFlyAddon islandFlyAddon;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;
    private @Nullable BukkitTask flightTimeTask;
    private @Nullable BukkitTask saveTask;

    /**
     * Constructor
     * @param islandFlyAddon An {@link IslandFlyAddon} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     */
    public TaskManager(
            @NotNull IslandFlyAddon islandFlyAddon,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull BossBarManager bossBarManager) {
        this.islandFlyAddon = islandFlyAddon;
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

    /**
     * Start the task that decrements flight time for players actively flying.
     */
    public void startFlightTimeTask() {
        flightTimeTask = islandFlyAddon.getServer().getScheduler().runTaskTimer(islandFlyAddon.getPlugin(), () -> {
            @NotNull Map<@NotNull UUID, @NotNull IslandFlyPlayerData> playerDataMap = playerDataManager.getPlayerDataMap();

            playerDataMap.entrySet().stream()
                    .filter(entry -> {
                        UUID uuid = entry.getKey();
                        Player player = islandFlyAddon.getServer().getPlayer(uuid);
                        if(player == null) return false;
                        IslandFlyPlayerData islandFlyPlayerData = entry.getValue();

                        return player.getAllowFlight() && player.isFlying() && islandFlyPlayerData.isTimedFlightEnabled();
                    })
                    .forEach(entry -> {
                        UUID uuid = entry.getKey();
                        Player player = islandFlyAddon.getServer().getPlayer(uuid);
                        if(player == null) return;
                        User user = User.getInstance(uuid);
                        IslandFlyPlayerData islandFlyPlayerData = entry.getValue();

                        switch (islandFlyPlayerData.getTimeSeconds()) {
                            case 30, 15, 10, 5, 4, 3, 2, 1 -> {
                                user.sendMessage("islandfly.flight-time-warning", TextVariables.NUMBER, String.valueOf(islandFlyPlayerData.getTimeSeconds()));

                                islandFlyPlayerData.removeTimeSeconds(1);

                                bossBarManager.updateBossBar(player);
                            }
                            default -> {
                                if(islandFlyPlayerData.getTimeSeconds() <= 0) {
                                    user.sendMessage("islandfly.flight-time-ended");
                                    islandFlyPlayerData.setTimedFlight(false);
                                    player.setFlying(false);
                                    player.setAllowFlight(false);
                                    bossBarManager.removeBossBar(player);

                                    playerDataManager.savePlayerData(islandFlyPlayerData);
                                } else {
                                    islandFlyPlayerData.removeTimeSeconds(1);

                                    bossBarManager.updateBossBar(player);
                                }
                            }
                        }
                    });
        }, 20L, 20L);
    }

    /**
     * Stops the flight time task.
     */
    public void stopFlightTimeTask() {
        if(flightTimeTask == null || flightTimeTask.isCancelled()) return;

        flightTimeTask.cancel();
        flightTimeTask = null;
    }

    /**
     * Start the task that saves player data.
     */
    public void startSaveTask() {
        saveTask = islandFlyAddon.getServer().getScheduler().runTaskTimer(islandFlyAddon.getPlugin(), () -> {
            @NotNull Map<@NotNull UUID, @NotNull IslandFlyPlayerData> playerDataMap = playerDataManager.getPlayerDataMap();

            playerDataMap.forEach((uuid, islandFlyPlayerData) -> playerDataManager.savePlayerData(islandFlyPlayerData));
        }, 20L * 600, 20L * 600);
    }

    /**
     * Stop the task that saves player data.
     */
    public void stopSaveTask() {
        if(saveTask == null || saveTask.isCancelled()) return;

        saveTask.cancel();
        saveTask = null;
    }
}
