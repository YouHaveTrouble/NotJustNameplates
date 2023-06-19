package me.youhavetrouble.notjustnameplates.nameplates;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Nameplate {

    private Component name;
    private final UUID playerUuid;
    private double heightOffset = 0.5;

    private TextDisplay textDisplay;

    public Nameplate(@NotNull UUID playerUuid, @NotNull Component name) {
        this.playerUuid = playerUuid;
        this.name = name;
    }

    private void createDisplayEntity() {
        if (textDisplay != null && !textDisplay.isDead()) return;
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        this.textDisplay = (TextDisplay) player.getWorld()
                .spawnEntity(
                        calculateCurrentLocation(),
                        EntityType.TEXT_DISPLAY,
                        CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
                            TextDisplay textDisplay = (TextDisplay) entity;
                            textDisplay.setCustomNameVisible(true);
                            textDisplay.setSilent(true);
                            textDisplay.setInvulnerable(true);
                            textDisplay.setPersistent(false);
                            textDisplay.text(this.name);
                        });
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
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.text(name);
    }

    /**
     * Set height offset from the player's eye location
     */
    public void setHeightOffset(double heightOffset) {
        this.heightOffset = heightOffset;
    }

    public Location calculateCurrentLocation() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return null;
        return player.getEyeLocation().add(0, heightOffset, 0);
    }

    /**
     * Update the nameplate position
     */
    public void updatePosition() {
        createDisplayEntity();
        Location location = calculateCurrentLocation();
        this.textDisplay.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
