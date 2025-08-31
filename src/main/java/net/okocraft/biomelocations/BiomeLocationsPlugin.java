package net.okocraft.biomelocations;

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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unused")
public final class BiomeLocationsPlugin extends JavaPlugin {

    private static Logger logger = NOPLogger.NOP_LOGGER;
    private static Logger debug = NOPLogger.NOP_LOGGER;

    public static @NotNull Logger logger() {
        return logger;
    }

    public static @NotNull Logger debug() {
        return debug;
    }

    private final Map<UUID, BiomeLocationData> worldDataMap = new ConcurrentHashMap<>();
    private Config config;
    private BiomeLocationsCommand command;

    public BiomeLocationsPlugin() {
        logger = this.getSLF4JLogger();
    }

    @Override
    public void onLoad() {
        try {
            this.config = Config.loadFromYamlFile(this.getDataFolder().toPath().resolve("config.yml"));
        } catch (IOException e) {
            logger().error("Failed to load config.yml", e);
            return;
        }

        if (this.config.debug()) {
            debug = this.getSLF4JLogger();
        }

        try {
            this.loadMessages();
        } catch (IOException e) {
            logger().error("Failed to load messages.", e);
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
        debug().info("Collecting worlds...");
        var queue = this.collectWorlds();
        this.getServer().getAsyncScheduler().runNow(this, ignoredTask -> this.runInitializeWorldDataTask(queue));
    }

    private void runInitializeWorldDataTask(@NotNull Queue<WorldInfo> queue) {
        WorldInfo info;

        var cacheDirectory = this.getDataFolder().toPath().resolve("cache");

        while ((info = queue.poll()) != null) {
            var data = new BiomeLocationData(info);
            this.worldDataMap.put(info.uid(), data);

            try {
                data.loadCacheOrCollectBiomes(cacheDirectory, this.config.searchDistance(), this.config.createBiomeFilter(), this.config.minimumBiomeDistance(), this.config.debug());
            } catch (IOException e) {
                this.getSLF4JLogger().error("Failed to load/save the cache file (world: {})", info.name(), e);
            } catch (RuntimeException e) {
                this.getSLF4JLogger().error("Unexpected exception occurred while initializing the biome location data for world {}", info.name(), e);
            }
        }
    }

    private @NotNull Queue<WorldInfo> collectWorlds() {
        var queue = new ConcurrentLinkedQueue<WorldInfo>();
        var ignoringWorldPatterns = this.config.ignoringWorldPatterns();

        for (var world : this.getServer().getWorlds()) {
            var name = world.getName();
            if (ignoringWorldPatterns.stream().anyMatch(pattern -> pattern.matcher(name).matches())) {
                continue;
            }
            queue.offer(WorldInfo.create(world));
        }

        return queue;
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

    private static @NotNull Locale getLocaleFrom(@Nullable Object object) {
        if (object instanceof Locale locale) {
            return locale;
        } else if (object instanceof Player player) {
            return player.locale();
        } else {
            return Locale.getDefault();
        }
    }
}
