package me.youhavetrouble.notjustnameplates;

import me.youhavetrouble.notjustnameplates.commands.MainCommand;
import me.youhavetrouble.notjustnameplates.nameplates.NameplateManager;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.permissions.DefaultPermissions;

public final class NotJustNameplates extends JavaPlugin {

    private static NotJustNameplates instance;
    private static NJNConfig config;
    private static long time = Long.MIN_VALUE;

    private final TeamManager teamManager = new TeamManager();
    private NameplateManager nameplateManager = null;

    @Override
    public void onEnable() {
        instance = this;
        config = new NJNConfig(this);

        DefaultPermissions.registerPermission("notjustnameplates.seeown", "Allows a player to see their own nameplate", PermissionDefault.FALSE);
        DefaultPermissions.registerPermission("notjustnameplates.command", "Allows a player to use the /njn command", PermissionDefault.TRUE);
        DefaultPermissions.registerPermission("notjustnameplates.command.reload", "Allows a player to use the /njn reload command", PermissionDefault.OP);

        this.nameplateManager = new NameplateManager(this);

        getServer().getPluginManager().registerEvents(this.nameplateManager, this);

        new MainCommand(this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            time++;
            if (config == null) return;
            config.getDisplayContents().values().forEach(displayContent -> {
                if (displayContent == null) return;
                if (displayContent.getRefreshRate() <= 0) return;
                if (time % displayContent.getRefreshRate() != 0) return;
                displayContent.advanceFrame();
            });
        }, 1, 1);
    }

    public void reloadPluginConfig() {
        config = new NJNConfig(this);
        nameplateManager.reloadNameplates();
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public NameplateManager getNameplateManager() {
        return nameplateManager;
    }

    public static NotJustNameplates getInstance() {
        return instance;
    }

    public static long getTime() {
        return time;
    }

    public static NJNConfig getPluginConfig() {
        return config;
    }

}
