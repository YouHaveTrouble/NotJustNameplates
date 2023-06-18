package me.youhavetrouble.notjustnameplates.listeners;

import me.youhavetrouble.notjustnameplates.teams.PacketTeam;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;

public class PlayerJoinLeaveListener implements Listener {

    private final Scoreboard scoreboard = new Scoreboard();
    private final PlayerTeam team = new PlayerTeam(scoreboard, "notjustnameplates");
    private final HashSet<String> players = new HashSet<>();

    public PlayerJoinLeaveListener() {
        team.setNameTagVisibility(PlayerTeam.Visibility.NEVER);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        for (Player player : event.getPlayer().getServer().getOnlinePlayers()) {
            addPlayerToTeam(joiner, player);
        }
        PacketTeam.sendTeamMembers(joiner);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player leaver = event.getPlayer();
        for (Player player : event.getPlayer().getServer().getOnlinePlayers()) {
            removePlayerFromTeam(leaver, player);
        }
    }

    private void addPlayerToTeam(Player player, Player target) {
        CraftPlayer craftPlayer = (CraftPlayer) target;
        players.add(player.getName());

        ClientboundSetPlayerTeamPacket teamCreatePacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        craftPlayer.getHandle().connection.send(teamCreatePacket);

        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD);

        craftPlayer.getHandle().connection.send(packet);
    }

    private void removePlayerFromTeam(Player player, Player target) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, player.getName(), ClientboundSetPlayerTeamPacket.Action.REMOVE);
        CraftPlayer craftPlayer = (CraftPlayer) target;
        players.remove(player.getName());
        craftPlayer.getHandle().connection.send(packet);
    }

    private void sendTeamMembers(Player target) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(team, players, ClientboundSetPlayerTeamPacket.Action.ADD);
        CraftPlayer craftPlayer = (CraftPlayer) target;
        craftPlayer.getHandle().connection.send(packet);
    }

}
