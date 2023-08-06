package me.youhavetrouble.notjustnameplates.text;

import me.clip.placeholderapi.PlaceholderAPI;
import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TextParser {

    public static Component parseWithPlaceholders(String input, Player player) {
        MiniMessage miniMessage = MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolvers(
                                StandardTags.defaults(),
                                placeholderTag(player))
                        .build())
                .build();
        return miniMessage.deserialize(input);
    }

    public static @NotNull TagResolver placeholderTag(final @NotNull Player player) {
        return TagResolver.resolver("placeholder", (argumentQueue, context) -> {
            final String placeholder = argumentQueue.popOr("placeholder tag requires an argument").value();
            switch (placeholder) {
                case "name" -> {
                    return Tag.selfClosingInserting(player.name());
                }
                case "displayname" -> {
                    return Tag.selfClosingInserting(player.displayName());
                }
                default -> {
                    if (!NotJustNameplates.isPapiHooked()) return Tag.selfClosingInserting(Component.text(placeholder));

                    final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + placeholder + '%');

                    if (parsedPlaceholder.contains(LegacyComponentSerializer.SECTION_CHAR + "")) {
                        Component componentPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);
                        return Tag.selfClosingInserting(componentPlaceholder);
                    }

                    return Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(parsedPlaceholder));
                }
            }

        });
    }

}
