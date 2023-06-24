package me.youhavetrouble.notjustnameplates.nameplates;

import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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

    private DisplayContent content;
    private final UUID playerUuid;
    private final float heightOffset = 0.7f;
    private TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.CENTER;
    private boolean visibleForOwner = false;

    private TextDisplay textDisplay;

    public Nameplate(@NotNull UUID playerUuid, DisplayContent content) {
        this.playerUuid = playerUuid;
        this.content = content;
    }

    private void createDisplayEntity() {
        if (textDisplay != null && !textDisplay.isDead()) return;
        if (this.content == null) return;
        if (content.getCurrentFrame().text() == null) return;
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
                    textDisplay.setBillboard(this.content.getBillboard());
                    textDisplay.setShadowRadius(0);

                    Color backgroundColor = this.content.getCurrentFrame().backgroundColor();
                    if (backgroundColor != null) textDisplay.setBackgroundColor(backgroundColor);

                    textDisplay.text(parseText(this.content.getCurrentFrame().text(), player));

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

    public void setContent(@NotNull DisplayContent content) {
        this.content = content;
    }

    public void setAlignment(@NotNull TextDisplay.TextAlignment alignment) {
        this.alignment = alignment;
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.setAlignment(alignment);
    }

    public TextDisplay.TextAlignment getAlignment() {
        return alignment;
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
     * Update the nameplate position
     */
    public void update() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || player.isDead() || content.getCurrentFrame().text() == null) {
            remove();
            return;
        }
        createDisplayEntity();
        if (textDisplay == null || textDisplay.isDead()) return;
        if (!player.getPassengers().contains(textDisplay)) {
            player.addPassenger(textDisplay);
        }

        textDisplay.text(parseText(this.content.getCurrentFrame().text(), player));

        textDisplay.setBillboard(this.content.getBillboard());
        textDisplay.setInterpolationDuration(content.getRefreshRate());

        Color backgroundColor = this.content.getCurrentFrame().backgroundColor();
        if (backgroundColor == null) {
            textDisplay.setDefaultBackground(true);
        } else {
            textDisplay.setBackgroundColor(backgroundColor);
        }

        setVisibleForOwner(this.visibleForOwner || player.hasPermission("notjustnameplates.seeown"));

    }

    protected void remove() {
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.remove();
    }

    private Component parseText(String text, Player player) {

        Component component = MiniMessage.miniMessage().deserialize(text);

        if (player == null || !player.isOnline()) return component;

        component = component.replaceText(builder -> {
            builder.matchLiteral("%displayname%");
            builder.replacement(player.displayName());
        });

        return component;
    }

}
