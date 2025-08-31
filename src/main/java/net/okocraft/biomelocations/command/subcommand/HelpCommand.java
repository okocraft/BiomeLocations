package net.okocraft.biomelocations.command.subcommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.okocraft.biomelocations.message.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class HelpCommand implements SubCommand {

    private final Supplier<Stream<? extends SubCommand>> subCommandStreamSupplier;

    public HelpCommand(@NotNull Supplier<Stream<? extends SubCommand>> subCommandStreamSupplier) {
        this.subCommandStreamSupplier = subCommandStreamSupplier;
    }

    @Override
    public @NotNull Component help() {
        return Messages.COMMAND_HELP_HELP;
    }

    @Override
    public @NotNull String permissionNode() {
        return "biomelocations.command.help";
    }

    @Override
    public void run(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        TextComponent.Builder result = Component.text().append(Messages.COMMAND_HELP_HEADER);

        this.subCommandStreamSupplier.get()
                .filter(cmd -> sender.hasPermission(cmd.permissionNode()))
                .map(SubCommand::help)
                .forEach(help -> result.append(Component.newline()).append(help));

        sender.sendMessage(result);
    }
}
