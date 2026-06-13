package net.okocraft.biomelocations;

import dev.siroshun.codec4j.api.error.DecodeError;
import dev.siroshun.jfun.result.Result;
import dev.siroshun.mcmsgdef.directory.DirectorySource;
import dev.siroshun.mcmsgdef.directory.MessageProcessors;
import dev.siroshun.mcmsgdef.file.PropertiesFile;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.okocraft.biomelocations.command.BiomeLocationsCommand;
import net.okocraft.biomelocations.command.subcommand.TellLocationCommand;
import net.okocraft.biomelocations.config.Config;
import net.okocraft.biomelocations.data.BiomeLocationData;
import net.okocraft.biomelocations.data.WorldInfo;
import net.okocraft.biomelocations.message.Messages;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class BiomeLocationsPlugin extends JavaPlugin {

    private final Map<UUID, BiomeLocationData> worldDataMap = new ConcurrentHashMap<>();
    private Config config;
    private BiomeLocationsCommand command;

    @Override
    public void onLoad() {
        try {
            this.config = Config.loadFromYamlFile(this.getDataFolder().toPath().resolve("config.yml"));
        } catch (IOException e) {
            this.getSLF4JLogger().error("Failed to load config.yml", e);
            return;
        }
        try {
            this.loadMessages();
        } catch (IOException e) {
            this.getSLF4JLogger().error("Failed to load messages.", e);
        }
    }

    @Override
    public void onEnable() {
        if (this.config == null) { // the config is failed to load
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.command =
            new BiomeLocationsCommand()
                .addSubCommand("telllocation", new TellLocationCommand(this.worldDataMap::get))
                .register(this.getName().toLowerCase(Locale.ENGLISH));

        this.getServer().getGlobalRegionScheduler().runDelayed(this, this::runCollectWorldsTask, 1L);
    }

    @Override
    public void onDisable() {
        if (this.command != null) {
            this.command.unregister();
        }
    }

    private void runCollectWorldsTask(@NotNull ScheduledTask ignored) {
        this.getServer().getAsyncScheduler().runNow(this, ignoredTask -> this.runInitializeWorldDataTask(this.collectWorlds()));
    }

    private void runInitializeWorldDataTask(@NotNull List<WorldInfo> queue) {
        Path cacheDirectory = this.getDataFolder().toPath().resolve("cache");
        if (!Files.isDirectory(cacheDirectory)) {
            try {
                Files.createDirectories(cacheDirectory);
            } catch (IOException e) {
                this.getSLF4JLogger().error("Failed to create cache directory", e);
                return;
            }
        }

        for (WorldInfo info : queue) {
            Path cacheFilepath = BiomeLocationData.createCacheFilepath(cacheDirectory, info);
            if (Files.isRegularFile(cacheFilepath)) {
                Result<BiomeLocationData, DecodeError> result = BiomeLocationData.loadCache(cacheFilepath);
                if (result.isSuccess()) {
                    this.worldDataMap.put(info.uid(), result.unwrap());
                    continue;
                }

                this.getSLF4JLogger().warn("Failed to load biome data from cache (world: {}): {}", info.name(), result.unwrapError());
            }

            BiomeLocationData.generate(info, this.config.searchDistance(), 5000, this.config.createBiomeFilter(), this.config.minimumBiomeDistance())
                .inspect(result -> {
                    this.worldDataMap.put(info.uid(), result);
                    BiomeLocationData.saveCache(cacheFilepath, result)
                        .inspect(_ -> this.getSLF4JLogger().info("Biome data is generated (world: {})", info.name()))
                        .inspectError(error -> this.getSLF4JLogger().warn("Failed to save biome data to cache (world: {}): {}", info.name(), error));
                })
                .inspectError(error -> this.getSLF4JLogger().error("Failed to generate the biome data (world: {}): {}", info.name(), error));
        }
    }

    private @NotNull List<WorldInfo> collectWorlds() {
        List<WorldInfo> list = new ArrayList<>();
        List<Pattern> ignoringWorldPatterns = this.config.ignoringWorldPatterns();

        for (World world : this.getServer().getWorlds()) {
            String name = world.getName();
            if (ignoringWorldPatterns.stream().anyMatch(pattern -> pattern.matcher(name).matches())) {
                continue;
            }
            list.add(WorldInfo.create(world));
        }

        return list;
    }

    private void loadMessages() throws IOException {
        DirectorySource.propertiesFiles(this.getDataPath().resolve("languages"))
            .defaultLocale(Locale.ENGLISH, Locale.JAPANESE)
            .messageProcessor(MessageProcessors.appendMissingMessagesToPropertiesFile(this::loadDefaultMessageMap))
            .loadAndRegister(Key.key("biomelocations", "languages"));
    }

    private @Nullable Map<String, String> loadDefaultMessageMap(@NotNull Locale locale) throws IOException {
        if (locale.equals(Locale.ENGLISH)) {
            return Messages.defaultMessages();
        } else {
            try (var input = this.getResource(locale + ".properties")) {
                return input != null ? PropertiesFile.load(input) : null;
            }
        }
    }
}
