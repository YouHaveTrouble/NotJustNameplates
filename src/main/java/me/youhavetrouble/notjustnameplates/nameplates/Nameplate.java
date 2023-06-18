package me.youhavetrouble.notjustnameplates.nameplates;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Nameplate {

    private Component name;
    private final UUID playerUuid;
    private double heightOffset = 0.5;

    public Nameplate(@NotNull UUID playerUuid, @NotNull Component name) {
        this.playerUuid = playerUuid;
        this.name = name;
    }

    /**
     * Get content of the nameplate
     * @return content of the nameplate
     */
    public Component getName() {
        return name;
    }

    /**
     * Set content of the nameplate
     */
    public void setName(Component name) {
        this.name = name;
    }

    /**
     * Set height offset from the player's eye location
     */
    public void setHeightOffset(double heightOffset) {
        this.heightOffset = heightOffset;
    }

    /**
     * Update the nameplate position
     */
    public void updatePosition() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        Location location = player
                .getEyeLocation()
                .clone()
                .add(0, heightOffset, 0);

        // TODO move fake entity to location

    }

}
