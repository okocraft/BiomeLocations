package net.okocraft.biomelocations.data;

import com.github.siroshun09.biomefinder.util.MapWalker;
import com.github.siroshun09.biomefinder.wrapper.BlockPos;
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
import net.kyori.adventure.key.Key;
import net.okocraft.biomelocations.BiomeLocationsPlugin;
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
                                         @NotNull Predicate<Key> biomeFilter, int minimumBiomeDistance) throws IOException {
        if (!this.worldInfo.canCreateBiomeSource()) {
            this.biomeLocationMap = Collections.emptyMap();
            return;
        }

        this.biomeLocationMap = null;
        var cacheFilepath = cacheDirectory.resolve(this.worldInfo.uid() + ".dat");

        if (Files.isRegularFile(cacheFilepath)) {
            BiomeLocationsPlugin.debug().info("Using cached biome data for {}", this.worldInfo.name());
            this.biomeLocationMap = Cache.fromMapNode(BinaryFormat.DEFAULT.load(cacheFilepath)).convertArrayToList();
        } else {
            BiomeLocationsPlugin.logger().info("Generating biome data of {}...", this.worldInfo.name());
            var collector = new BiomeLocationCollector(biomeFilter, minimumBiomeDistance);
            var walker = new MapWalker(this.worldInfo.createBiomeSource(), collector);

            walker.walk(this.worldInfo.center(), this.worldInfo.radius(), searchDistance);

            this.biomeLocationMap = collector.getResult();

            BiomeLocationsPlugin.debug().info("Saving biome data of {}...", this.worldInfo.name());
            BinaryFormat.DEFAULT.save(Cache.createCache(collector.getResult()).toMapNode(), cacheFilepath);
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
