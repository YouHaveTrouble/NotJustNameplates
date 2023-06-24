package me.youhavetrouble.notjustnameplates.commands;


import me.youhavetrouble.notjustnameplates.NotJustNameplates;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainCommand extends Command {

    public MainCommand(NotJustNameplates plugin) {
        super("njn");
        setPermission("notjustnameplates.command");
        plugin.getServer().getCommandMap().register("njn", this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Component.text("NotJustNameplates " + NotJustNameplates.getInstance().getPluginMeta().getVersion()));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("notjustnameplates.command.reload")) {
                sender.sendMessage(Component.text("You do not have permission to use this command"));
                return true;
            }
            NotJustNameplates.getInstance().reloadPluginConfig();
            sender.sendMessage(Component.text("Reloaded config"));
            return true;
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 0) return completions;
        if (args.length == 1 && sender.hasPermission("notjustnameplates.command.reload")) {
            completions.add("reload");
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}
