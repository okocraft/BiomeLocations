package net.okocraft.biomelocations.data;

import com.github.siroshun09.configapi.core.file.java.binary.BinaryFormat;
import com.github.siroshun09.configapi.core.node.IntArray;
import com.github.siroshun09.configapi.core.node.ListNode;
import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.Node;
import com.github.siroshun09.configapi.core.node.NumberValue;
import com.github.siroshun09.configapi.core.node.StringRepresentable;
import com.github.siroshun09.configapi.core.node.StringValue;
import com.github.siroshun09.configapi.core.serialization.Serialization;
import com.github.siroshun09.configapi.core.serialization.annotation.MapType;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordSerialization;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.kyori.adventure.key.Key;
import net.okocraft.biomelocations.BiomeLocationsPlugin;
import net.okocraft.biomelocations.util.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.generator.BiomeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BiomeLocationData {

    private final WorldInfo worldInfo;
    private volatile Map<Key, List<BlockPos>> biomeLocationMap;

    public BiomeLocationData(@NotNull WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    public void loadCacheOrCollectBiomes(@NotNull Path cacheDirectory, int searchDistance,
                                         @NotNull Predicate<Key> biomeFilter, int minimumBiomeDistance,
                                         boolean dumpToYaml) throws IOException {
        this.biomeLocationMap = null;
        var cacheFilepath = cacheDirectory.resolve(this.worldInfo.uid() + ".dat");

        if (Files.isRegularFile(cacheFilepath)) {
            BiomeLocationsPlugin.debug().info("Using cached biome data for {}", this.worldInfo.name());
            this.biomeLocationMap = Cache.fromMapNode(BinaryFormat.DEFAULT.load(cacheFilepath)).convertArrayToList();
        } else {
            BiomeLocationsPlugin.logger().info("Generating biome data of {}...", this.worldInfo.name());
            var collector = new BiomeLocationCollector(biomeFilter, minimumBiomeDistance);

            var world = Bukkit.getWorld(this.worldInfo.uid());

            if (world == null) {
                BiomeLocationsPlugin.logger().warn("Could not find the world '{}'", this.worldInfo.name());
                return;
            }

            this.collectBiomes(searchDistance, collector, world.vanillaBiomeProvider());

            this.biomeLocationMap = collector.getResult();

            BiomeLocationsPlugin.debug().info("Saving biome data of {}...", this.worldInfo.name());
            var node = Cache.createCache(collector.getResult()).toMapNode();
            BinaryFormat.DEFAULT.save(node, cacheFilepath);

            if (dumpToYaml) {
                YamlFormat.DEFAULT.save(node, cacheDirectory.resolve(this.worldInfo.uid() + ".debug.yml"));
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void collectBiomes(int searchDistance, BiomeLocationCollector collector, @NotNull BiomeProvider provider) {
        var center = this.worldInfo.center();
        int radius = this.worldInfo.radius();
        int minX = center.x() - radius;
        int minZ = center.z() - radius;
        int maxX = center.x() + radius;
        int maxZ = center.z() + radius;
        int y = center.y();

        for (int x = minX; x < maxX; x += searchDistance) {
            for (int z = minZ; z < maxZ; z += searchDistance) {
                collector.accept(provider.getBiome(null, x, y, z).key(), new BlockPos(x, y, z));
            }
        }
    }

    public boolean isUnavailable() {
        return this.biomeLocationMap == null;
    }

    public @NotNull @Unmodifiable List<BlockPos> getLocations(@NotNull Key key) {
        return this.biomeLocationMap != null ? Collections.unmodifiableList(this.biomeLocationMap.getOrDefault(key, Collections.emptyList())) : Collections.emptyList();
    }

    @SuppressWarnings("UnstableApiUsage")
    private record Cache(@MapType(key = Key.class, value = BlockPos[].class) Map<Key, BlockPos[]> map) {
        @SuppressWarnings("PatternValidation")
        private static final RecordSerialization<Cache> SERIALIZATION =
                RecordSerialization.builder(Cache.class)
                        .keyGenerator(KeyGenerator.CAMEL_TO_KEBAB)
                        .addSerialization(BlockPos.class, Serialization.<BlockPos, Node<?>>create(
                                pos -> new IntArray(new int[]{pos.x(), pos.z()}),
                                node -> {
                                    if (node instanceof ListNode listNode) {
                                        var list = listNode.asList(NumberValue.class);
                                        if (list.size() == 2) {
                                            return new BlockPos(list.get(0).asInt(), 64, list.get(1).asInt());
                                        }
                                    } else if (node instanceof IntArray intArray) {
                                        var array = intArray.value();
                                        if (array.length == 2) {
                                            return new BlockPos(array[0], 64, array[1]);
                                        }
                                    }
                                    return null;
                                }
                        ))
                        .addSerialization(Key.class, Serialization.<Key, Node<?>>create(
                                key -> StringValue.fromString(key.asString()),
                                node -> node instanceof StringRepresentable str ? Key.key(str.asString()) : null
                        )).build();

        public static Cache fromMapNode(@NotNull Node<?> node) {
            return node instanceof MapNode mapNode ? SERIALIZATION.deserializer().deserialize(mapNode) : new Cache(Collections.emptyMap());
        }

        public static Cache createCache(@NotNull Map<Key, List<BlockPos>> original) {
            var map = new HashMap<Key, BlockPos[]>(original.size(), 1.0f);

            for (var entry : original.entrySet()) {
                map.put(entry.getKey(), entry.getValue().toArray(BlockPos[]::new));
            }

            return new Cache(map);
        }

        public @NotNull MapNode toMapNode() {
            return SERIALIZATION.serializer().serialize(this);
        }

        public @NotNull Map<Key, List<BlockPos>> convertArrayToList() {
            var map = new HashMap<Key, List<BlockPos>>(this.map.size(), 1.0f);

            for (var entry : this.map.entrySet()) {
                var array = entry.getValue();
                var list = new ArrayList<BlockPos>(array.length);

                for (var pos : array) {
                    BiomeLocationsPlugin.debug().info("The location of the biome '{}': {}, {}", entry.getKey().asString(), pos.x(), pos.z());
                    list.add(pos);
                }

                map.put(entry.getKey(), list);
            }

            return map;
        }
    }
}
