package me.youhavetrouble.notjustnameplates;

import me.youhavetrouble.notjustnameplates.displays.DisplayContent;
import me.youhavetrouble.notjustnameplates.displays.DisplayFrame;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class NJNConfig {

    private final NotJustNameplates plugin;

    private final LinkedHashMap<String, DisplayContent> displayContents = new LinkedHashMap<>();

    public final String noPermissionMessage, configReloadedMessage;

    protected NJNConfig(NotJustNameplates plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().getPermissions().forEach(permission -> {
            if (permission.getName().startsWith("notjustnameplates.display.")) {
                plugin.getServer().getPluginManager().removePermission(permission);
            }
        });

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) {
            messagesSection = config.createSection("messages");
            plugin.getLogger().severe("No messages section found in config! Correct your config and reload.");
        }

        noPermissionMessage = messagesSection.getString("no-permission", "<red>You do not have permission to use this.");
        configReloadedMessage = messagesSection.getString("config-reloaded", "<aqua>NJN Config reloaded.");


        ConfigurationSection namePlatesSection = config.getConfigurationSection("nameplates");
        if (namePlatesSection == null) {
            plugin.getLogger().severe("No nameplates section found in config! Correct your config and reload.");
            return;
        }

        for (String sectionName : namePlatesSection.getKeys(false)) {
            ConfigurationSection displayContentSection = namePlatesSection.getConfigurationSection(sectionName);
            if (displayContentSection == null) continue;
            DisplayContent displayContent = createDisplayContent(displayContentSection);
            if (displayContent == null) continue;
            displayContents.put(sectionName, displayContent);
        }
    }


    public DisplayContent getDisplayContent(String name) {
        return displayContents.get(name);
    }

    public HashMap<String, DisplayContent> getDisplayContents() {
        return displayContents;
    }

    private DisplayContent createDisplayContent(ConfigurationSection displayContentSection) {

        ConfigurationSection framesSection = displayContentSection.getConfigurationSection("frames");
        if (framesSection == null) {
            plugin.getLogger().severe("No frames section found in " + displayContentSection.getName());
            return null;
        }

        DisplayContent displayContent = new DisplayContent();

        displayContent.setRefreshRate(displayContentSection.getInt("refresh-rate", 0));
        displayContent.setSeeThrough(displayContentSection.getBoolean("see-through", false));
        displayContent.setInterpolationDelay(displayContentSection.getInt("interpolation-delay", displayContent.getRefreshRate()));
        displayContent.setInterpolationDuration(displayContentSection.getInt("interpolation-duration", displayContent.getRefreshRate()));

        Display.Billboard billboard = Display.Billboard.HORIZONTAL;
        try {
            billboard = Display.Billboard.valueOf(displayContentSection.getString("billboard", "horizontal").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid billboard type in " + displayContentSection.getName() + ": " + displayContentSection.getString("billboard")+". Using horizontal.");
        }
        displayContent.setBillboard(billboard);

        framesSection.getKeys(false).forEach(frameName -> {
            ConfigurationSection frameSection = framesSection.getConfigurationSection(frameName);
            if (frameSection == null) return;
            String text = frameSection.getString("text", null);
            String backgroundColor = frameSection.getString("background");
            float scaleX = (float) frameSection.getDouble("scale-x", 1);
            float scaleY = (float) frameSection.getDouble("scale-y", 1);
            float scaleZ = (float) frameSection.getDouble("scale-z", 1);
            displayContent.addFrame(new DisplayFrame(text, colorFromHex(backgroundColor), scaleX, scaleY, scaleZ));
        });
        Permission permission = new Permission("notjustnameplates.display." + displayContentSection.getName(), "Allows player to use " + displayContentSection.getName() + " nameplate", PermissionDefault.FALSE);
        plugin.getServer().getPluginManager().addPermission(permission);
        return displayContent;
    }

    private Color colorFromHex(@Nullable String hex) {
        if (hex == null) return null;
        if (!hex.startsWith("#")) {
            plugin.getLogger().warning("Invalid hex color: " + hex + " (does not start with '#')");
            return null;
        }

        hex = hex.substring(1); // Remove the '#' character

        int r, g, b, a;

        return switch (hex.length()) {
            case 3 -> {
                r = Integer.parseInt(String.valueOf(hex.charAt(0) + hex.charAt(0)), 16);
                g = Integer.parseInt(String.valueOf(hex.charAt(1) + hex.charAt(1)), 16);
                b = Integer.parseInt(String.valueOf(hex.charAt(2) + hex.charAt(2)), 16);
                yield Color.fromRGB(r, g, b);
            }
            case 6 -> {
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
                yield Color.fromRGB(r, g, b);
            }
            case 8 -> {
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
                a = Integer.parseInt(hex.substring(6, 8), 16);
                yield Color.fromARGB(a, r, g, b);
            }
            default -> {
                plugin.getLogger().warning("Invalid hex color: " + hex + " (invalid length)");
                yield null;
            }
        };
    }

}
