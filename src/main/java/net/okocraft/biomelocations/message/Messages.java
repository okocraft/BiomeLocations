package net.okocraft.biomelocations.message;

import dev.siroshun.mcmsgdef.MessageKey;
import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Messages {

    private static final Map<String, String> DEFAULT_MESSAGES = new LinkedHashMap<>();

    private static final Placeholder<String> ARGUMENT_PLACEHOLDER = arg -> Argument.string("argument", arg);
    private static final Placeholder<String> PERMISSION_PLACEHOLDER = permissionNode -> Argument.string("permission", permissionNode);
    private static final Placeholder<Key> BIOME_PLACEHOLDER = key -> Argument.component("biome", Component.translatable("biome.minecraft." + key.value()));
    private static final Placeholder<String> WORLD_PLACEHOLDER = world -> Argument.string("world", world);
    private static final Placeholder<Integer> X_PLACEHOLDER = x -> Argument.numeric("x", x);
    private static final Placeholder<Integer> Z_PLACEHOLDER = z -> Argument.numeric("z", z);
    private static final Placeholder<Component> COMMANDLINE_PLACEHOLDER = commandline -> Argument.component("commandline", commandline);
    private static final Placeholder<Component> HELP_PLACEHOLDER = help -> Argument.component("help", help);

    public static final MessageKey.Arg1<String> NO_PERMISSION = MessageKey.arg1(def("no-permission", "<gray>You don't have the permission: <aqua><permission>"), PERMISSION_PLACEHOLDER);

    public static final MessageKey.Arg1<String> COMMAND_GENERAL_PLAYER_NOT_FOUND = MessageKey.arg1(def("command.general.player-not-found", "<red>The player named <aqua><argument><red> cannot be found."), ARGUMENT_PLACEHOLDER);

    public static final MessageKey COMMAND_HELP_HEADER = MessageKey.key(def("command.help.header", "<dark_gray><st>======================<reset><gold><b> BiomeLocations <reset><dark_gray><st>======================"));
    private static final String COMMAND_HELP_LINE_KEY = def("command.help.line", "<aqua><commandline><dark_gray>: <help>");
    public static final Component COMMAND_HELP_HELP = help(def("command.help.commandline", "/biomelocations help"), def("command.help.help", "<gray>Shows this help."));

    public static final MessageKey.Arg1<String> COMMAND_TELL_LOCATION_INVALID_BIOME_KEY = MessageKey.arg1(def("command.tell-location.invalid-biome-key", "<red>The specified biome key '<aqua><argument><red>' is invalid."), ARGUMENT_PLACEHOLDER);
    public static final MessageKey.Arg1<String> COMMAND_TELL_LOCATION_NOT_AVAILABLE = MessageKey.arg1(def("command.tell-location.not-available", "<red>This command is not available in world <aqua><world><red>."), WORLD_PLACEHOLDER);
    public static final MessageKey.Arg1<Key> COMMAND_TELL_LOCATION_NOT_FOUND = MessageKey.arg1(def("command.tell-location.not-found", "<red>The biome <aqua><biome><red> is not found in the world."), BIOME_PLACEHOLDER);
    public static final MessageKey.Arg3<Key, Integer, Integer> COMMAND_TELL_LOCATION_SUCCESS = MessageKey.arg3(def("command.tell-location.success", "<gray>The biome <aqua><biome><gray> is at (<aqua><x>, <z><gray>)."), BIOME_PLACEHOLDER, X_PLACEHOLDER, Z_PLACEHOLDER);
    public static final Component COMMAND_TELL_LOCATION_HELP = help(def("command.tell-location.commandline", "/biomelocations telllocation <player> <biome>"), def("command.tell-location.help", "<gray>Shows the known random location of the biome."));

    @Contract("_, _ -> param1")
    private static @NotNull String def(@NotNull String key, @NotNull String msg) {
        DEFAULT_MESSAGES.put(key, msg);
        return key;
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<String, String> defaultMessages() {
        return Collections.unmodifiableMap(DEFAULT_MESSAGES);
    }

    private static @NotNull Component help(String commandlineKey, String helpKey) {
        return MessageKey.arg2(COMMAND_HELP_LINE_KEY, COMMANDLINE_PLACEHOLDER, HELP_PLACEHOLDER).apply(Component.translatable(commandlineKey), Component.translatable(helpKey));
    }

    private Messages() {
        throw new UnsupportedOperationException();
    }
}
