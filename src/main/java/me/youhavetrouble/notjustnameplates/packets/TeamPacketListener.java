package me.youhavetrouble.notjustnameplates.packets;

import io.netty.channel.*;
import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Locale;

public class TeamPacketListener extends ChannelDuplexHandler {

    public static final String HANDLER_NAME = "njn_team_packet_handler";

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof ClientboundSetPlayerTeamPacket originalPacket)) {
            super.write(ctx, msg, promise);
            return;
        }
        // if nametag visibility is already never, no need to override the packet
        if (originalPacket.getParameters().isPresent()) {
            ClientboundSetPlayerTeamPacket.Parameters parameters = originalPacket.getParameters().get();
            Team.OptionStatus nametagVisibility = Team.OptionStatus.valueOf(parameters.getNametagVisibility().toUpperCase(Locale.ENGLISH));
            if (nametagVisibility == Team.OptionStatus.NEVER) {
                super.write(ctx, msg, promise);
                return;
            }
        }

        PacketTeam packetTeam = PacketTeam.create(originalPacket.getName(), originalPacket.getParameters().orElse(null));
        ClientboundSetPlayerTeamPacket newPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(packetTeam, true);
        super.write(ctx, newPacket, promise);
    }


    public static void addPlayer(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Channel channel = craftPlayer.getHandle().connection.connection.channel;
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(HANDLER_NAME) == null) {
            pipeline.addBefore("packet_handler", HANDLER_NAME, new TeamPacketListener());
        }
    }

    public static void removePlayer(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Channel channel = craftPlayer.getHandle().connection.connection.channel;
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(HANDLER_NAME) != null) {
            pipeline.remove(HANDLER_NAME);
        }
    }

}
