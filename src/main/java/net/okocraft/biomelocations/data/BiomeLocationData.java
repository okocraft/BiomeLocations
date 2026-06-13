package net.okocraft.biomelocations.data;

import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.collection.ListCodec;
import dev.siroshun.codec4j.api.codec.collection.MapCodec;
import dev.siroshun.codec4j.api.error.DecodeError;
import dev.siroshun.codec4j.api.error.EncodeError;
import dev.siroshun.codec4j.api.file.FileIO;
import dev.siroshun.codec4j.io.gson.GsonIO;
import dev.siroshun.codec4j.io.gzip.GzipIO;
import dev.siroshun.jfun.result.Result;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public record BiomeLocationData(@NotNull Map<Key, List<BiomePos>> biomeLocationMap) {

    private static final Codec<Key> KEY_CODEC = Codec.STRING.flatXmap(
        key -> Result.success(key.asMinimalString()),
        key -> {
            try {
                return Result.success(Key.key(key));
            } catch (InvalidKeyException e) {
                return Result.failure(DecodeError.invalidChar(key));
            }
        }
    );

    private static final Codec<BiomeLocationData> CODEC = MapCodec.create(KEY_CODEC, ListCodec.create(BiomePos.CODEC)).xmap(BiomeLocationData::biomeLocationMap, BiomeLocationData::new);

    private static final String CACHE_EXTENSION = ".json.gz";
    private static final FileIO CACHE_FORMAT = GzipIO.bestCompression(GsonIO.DEFAULT);

    public static @NotNull Path createCacheFilepath(@NotNull Path cacheDirectory, @NotNull WorldInfo worldInfo) {
        return cacheDirectory.resolve(worldInfo.uid() + CACHE_EXTENSION);
    }

    public static @NotNull Result<BiomeLocationData, DecodeError> loadCache(@NotNull Path cacheFilepath) {
        return CACHE_FORMAT.decodeFrom(cacheFilepath, CODEC);
    }


    public static @NotNull Result<Void, EncodeError> saveCache(@NotNull Path cacheFilepath, @NotNull BiomeLocationData data) {
        return CACHE_FORMAT.encodeTo(cacheFilepath, CODEC, data);
    }

    public static @NotNull Result<BiomeLocationData, DecodeError> generate(WorldInfo worldInfo,
                                                                           int searchDistance, int maxRadius,
                                                                           @NotNull Predicate<Key> biomeFilter, int minimumBiomeDistance) {
        World world = Bukkit.getWorld(worldInfo.uid());
        if (world == null) {
            return Result.failure(DecodeError.failure("Could not find the world: " + worldInfo.name()));
        }

        BiomeLocationCollector collector = new BiomeLocationCollector(biomeFilter, minimumBiomeDistance);

        int centerX = worldInfo.centerX();
        int centerZ = worldInfo.centerZ();
        int radius = (int) Math.floor(Math.min(worldInfo.radius(), maxRadius));
        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;
        BiomeProvider provider = world.vanillaBiomeProvider();

        collectBiomes(searchDistance, minX, maxX, minZ, maxZ, collector, provider);

        return Result.success(new BiomeLocationData(collector.getResult()));
    }

    @SuppressWarnings("DataFlowIssue")
    private static void collectBiomes(int searchDistance, int minX, int maxX, int minZ, int maxZ, BiomeLocationCollector collector, BiomeProvider provider) {
        for (int x = minX; x < maxX; x += searchDistance) {
            for (int z = minZ; z < maxZ; z += searchDistance) {
                collector.accept(provider.getBiome(null, x, 64, z).key(), new BiomePos(x, z));
            }
        }
    }

    public @NotNull @Unmodifiable List<BiomePos> getLocations(@NotNull Key key) {
        return Collections.unmodifiableList(this.biomeLocationMap.getOrDefault(key, Collections.emptyList()));
    }
}
