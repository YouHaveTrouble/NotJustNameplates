package me.youhavetrouble.notjustnameplates.nameplates;

import de.myzelyam.api.vanish.VanishAPI;
import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import me.youhavetrouble.notjustnameplates.text.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.kitteh.vanish.VanishPlugin;

import java.util.UUID;

public class Nameplate {

    public static final NamespacedKey NAMEPLATE_KEY = new NamespacedKey(NotJustNameplates.getInstance(), "nameplate");

    protected boolean forceHide = false;
    private DisplayContent content;
    public final UUID playerUuid;
    private TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.CENTER;
    private boolean visibleForOwner = false;

    private TextDisplay textDisplay;

    public Nameplate(@NotNull UUID playerUuid, DisplayContent content) {
        this.playerUuid = playerUuid;
        this.content = content;
    }

    protected TextDisplay getEntity() {
        return textDisplay;
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
                    textDisplay.getPersistentDataContainer().set(NAMEPLATE_KEY, PersistentDataType.STRING, player.getName());
                    textDisplay.setInvulnerable(true);
                    textDisplay.setPersistent(false);
                    textDisplay.setAlignment(alignment);
                    textDisplay.setBillboard(this.content.getBillboard());
                    textDisplay.setSeeThrough(this.content.getSeeThrough());
                    textDisplay.setViewRange(content.getViewRange());
                    textDisplay.setShadowRadius(0);
                    textDisplay.setInterpolationDuration(content.getInterpolationDuration());
                    textDisplay.setInterpolationDelay(content.getInterpolationDelay());
                    textDisplay.setShadowed(content.getCurrentFrame().shadowed());
                    textDisplay.setTextOpacity(content.getCurrentFrame().textOpacity());

                    Color backgroundColor = this.content.getCurrentFrame().backgroundColor();
                    if (backgroundColor != null) textDisplay.setBackgroundColor(backgroundColor);

                    textDisplay.text(parseText(this.content.getCurrentFrame().text(), player));

                    textDisplay.setTransformation(new Transformation(
                            content.getCurrentFrame().offset(),
                            new AxisAngle4f(0, 0, 0, 0), // left rotation
                            content.getCurrentFrame().scale(),
                            new AxisAngle4f(0, 0, 0, 0) // right rotation
                    ));

                    NotJustNameplates.getInstance().getNameplateManager().getPlayersWithNameplatesOff().forEach(uuid -> {
                        Player playerWithNameplatesOff = Bukkit.getPlayer(uuid);
                        if (playerWithNameplatesOff != null) {
                            playerWithNameplatesOff.hideEntity(NotJustNameplates.getInstance(), textDisplay);
                        }
                    });

                });
        if (!this.visibleForOwner) {
            player.hideEntity(NotJustNameplates.getInstance(), textDisplay);
        }
        player.addPassenger(textDisplay);
    }

    public void setContent(DisplayContent content) {
        if (content == null || this.content == content) return;
        this.content = content;
        Bukkit.getScheduler().runTask(NotJustNameplates.getInstance(), this::remove);
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
        if (forceHide) {
            remove();
            return;
        }
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || player.isDead()) {
            remove();
            return;
        }
        if (content.getCurrentFrame().text() == null) {
            remove();
            return;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            remove();
            return;
        }
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            remove();
            return;
        }

        if (NotJustNameplates.isSuperVanishHooked() && VanishAPI.isInvisible(player)) {
            remove();
            return;
        }

        if (NotJustNameplates.isVanishNoPacketHooked()) {
            VanishPlugin vanishPlugin = (VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket");
            if (vanishPlugin != null && vanishPlugin.getManager().isVanished(player)) {
                remove();
                return;
            }
        }

        createDisplayEntity();
        if (textDisplay == null || textDisplay.isDead()) return;
        if (!player.getPassengers().contains(textDisplay)) {
            player.addPassenger(textDisplay);
        }

        textDisplay.text(parseText(this.content.getCurrentFrame().text(), player));

        textDisplay.setBillboard(this.content.getBillboard());
        textDisplay.setShadowed(content.getCurrentFrame().shadowed());
        textDisplay.setTextOpacity(content.getCurrentFrame().textOpacity());
        textDisplay.setTransformation(new Transformation(
                content.getCurrentFrame().offset(),
                new AxisAngle4f(0, 0, 0, 0), // left rotation
                content.getCurrentFrame().scale(),
                new AxisAngle4f(0, 0, 0, 0) // right rotation
        ));

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

        if (player == null || !player.isOnline()) return Component.empty();

        return TextParser.parseWithPlaceholders(text, player);
    }


}
