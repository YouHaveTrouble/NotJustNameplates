package me.youhavetrouble.notjustnameplates;

import me.youhavetrouble.notjustnameplates.listeners.TeamManagementListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotJustNameplates extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new TeamManagementListener(), this);
    }

}
