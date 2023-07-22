package me.youhavetrouble.notjustnameplates.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;

public class PAPIHook {

    public static Component setPlaceholders(Component component, OfflinePlayer offlinePlayer) {
        component = component.replaceText(TextReplacementConfig.builder()
                .match(PlaceholderAPI.getPlaceholderPattern())
                .replacement((matchResult, builder) -> LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(offlinePlayer, matchResult.group(0))))
                .build());
        return component;
    }

}
