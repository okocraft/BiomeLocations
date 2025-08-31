package net.okocraft.biomelocations.command;

import net.okocraft.biomelocations.command.subcommand.HelpCommand;
import net.okocraft.biomelocations.command.subcommand.SubCommand;
import net.okocraft.biomelocations.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BiomeLocationsCommand extends Command implements TabCompleter {

    private final HelpCommand helpCommand;

    private final Map<String, SubCommand> subCommandMap = new ConcurrentHashMap<>();

    public BiomeLocationsCommand() {
        super("biomelocations");
        this.helpCommand = new HelpCommand(() -> this.subCommandMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue));

        this.subCommandMap.put("help", this.helpCommand);
    }

    public BiomeLocationsCommand register(String prefix) {
        Bukkit.getCommandMap().register(this.getLabel(), prefix, this);
        return this;
    }

    public void unregister() {
        var knownCommands = Bukkit.getCommandMap().getKnownCommands();

        Set.copyOf(knownCommands.entrySet()).stream()
                .filter(entry -> this.equals(entry.getValue()))
                .forEach(entry -> knownCommands.remove(entry.getKey()));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("biomelocations.command")) {
            sender.sendMessage(Messages.NO_PERMISSION.apply("biomelocations.command"));
            return true;
        }

        SubCommand subCommand;

        if (args.length == 0) {
            subCommand = this.helpCommand;
        } else {
            subCommand = this.subCommandMap.getOrDefault(args[0].toLowerCase(Locale.ENGLISH), this.helpCommand);
        }

        if (sender.hasPermission(subCommand.permissionNode())) {
            subCommand.run(sender, args);
        } else {
            sender.sendMessage(Messages.NO_PERMISSION.apply(subCommand.permissionNode()));
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0 || !sender.hasPermission("biomelocations.command")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return this.subCommandMap.keySet()
                    .stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                    .filter(cmd -> sender.hasPermission(this.subCommandMap.get(cmd).permissionNode()))
                    .toList();
        }

        var subCommand = this.subCommandMap.get(args[0].toLowerCase(Locale.ENGLISH));

        if (subCommand != null && sender.hasPermission(subCommand.permissionNode())) {
            return subCommand.tabComplete(sender, args);
        } else {
            return Collections.emptyList();
        }
    }

    public @NotNull BiomeLocationsCommand addSubCommand(@NotNull String name, @NotNull SubCommand subCommand) {
        this.subCommandMap.put(name.toLowerCase(Locale.ENGLISH), subCommand);
        return this;
    }
}
