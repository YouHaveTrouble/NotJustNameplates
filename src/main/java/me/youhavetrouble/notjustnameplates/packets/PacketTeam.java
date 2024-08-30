package me.youhavetrouble.notjustnameplates.packets;

import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

public class PacketTeam extends PlayerTeam {

    private PacketTeam(String name, ClientboundSetPlayerTeamPacket.Parameters parameters) {
        super(MinecraftServer.getServer().getScoreboard(), name);
        if (parameters != null) {
            Team.CollisionRule collisionRule = Team.CollisionRule.byName(parameters.getCollisionRule());
            if (collisionRule != null) setCollisionRule(collisionRule);
            setColor(parameters.getColor());
            setPlayerPrefix(parameters.getPlayerPrefix());
            setPlayerSuffix(parameters.getPlayerSuffix());
            setDisplayName(parameters.getDisplayName());
            unpackOptions(parameters.getOptions());
        }

        setNameTagVisibility(Team.Visibility.NEVER);
    }

    public static PacketTeam create(String name, @Nullable ClientboundSetPlayerTeamPacket.Parameters parameters) {
        return new PacketTeam(name, parameters);
    }
}
