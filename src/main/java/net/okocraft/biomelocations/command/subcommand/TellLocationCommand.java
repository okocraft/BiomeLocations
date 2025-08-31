package net.okocraft.biomelocations.command.subcommand;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.biomelocations.data.BiomeLocationData;
import net.okocraft.biomelocations.message.Messages;
import net.okocraft.biomelocations.util.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class TellLocationCommand implements SubCommand {

    private final Function<UUID, BiomeLocationData> biomeLocationDataAccessor;

    public TellLocationCommand(@NotNull Function<UUID, BiomeLocationData> biomeLocationDataAccessor) {
        this.biomeLocationDataAccessor = biomeLocationDataAccessor;
    }

    @Override
    public @NotNull Component help() {
        return Messages.COMMAND_TELL_LOCATION_HELP;
    }

    @Override
    public @NotNull String permissionNode() {
        return "biomelocations.command.tell-location";
    }

    @Override
    public void run(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {

        if (args.length < 3) {
            sender.sendMessage(this.help());
            return;
        }

        var target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(Messages.COMMAND_GENERAL_PLAYER_NOT_FOUND.apply(args[1]));
            return;
        }

        Key biomeKey;

        try {
            //noinspection PatternValidation
            biomeKey = Key.key(args[2]);
        } catch (InvalidKeyException e) {
            sender.sendMessage(Messages.COMMAND_TELL_LOCATION_INVALID_BIOME_KEY.apply(args[2]));
            return;
        }

        var data = this.biomeLocationDataAccessor.apply(target.getWorld().getUID());

        if (data == null || data.isUnavailable()) {
            sender.sendMessage(Messages.COMMAND_TELL_LOCATION_NOT_AVAILABLE.apply(target.getWorld().getName()));
            return;
        }

        var locations = data.getLocations(biomeKey);

        if (locations.isEmpty()) {
            target.sendMessage(Messages.COMMAND_TELL_LOCATION_NOT_FOUND.apply(biomeKey));
        } else {
            BlockPos randomSelectedPos = locations.get(ThreadLocalRandom.current().nextInt(0, locations.size()));
            sender.sendMessage(Messages.COMMAND_TELL_LOCATION_SUCCESS.apply(biomeKey, randomSelectedPos.x(), randomSelectedPos.z()));
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2) {
            var filter = args[1].toLowerCase(Locale.ENGLISH);
            return Bukkit.getOnlinePlayers().stream()
                    .map(CommandSender::getName)
                    .filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(filter))
                    .toList();
        }

        if (args.length == 3) {
            var filter = args[2].toLowerCase(Locale.ENGLISH);
            return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)
                    .stream()
                    .map(Biome::getKey)
                    .filter(key -> key.asString().startsWith(filter) || key.asMinimalString().startsWith(filter))
                    .map(Key::asString)
                    .toList();
        }

        return Collections.emptyList();
    }
}
