package net.okocraft.biomelocations.message;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg3;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg3.arg3;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.withTagResolverBase;
import static com.github.siroshun09.messages.minimessage.base.Placeholder.component;
import static com.github.siroshun09.messages.minimessage.base.Placeholder.messageBase;

public final class Messages {

    private static final Map<String, String> DEFAULT_MESSAGES = new LinkedHashMap<>();

    private static final Placeholder<String> ARGUMENT_PLACEHOLDER = component("argument", Component::text);
    private static final Placeholder<String> PERMISSION_PLACEHOLDER = component("permission", Component::text);
    private static final Placeholder<Key> BIOME_PLACEHOLDER = component("biome", key -> Component.translatable("biome.minecraft." + key.value()));
    private static final Placeholder<String> WORLD_PLACEHOLDER = component("world", Component::text);
    private static final Placeholder<Integer> X_PLACEHOLDER = component("x", Component::text);
    private static final Placeholder<Integer> Z_PLACEHOLDER = component("z", Component::text);
    private static final Placeholder<String> COMMANDLINE_PLACEHOLDER = messageBase("commandline", MiniMessageBase::messageKey);
    private static final Placeholder<String> HELP_PLACEHOLDER = messageBase("help", MiniMessageBase::messageKey);

    public static final Arg1<String> NO_PERMISSION = arg1(def("no-permission", "<gray>You don't have the permission: <aqua><permission>"), PERMISSION_PLACEHOLDER);

    public static final Arg1<String> COMMAND_GENERAL_PLAYER_NOT_FOUND = arg1(def("command.general.player-not-found", "<red>The player named <aqua><argument><red> cannot be found."), ARGUMENT_PLACEHOLDER);

    public static final MiniMessageBase COMMAND_HELP_HEADER = withTagResolverBase(def("command.help.header", "<dark_gray><st>======================<reset><gold><b> BiomeLocations <reset><dark_gray><st>======================"));
    private static final String COMMAND_HELP_LINE_KEY = def("command.help.line", "<aqua><commandline><dark_gray>: <help>");
    public static final MiniMessageBase COMMAND_HELP_HELP = help(def("command.help.commandline", "/biomelocations help"), def("command.help.help", "<gray>Shows this help."));

    public static final Arg1<String> COMMAND_TELL_LOCATION_INVALID_BIOME_KEY = arg1(def("command.tell-location.invalid-biome-key", "<red>The specified biome key '<aqua><argument><red>' is invalid."), ARGUMENT_PLACEHOLDER);
    public static final Arg1<String> COMMAND_TELL_LOCATION_NOT_AVAILABLE = arg1(def("command.tell-location.not-available", "<red>This command is not available in world <aqua><world><red>."), WORLD_PLACEHOLDER);
    public static final Arg1<Key> COMMAND_TELL_LOCATION_NOT_FOUND = arg1(def("command.tell-location.not-found", "<red>The biome <aqua><biome><red> is not found in the world."), BIOME_PLACEHOLDER);
    public static final Arg3<Key, Integer, Integer> COMMAND_TELL_LOCATION_SUCCESS = arg3(def("command.tell-location.success", "<gray>The biome <aqua><biome><gray> is at (<aqua><x>, <z><gray>)."), BIOME_PLACEHOLDER, X_PLACEHOLDER, Z_PLACEHOLDER);
    public static final MiniMessageBase COMMAND_TELL_LOCATION_HELP = help(def("command.tell-location.commandline", "/biomelocations telllocation <player> <biome>"), def("command.tell-location.help", "<gray>Shows the known random location of the biome."));

    @Contract("_, _ -> param1")
    private static @NotNull String def(@NotNull String key, @NotNull String msg) {
        DEFAULT_MESSAGES.put(key, msg);
        return key;
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<String, String> defaultMessages() {
        return Collections.unmodifiableMap(DEFAULT_MESSAGES);
    }

    private static @NotNull MiniMessageBase help(String commandlineKey, String helpKey) {
        return MiniMessageBase.withTagResolverBase(COMMAND_HELP_LINE_KEY, COMMANDLINE_PLACEHOLDER.apply(commandlineKey), HELP_PLACEHOLDER.apply(helpKey));
    }

    private Messages() {
        throw new UnsupportedOperationException();
    }
}
