package me.youhavetrouble.notjustnameplates.nameplates;

import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class Nameplate {

    private Component name;
    private final UUID playerUuid;
    private float heightOffset = 0.7f;
    private Color backgroundColor = null;
    private Display.Billboard billboard = Display.Billboard.CENTER;
    private TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.CENTER;
    private boolean visibleForOwner = true;

    private TextDisplay textDisplay;

    public Nameplate(@NotNull UUID playerUuid, Component name) {
        this.playerUuid = playerUuid;
        this.name = name;
    }

    private void createDisplayEntity() {
        if (textDisplay != null && !textDisplay.isDead()) return;
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        this.textDisplay = (TextDisplay) player.getWorld().spawnEntity(
                player.getEyeLocation(),
                EntityType.TEXT_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
                    TextDisplay textDisplay = (TextDisplay) entity;
                    textDisplay.setInvulnerable(true);
                    textDisplay.setPersistent(false);
                    textDisplay.setAlignment(alignment);
                    textDisplay.setBillboard(billboard);
                    textDisplay.setShadowRadius(0);
                    if (this.backgroundColor != null) textDisplay.setBackgroundColor(this.backgroundColor);
                    textDisplay.setTransformation(
                            new Transformation(
                                    new Vector3f(0, heightOffset, 0), // offset
                                    new AxisAngle4f(0, 0, 0, 0), // left rotation
                                    new Vector3f(1, 1, 1), // scale
                                    new AxisAngle4f(0, 0, 0, 0) // right rotation
                            ));
                });
        if (!this.visibleForOwner) {
            player.hideEntity(NotJustNameplates.getInstance(), textDisplay);
        }
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

    public void setBillboard(@NotNull Display.Billboard billboard) {
        this.billboard = billboard;
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.setBillboard(billboard);
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    public void setAlignment(@NotNull TextDisplay.TextAlignment alignment) {
        this.alignment = alignment;
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.setAlignment(alignment);
    }

    public TextDisplay.TextAlignment getAlignment() {
        return alignment;
    }

    public void setBackgroundColor(@Nullable Color color) {
        this.backgroundColor = color;
        if (textDisplay == null || textDisplay.isDead()) return;
        if (this.backgroundColor == null) {
            textDisplay.setDefaultBackground(true);
            return;
        }
        textDisplay.setBackgroundColor(this.backgroundColor);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setVisibleForOwner(boolean visible) {
        this.visibleForOwner = visible;
        if (textDisplay == null || textDisplay.isDead()) return;
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        if (visible) {
            player.showEntity(NotJustNameplates.getInstance(), textDisplay);
        } else {
            player.hideEntity(NotJustNameplates.getInstance(), textDisplay);
        }
    }

    public boolean isVisibleForOwner() {
        return this.visibleForOwner;
    }

    /**
     * Set height offset from the player's eye location
     */
    public void setHeightOffset(float heightOffset) {
        this.heightOffset = heightOffset;
    }

    public float getHeightOffset() {
        return heightOffset;
    }

    /**
     * Update the nameplate position
     */
    public void update() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || player.isDead() || name == null) {
            remove();
            return;
        }
        createDisplayEntity();
        if (!player.getPassengers().contains(textDisplay)) {
            player.addPassenger(textDisplay);
        }
        textDisplay.text(this.name);
    }

    protected void remove() {
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.remove();
    }

}
