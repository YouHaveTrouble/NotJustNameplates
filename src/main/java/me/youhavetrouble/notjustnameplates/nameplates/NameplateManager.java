package me.youhavetrouble.notjustnameplates.nameplates;

import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import net.minecraft.world.phys.AABB;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.purpurmc.purpur.event.entity.EntityTeleportHinderedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameplateManager implements Listener {

    private final HashMap<UUID, Nameplate> nameplates = new HashMap<>();

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
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!event.hasChangedPosition()) return;

        Nameplate nameplate = nameplates.get(event.getPlayer().getUniqueId());
        if (nameplate == null) return;

        CraftPlayer craftPlayer = (CraftPlayer) player;
        AABB playerBox = craftPlayer.getHandle().getBoundingBox();
        World world = player.getWorld();
        Location loc1 = new Location(world, playerBox.maxX, playerBox.maxY, playerBox.maxZ);
        Location loc2 = loc1.clone().subtract(0, 1, 0);
        Location loc3 = new Location(world, playerBox.minX, playerBox.minY, playerBox.minZ);
        Location loc4 = loc3.clone().add(0, 1, 0);

        boolean inPortal = false;
        for (Location loc : new Location[]{loc1, loc2, loc3, loc4}) {
            Block block = loc.getBlock();
            if (
                    block.getType() == Material.NETHER_PORTAL
                    || block.getType() == Material.END_PORTAL
                    || block.getType() == Material.END_GATEWAY
            ) {
                inPortal = true;
                break;
            }
        }

        if (inPortal) {
            nameplate.remove();
            nameplate.forceHide = true;
            return;
        }

        nameplate.forceHide = false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleportHindered(EntityTeleportHinderedEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getReason() != EntityTeleportHinderedEvent.Reason.IS_VEHICLE) return;
        Nameplate nameplate = nameplates.get(player.getUniqueId());
        if (nameplate == null) return;
        nameplate.remove();
        event.setShouldRetry(true);
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

}
