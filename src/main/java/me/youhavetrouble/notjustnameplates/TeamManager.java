package me.youhavetrouble.notjustnameplates;

import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class TeamManager implements Listener {

    private final Scoreboard scoreboard = new Scoreboard();
    private final PlayerTeam team = new PlayerTeam(scoreboard, "notjustnameplates");

    private final Set<String> players = new HashSet<>();

    public TeamManager() {
        team.setNameTagVisibility(PlayerTeam.Visibility.NEVER);

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            // put player names that played in the last week in the team
            if (player.getLastSeen() > System.currentTimeMillis() - 604800000L) {
                players.add(player.getName());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!players.add(player.getName())) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> sendAddPlayerToTeam(player, onlinePlayer));
        }
        sendTeamCreatePacket(player);
        Bukkit.getScheduler().runTaskAsynchronously(NotJustNameplates.getInstance(), () -> sendTeamMembers(player));
    }

    public void sendTeamCreatePacket(@NotNull Player target) {
        CraftPlayer craftPlayer = (CraftPlayer) target;
        ClientboundSetPlayerTeamPacket teamCreatePacket = ClientboundSetPlayerTeamPacket
                .createAddOrModifyPacket(team, true);
        craftPlayer.getHandle().connection.send(teamCreatePacket);
    }

    private void sendAddPlayerToTeam(@NotNull Player player, @NotNull Player target) {
        CraftPlayer craftPlayer = (CraftPlayer) target;
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket
                .createPlayerPacket(team, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD);
        craftPlayer.getHandle().connection.send(packet);
    }

    private void sendRemovePlayerFromTeam(@NotNull Player playerToRemove, @NotNull Player target) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket
                .createPlayerPacket(team, playerToRemove.getName(), ClientboundSetPlayerTeamPacket.Action.REMOVE);
        CraftPlayer craftPlayer = (CraftPlayer) target;
        craftPlayer.getHandle().connection.send(packet);
    }

    private void sendTeamMembers(@NotNull Player target) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket
                .createMultiplePlayerPacket(team, players, ClientboundSetPlayerTeamPacket.Action.ADD);
        CraftPlayer craftPlayer = (CraftPlayer) target;
        craftPlayer.getHandle().connection.send(packet);
    }



}
