package net.okocraft.biomelocations.command.subcommand;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.localization.MiniMessageLocalization;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.okocraft.biomelocations.data.BiomeLocationData;
import net.okocraft.biomelocations.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
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

    private final MiniMessageLocalization localization;
    private final Function<UUID, BiomeLocationData> biomeLocationDataAccessor;

    public TellLocationCommand(@NotNull MiniMessageLocalization localization, @NotNull Function<UUID, BiomeLocationData> biomeLocationDataAccessor) {
        this.localization = localization;
        this.biomeLocationDataAccessor = biomeLocationDataAccessor;
    }

    @Override
    public @NotNull MiniMessageBase help() {
        return Messages.COMMAND_TELL_LOCATION_HELP;
    }

    @Override
    public @NotNull String permissionNode() {
        return "biomelocations.command.tell-location";
    }

    @Override
    public void run(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        var senderSource = this.localization.findSource(sender);

        if (args.length < 3) {
            this.help().source(senderSource).send(sender);
            return;
        }

        var target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            Messages.COMMAND_GENERAL_PLAYER_NOT_FOUND.apply(args[1]).source(senderSource).send(sender);
            return;
        }

        Key biomeKey;

        try {
            //noinspection PatternValidation
            biomeKey = Key.key(args[2]);
        } catch (InvalidKeyException e) {
            Messages.COMMAND_TELL_LOCATION_INVALID_BIOME_KEY.apply(args[2]).source(senderSource).send(sender);
            return;
        }

        var data = this.biomeLocationDataAccessor.apply(target.getWorld().getUID());

        if (data == null || data.isUnavailable()) {
            Messages.COMMAND_TELL_LOCATION_NOT_AVAILABLE.apply(target.getWorld().getName()).source(senderSource).send(sender);
            return;
        }

        var locations = data.getLocations(biomeKey);
        var targetSource = this.localization.findSource(target);

        if (locations.isEmpty()) {
            Messages.COMMAND_TELL_LOCATION_NOT_FOUND.apply(biomeKey).source(targetSource).send(target);
        } else {
            var randomSelectedPos = locations.get(ThreadLocalRandom.current().nextInt(0, locations.size()));
            Messages.COMMAND_TELL_LOCATION_SUCCESS.apply(biomeKey, randomSelectedPos.x(), randomSelectedPos.z()).source(targetSource).send(target);
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
            return Registry.BIOME.stream()
                    .map(Biome::getKey)
                    .filter(key -> key.asString().startsWith(filter) || key.asMinimalString().startsWith(filter))
                    .map(Key::asString)
                    .toList();
        }

        return Collections.emptyList();
    }
}
