package world.bentobox.islandfly;

import org.bukkit.Material;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.islandfly.commands.FlightTimeAdminCommand;
import world.bentobox.islandfly.commands.FlightTimePlayerCommand;
import world.bentobox.islandfly.commands.FlyToggleCommand;
import world.bentobox.islandfly.commands.TimedFlyToggleCommand;
import world.bentobox.islandfly.config.Settings;
import world.bentobox.islandfly.database.object.IslandFlyPlayerData;
import world.bentobox.islandfly.listeners.*;
import world.bentobox.islandfly.managers.BossBarManager;
import world.bentobox.islandfly.managers.FlightValidationManager;
import world.bentobox.islandfly.managers.PlayerDataManager;
import world.bentobox.islandfly.managers.TaskManager;
import world.bentobox.level.Level;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IslandFlyAddon main class. Enables addon.
 */
public class IslandFlyAddon extends Addon {
    /**
     * Settings object for IslandFlyAddon
     */
    private Settings settings;

    /**
     * Level addon instance.
     */
    private Level levelAddon;

    /**
     * PlayerDataManager instance
     */
    private PlayerDataManager playerDataManager;
    private BossBarManager bossBarManager;
    private TaskManager taskManager;

    /**
     * A flag to allow or disallow flight on island
     * based on player's rank
     */
    public static final Flag ISLAND_FLY_PROTECTION =
            new Flag.Builder("ISLAND_FLY_PROTECTION", Material.ELYTRA)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.ADVANCED)
            .defaultRank(RanksManager.MEMBER_RANK)
            .defaultSetting(true).build();

    /**
     * Boolean that indicate if addon is hooked into any gamemode.
     */
    private boolean hooked;

    /**
     * Executes code when loading the addon. This is called before {@link #onEnable()}. This should preferably
     * be used to setup configuration and worlds.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        // Save default config.yml
        this.saveDefaultConfig();
        // Load the plugin's config
        this.settings = new Config<>(this, Settings.class).loadConfigObject();
        this.loadSettings();
    }

    /**
     * Executes code when reloading the addon.
     */
    @Override
    public void onReload() {
        super.onReload();

        if(this.hooked) {
            this.loadSettings();
            log("IslandFly addon reloaded.");
        }
    }

    /**
     * Loads addon settings and hooks into available GameModes
     */
    @Override
    public void onEnable() {
        Database<IslandFlyPlayerData> islandFlyPlayerDatabase = new Database<>(this, IslandFlyPlayerData.class);
        playerDataManager = new PlayerDataManager(islandFlyPlayerDatabase);
        bossBarManager = new BossBarManager(playerDataManager);
        taskManager = new TaskManager(this, playerDataManager, bossBarManager);
        FlightValidationManager flightValidationManager = new FlightValidationManager(this, playerDataManager);

        //Hook into gamemodes
        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
            if(!this.getSettings().getDisabledGameModes().contains(gameModeAddon.getDescription().getName())) {
                this.getPlugin().log("Hooking into " + gameModeAddon.getDescription().getName());

                AtomicBoolean playerSuccess = new AtomicBoolean(false);
                AtomicBoolean adminSuccess = new AtomicBoolean(false);
                gameModeAddon.getPlayerCommand().ifPresent(
                        playerCommand -> {
                            new FlyToggleCommand(playerCommand, this, playerDataManager, bossBarManager);
                            new TimedFlyToggleCommand(playerCommand, this, playerDataManager, bossBarManager);
                            new FlightTimePlayerCommand(playerCommand, playerDataManager);
                            playerSuccess.set(true);
                        });
                gameModeAddon.getAdminCommand().ifPresent(
                        adminCommand -> {
                            new FlightTimeAdminCommand(adminCommand, this, playerDataManager);
                            adminSuccess.set(true);
                        });
                if(playerSuccess.get() && adminSuccess.get()) hooked = true;

                ISLAND_FLY_PROTECTION.addGameModeAddon(gameModeAddon);
            }
        });

        if(hooked) {
            // Register Listeners
            this.registerListener(new FlyListener(this, playerDataManager, bossBarManager, flightValidationManager));
            this.registerListener(new FlyDeathListener(this, playerDataManager, bossBarManager));
            this.registerListener(new FlyLogoutListener(this, playerDataManager, bossBarManager));
            this.registerListener(new FlyLoginListener(this, playerDataManager, bossBarManager));
            this.registerListener(new FlyFlagListener(this, playerDataManager, bossBarManager));

            // Register a flag
            this.registerFlag(ISLAND_FLY_PROTECTION);
        }

        taskManager.startFlightTimeTask();
        taskManager.startSaveTask();
    }

    /**
     * Disable addon.
     */
    @Override
    public void onDisable() {
        if(taskManager != null) {
            taskManager.stopFlightTimeTask();
            taskManager.stopSaveTask();
        }

        if(playerDataManager != null) {
            this.getServer().getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();

                playerDataManager.savePlayerData(uuid);

                if(bossBarManager != null) bossBarManager.removeBossBar(player);

                playerDataManager.unloadPlayerData(uuid);
            });
        }
    }

    /**
     * Check addon hooks.
     */
    public void allLoaded() {
        // Try to find Level addon and if it does not exist, display a warning
        this.getAddonByName("Level").ifPresentOrElse(addon -> {
            this.levelAddon = (Level) addon;
            this.log("Level Addon hooked into Level addon.");
        }, () -> this.levelAddon = null);
    }

    /**
     * This method loads addon configuration settings in memory.
     */
    private void loadSettings() {
        if (this.settings == null) {
            // Disable
            this.logError("IslandFly settings could not load! Addon disabled.");
            this.setState(State.DISABLED);
        }
    }

    /**
     * Get addon settings
     * @return settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Gets level addon.
     *
     * @return the level addon
     */
    public Level getLevelAddon()
    {
        return levelAddon;
    }
}
