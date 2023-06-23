package me.youhavetrouble.notjustnameplates;

import me.youhavetrouble.notjustnameplates.nameplates.TeamManagementListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotJustNameplates extends JavaPlugin {

    private static NotJustNameplates instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new TeamManagementListener(this), this);
    }

    public static NotJustNameplates getInstance() {
        return instance;
    }

}
