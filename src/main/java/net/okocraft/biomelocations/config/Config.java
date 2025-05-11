package net.okocraft.biomelocations.config;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import dev.siroshun.configapi.serialization.record.RecordSerialization;
import dev.siroshun.serialization.annotation.CollectionType;
import dev.siroshun.serialization.annotation.DefaultBoolean;
import dev.siroshun.serialization.annotation.DefaultInt;
import dev.siroshun.serialization.annotation.DefaultMethod;
import dev.siroshun.serialization.core.key.KeyGenerator;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public record Config(
        @DefaultMethod(clazz = Config.class, name = "defaultIgnoringWorlds") @CollectionType(String.class) Set<String> ignoringWorlds,
        @DefaultMethod(clazz = Config.class, name = "defaultIgnoringBiomes") @CollectionType(String.class) Set<String> ignoringBiomes,
        @DefaultInt(64) int searchDistance,
        @DefaultInt(500) int minimumBiomeDistance,
        @DefaultBoolean(false) boolean debug
) {

    private static final RecordSerialization<Config> SERIALIZATION = RecordSerialization.builder(Config.class).keyGenerator(KeyGenerator.CAMEL_TO_KEBAB).build();

    public static @NotNull Config loadFromYamlFile(@NotNull Path filepath) throws IOException {
        if (Files.isRegularFile(filepath)) {
            return SERIALIZATION.deserializer().deserialize(YamlFormat.DEFAULT.load(filepath));
        } else {
            var config = SERIALIZATION.deserializer().deserialize(MapNode.empty());
            YamlFormat.COMMENT_PROCESSING.save(SERIALIZATION.serializer().serialize(config), filepath);
            return config;
        }
    }

    @Contract(value = " -> new", pure = true)
    private static @NotNull @Unmodifiable Set<String> defaultIgnoringWorlds() {
        return Set.of("ignoring_world_name");
    }

    @Contract(value = " -> new", pure = true)
    private static @NotNull @Unmodifiable Set<String> defaultIgnoringBiomes() {
        return Set.of("minecraft:ocean");
    }

    public @NotNull Predicate<Key> createBiomeFilter() {
        var patterns = this.ignoringBiomes.stream().map(Pattern::compile).map(Pattern::asMatchPredicate).toList();
        return key -> {
            var strKey = key.asString();
            return this.ignoringBiomes.contains(strKey) || patterns.stream().anyMatch(pattern -> pattern.test(strKey));
        };
    }

    public List<Pattern> ignoringWorldPatterns() {
        return this.ignoringWorlds.stream().map(Pattern::compile).toList();
    }
}
