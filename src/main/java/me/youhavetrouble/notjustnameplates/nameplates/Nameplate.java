package me.youhavetrouble.notjustnameplates.nameplates;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class Nameplate {

    private Component name;
    private final UUID playerUuid;
    private float heightOffset = 0.75f;

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
                            textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                            textDisplay.setBillboard(Display.Billboard.CENTER);
                            textDisplay.setInvulnerable(true);
                            textDisplay.setTransformation(
                                    new Transformation(
                                            new Vector3f(0, heightOffset, 0), // offset
                                            new AxisAngle4f(0, 0, 0, 0), // left rotation
                                            new Vector3f(1, 1, 1), // scale
                                            new AxisAngle4f(0, 0, 0, 0) // right rotation
                                    ));
                        });
        player.addPassenger(textDisplay);
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
    public void setHeightOffset(float heightOffset) {
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
    public void update() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        if (player.isDead()) {
            remove();
            return;
        }
        createDisplayEntity();
        if (!player.getPassengers().contains(textDisplay)) {
            player.addPassenger(textDisplay);
        }
    }

    protected void remove() {
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.remove();
    }

}
