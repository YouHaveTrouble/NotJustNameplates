package me.youhavetrouble.notjustnameplates.nameplates;

import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TeamManagementListener implements Listener {

    private final Scoreboard scoreboard = new Scoreboard();
    private final PlayerTeam team = new PlayerTeam(scoreboard, "notjustnameplates");
    private final HashMap<String, Nameplate> players = new HashMap<>();

    public TeamManagementListener(NotJustNameplates plugin) {
        team.setNameTagVisibility(PlayerTeam.Visibility.NEVER);
        team.setCollisionRule(PlayerTeam.CollisionRule.ALWAYS);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> players.values().forEach(Nameplate::update), 0, 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        DisplayContent displayContent = NotJustNameplates.getPluginConfig().getDisplayContent("default");
        players.put(joiner.getName(), new Nameplate(joiner.getUniqueId(), displayContent != null ? displayContent : new DisplayContent()));
        for (Player player : event.getPlayer().getServer().getOnlinePlayers()) {
            addPlayerToTeam(joiner, player);
        }
        sendTeamMembers(joiner);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player leaver = event.getPlayer();
        Nameplate nameplate = players.get(leaver.getName());
        nameplate.remove();
        players.remove(leaver.getName());
        for (Player player : event.getPlayer().getServer().getOnlinePlayers()) {
            removePlayerFromTeam(leaver, player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Nameplate nameplate = players.get(event.getPlayer().getName());
        if (nameplate == null) return;
        nameplate.remove();
    }

    public void reloadTeams() {
        this.players.values().forEach(Nameplate::remove);
        this.players.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            DisplayContent displayContent = NotJustNameplates.getPluginConfig().getDisplayContent("default");
            players.put(player.getName(), new Nameplate(player.getUniqueId(), displayContent != null ? displayContent : new DisplayContent()));
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTeamMembers(player);
        }
    }

    private void addPlayerToTeam(@NotNull Player player, @NotNull Player target) {
        CraftPlayer craftPlayer = (CraftPlayer) target;
        ClientboundSetPlayerTeamPacket teamCreatePacket = ClientboundSetPlayerTeamPacket
                .createAddOrModifyPacket(team, true);
        craftPlayer.getHandle().connection.send(teamCreatePacket);
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket
                .createPlayerPacket(team, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD);
        craftPlayer.getHandle().connection.send(packet);
    }

    private void removePlayerFromTeam(@NotNull Player player, @NotNull Player target) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket
                .createPlayerPacket(team, player.getName(), ClientboundSetPlayerTeamPacket.Action.REMOVE);
        CraftPlayer craftPlayer = (CraftPlayer) target;
        craftPlayer.getHandle().connection.send(packet);
    }

    private void sendTeamMembers(@NotNull Player target) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket
                .createMultiplePlayerPacket(team, players.keySet(), ClientboundSetPlayerTeamPacket.Action.ADD);
        CraftPlayer craftPlayer = (CraftPlayer) target;
        craftPlayer.getHandle().connection.send(packet);
    }

}