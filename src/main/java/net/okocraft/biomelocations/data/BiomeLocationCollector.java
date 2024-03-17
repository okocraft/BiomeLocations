package net.okocraft.biomelocations.data;

import com.github.siroshun09.biomefinder.wrapper.BlockPos;
import net.kyori.adventure.key.Key;
import net.okocraft.biomelocations.BiomeLocationsPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

class BiomeLocationCollector implements BiConsumer<Key, BlockPos> {

    private final Map<Key, List<BlockPos>> result = new HashMap<>();

    private final Predicate<Key> biomeFilter;
    private final long minimumBiomeDistanceSquared;

    BiomeLocationCollector(@NotNull Predicate<Key> biomeFilter, int minimumBiomeDistance) {
        this.biomeFilter = biomeFilter;
        this.minimumBiomeDistanceSquared = (long) minimumBiomeDistance * minimumBiomeDistance;
    }

    @Override
    public void accept(@NotNull Key biome, @NotNull BlockPos pos) {
        if (this.biomeFilter.test(biome)) {
            return;
        }

        var posList = this.result.computeIfAbsent(biome, ignored -> new ArrayList<>());

        for (var otherPos : posList) {
            if (distanceSquared2d(pos, otherPos) < this.minimumBiomeDistanceSquared) {
                return;
            }
        }

        BiomeLocationsPlugin.debug().info("The biome '{}' was found at {}, {}", biome.asString(), pos.x(), pos.z());
        posList.add(pos);
    }

    @NotNull Map<Key, List<BlockPos>> getResult() {
        return this.result;
    }

    private static long distanceSquared2d(@NotNull BlockPos pos1, @NotNull BlockPos pos2) {
        return square(pos1.x() - pos2.x()) + square(pos1.z() - pos2.z());
    }

    private static long square(long num) {
        return num * num;
    }
}
