package me.youhavetrouble.notjustnameplates.text;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TextParser {
    private static final Map<String, String> legacyFormatConversions = new HashMap<>() {
        {
            put("&0", "<black>");
            put("&1", "<dark_blue>");
            put("&2", "<dark_green>");
            put("&3", "<dark_aqua>");
            put("&4", "<dark_red>");
            put("&5", "<dark_purple>");
            put("&6", "<gold>");
            put("&7", "<gray>");
            put("&8", "<dark_gray>");
            put("&9", "<blue>");
            put("&a", "<green>");
            put("&b", "<aqua>");
            put("&c", "<red>");
            put("&d", "<light_purple>");
            put("&e", "<yellow>");
            put("&f", "<white>");
            put("&k", "<obf>");
            put("&l", "<b>");
            put("&m", "<st>");
            put("&n", "<u>");
            put("&o", "<i>");
            put("&r", "<reset>");
        }
    };

    public static Component parseWithPlaceholders(String input, Player player) {
        Integer tagOpener = null;
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);

            switch (character) {
                case '<':
                    tagOpener = i;
                    break;
                case '>':
                    if (tagOpener != null) {
                        String tag = input.substring(tagOpener, i + 1);
                        String[] tagData = input.substring(tagOpener + 1, i).split(":");
                        
                        if (tagData[0].equals("placeholder") && tagData.length > 1) {
                            String parsedPlaceholder = "";
                            switch (tagData[1]) {
                                case "name":
                                    parsedPlaceholder = MiniMessage.miniMessage().serialize(player.name());
                                    break;
                                case "displayname":
                                    parsedPlaceholder = MiniMessage.miniMessage().serialize(player.displayName());
                                    break;
                                default:
                                    parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + tagData[1] + '%');
                            }

                            input = input.replaceFirst(tag, parsedPlaceholder);
                            i += parsedPlaceholder.length() - tag.length();
                        }
                    }
            }
        }

        for (Map.Entry<String, String> color : legacyFormatConversions.entrySet()) {
            input = input.replace(color.getKey(), color.getValue());
        }

        return MiniMessage.miniMessage().deserialize(input);
    }
}
