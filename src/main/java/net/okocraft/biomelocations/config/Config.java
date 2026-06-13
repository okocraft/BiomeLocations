package net.okocraft.biomelocations.config;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@NullMarked
@ConfigSerializable
public class Config {

    public static Config loadFrom(Path filepath) throws ConfigurateException {
        return YamlConfigurationLoader.builder().path(filepath).build().load().get(Config.class, new Config());
    }

    public Set<String> ignoringWorlds = Set.of("minecraft:ignored_world");
    public Set<String> ignoringBiomes = Set.of("minecraft:ocean");
    public int searchDistance = 64;
    public int maximumRadius = 5000;
    public int minimumBiomeDistance = 500;

    public Predicate<Key> createBiomeFilter() {
        List<Predicate<String>> patterns = this.ignoringBiomes.stream().map(Pattern::compile).map(Pattern::asMatchPredicate).toList();
        return key -> {
            String strKey = key.asString();
            return this.ignoringBiomes.contains(strKey) || patterns.stream().anyMatch(pattern -> pattern.test(strKey));
        };
    }

    public List<Pattern> ignoringWorldPatterns() {
        return this.ignoringWorlds.stream().map(Pattern::compile).toList();
    }

    @PostProcess
    public void postProcess() {
        this.searchDistance = Math.max(this.searchDistance, 1);
        this.maximumRadius = Math.max(this.maximumRadius, 1);
        this.minimumBiomeDistance = Math.max(this.minimumBiomeDistance, 1);
    }
}
