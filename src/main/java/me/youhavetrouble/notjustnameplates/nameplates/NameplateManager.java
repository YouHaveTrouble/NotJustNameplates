package me.youhavetrouble.notjustnameplates.nameplates;

import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.*;

public class NameplateManager implements Listener {

    private final Map<UUID, Nameplate> nameplates = new HashMap<>();
    private final Set<UUID> playersWithNameplatesOff = new HashSet<>();

    public NameplateManager(NotJustNameplates plugin) {
        reloadNameplates();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> nameplates.values().forEach(Nameplate::update), 0, 1);

        // Remove all orphan nameplates
        Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            if (!(entity instanceof TextDisplay textDisplay)) return;
            if (!textDisplay.getPersistentDataContainer().has(Nameplate.NAMEPLATE_KEY)) return;
            for (Nameplate nameplate : nameplates.values()) {
                if (nameplate.getEntity() == textDisplay) return;
            }
            textDisplay.remove();
        })), 100, 100);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID joinerUuid = event.getPlayer().getUniqueId();
        DisplayContent displayContent = NotJustNameplates.getInstance().getDisplayContentForPlayerBasedOnPermission(event.getPlayer());
        if (displayContent == null) return;
        nameplates.put(joinerUuid, new Nameplate(joinerUuid, displayContent));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Nameplate nameplate = nameplates.get(event.getPlayer().getUniqueId());
        if (nameplate == null) return;
        nameplate.remove();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Nameplate nameplate = nameplates.get(player.getUniqueId());
        if (nameplate == null) return;
        if (nameplate.getContent().getCurrentFrame().sneakOverride() == null) return;
        nameplate.update();
    }

    public void reloadNameplates() {
        this.nameplates.values().forEach(Nameplate::remove);
        this.nameplates.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            DisplayContent displayContent = NotJustNameplates.getPluginConfig().getDisplayContent("default");
            for (Map.Entry<String, DisplayContent> entry : NotJustNameplates.getPluginConfig().getDisplayContents().entrySet()) {
                if (player.hasPermission("notjustnameplates.display." + entry.getKey())) {
                    displayContent = entry.getValue();
                    break;
                }
            }
            nameplates.put(player.getUniqueId(), new Nameplate(player.getUniqueId(), displayContent));
        }
    }

    public Map<UUID, Nameplate> getNameplates() {
        return Collections.unmodifiableMap(nameplates);
    }

    public Set<UUID> getPlayersWithNameplatesOff() {
        return playersWithNameplatesOff;
    }

    /**
     * Hide or show nameplates of other players for a player
     * @param player the player to hide nameplates for
     * @param hide true to hide, false to show
     */
    public void nameplatesToggle(Player player, boolean hide) {
        if (hide) {
            playersWithNameplatesOff.add(player.getUniqueId());
            nameplates.values().forEach(nameplate -> {
                if (nameplate.playerUuid.equals(player.getUniqueId())) return;
                TextDisplay display = nameplate.getEntity();
                if (display == null) return;
                player.hideEntity(NotJustNameplates.getInstance(), display);
            });
            return;
        }
        playersWithNameplatesOff.remove(player.getUniqueId());
        nameplates.values().forEach(nameplate -> {
            if (nameplate.playerUuid.equals(player.getUniqueId())) return;
            TextDisplay display = nameplate.getEntity();
            if (display == null) return;
            player.showEntity(NotJustNameplates.getInstance(), display);
        });
    }

}
