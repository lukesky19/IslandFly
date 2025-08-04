package world.bentobox.islandfly.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandfly.IslandFlyAddon;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.level.Level;

import java.util.UUID;

public class FlightCheckManager {

    private final IslandFlyAddon islandFlyAddon;
    private final Database<IslandFlyPlayerData> islandFlyPlayerDatabase;

    public FlightCheckManager(IslandFlyAddon islandFlyAddon, Database<IslandFlyPlayerData> islandFlyPlayerDatabase) {
        this.islandFlyAddon = islandFlyAddon;
        this.islandFlyPlayerDatabase = islandFlyPlayerDatabase;
    }

    public boolean isUserOp(User user) {
        return user.isOp();
    }

    public boolean isUserCreativeOrSpectator(User user) {
        GameMode gameMode = user.getPlayer().getGameMode();
        return gameMode.equals(GameMode.CREATIVE) || gameMode.equals(GameMode.SPECTATOR);
    }

    public boolean canUserBypassFly(User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.flybypass");
    }

    public boolean isIslandSpawnIsland(Island island) {
        return island.isSpawn();
    }

    public boolean canUserFlySpawn(User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.flyspawn");
    }

    public boolean canUserUseFly(User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.fly");
    }

    public boolean canUserUseTempFly(User user) {
        String permPrefix = islandFlyAddon.getPlugin().getIWM().getPermissionPrefix(user.getWorld());
        return user.hasPermission(permPrefix + "island.tempfly") && doesUserHaveFlightTime(user);
    }

    public boolean doesUserHaveFlightTime(@NotNull User user) {
        UUID uuid = user.getUniqueId();

        if(islandFlyPlayerDatabase.objectExists(uuid.toString())) {
            IslandFlyPlayerData islandFlyPlayerData = islandFlyPlayerDatabase.loadObject(uuid.toString());
            if(islandFlyPlayerData == null) return false;

            return islandFlyPlayerData.getTimeSeconds() > 0;
        }

        return false;
    }

    public @Nullable Island getIslandUserIsOn(User user) {
        return islandFlyAddon.getIslands().getProtectedIslandAt(user.getLocation()).orElse(null);
    }

    public boolean canUserFlyIslandLevel(Island island) {
        Level levelAddon = islandFlyAddon.getLevelAddon();
        if(levelAddon == null) return true;
        if(islandFlyAddon.getSettings().getFlyMinLevel() <= 0) return true;
        return levelAddon.getIslandLevel(island.getWorld(), island.getOwner()) >= islandFlyAddon.getSettings().getFlyMinLevel();
    }

    public boolean canUserFlyOnIsland(Island island, User user) {
        return island.isAllowed(user, IslandFlyAddon.ISLAND_FLY_PROTECTION);
    }

    public boolean isPlayerAllowedFlight(Player player) {
        return player.getAllowFlight();
    }

    public boolean isUserWorldGamemodeWorld(User user) {
        return islandFlyAddon.getPlugin().getIWM().getAddon(user.getWorld()).isPresent();
    }
}
