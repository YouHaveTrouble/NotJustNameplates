package me.youhavetrouble.notjustnameplates;

import me.youhavetrouble.notjustnameplates.commands.MainCommand;
import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import me.youhavetrouble.notjustnameplates.nameplates.NameplateManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.permissions.DefaultPermissions;

import java.util.Map;

public final class NotJustNameplates extends JavaPlugin {

    private static NotJustNameplates instance;
    private static NJNConfig config;
    private static long time = Long.MIN_VALUE;

    private final TeamManager teamManager = new TeamManager();
    private NameplateManager nameplateManager = null;

    private static boolean papiHook, superVanishHook, vanishNoPacketHook = false;

    @Override
    public void onEnable() {
        instance = this;

        config = new NJNConfig(this);
        checkHooks();

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

            if (time % 20 != 0) return;
            updateNameplatesBasedOnPermission();
        }, 1, 1);
    }

    public void reloadPluginConfig() {
        config = new NJNConfig(this);
        checkHooks();
        nameplateManager.reloadNameplates();
        updateNameplatesBasedOnPermission();
    }

    private void updateNameplatesBasedOnPermission() {
        nameplateManager.getNameplates().forEach(((uuid, nameplate) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            nameplate.setContent(getDisplayContentForPlayerBasedOnPermission(player));
        }));
    }

    public DisplayContent getDisplayContentForPlayerBasedOnPermission(Player player) {
        if (player == null) return null;
        for (Map.Entry<String, DisplayContent> entry : config.getDisplayContents().entrySet()) {
            String id = entry.getKey();
            if (id.equalsIgnoreCase("default")) continue;
            if (player.hasPermission("notjustnameplates.display." + id)) {
                return entry.getValue();
            }
        }
        return config.getDisplayContent("default");
    }

    private void checkHooks() {
        papiHook = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        superVanishHook = Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish");
        vanishNoPacketHook = Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket");
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

    public static boolean isPapiHooked() {
        return papiHook;
    }

    public static boolean isSuperVanishHooked() {
        return superVanishHook;
    }

    public static boolean isVanishNoPacketHooked() {
        return vanishNoPacketHook;
    }
}
