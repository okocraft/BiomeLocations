package net.okocraft.biomelocations.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

class BiomeLocationCollector implements BiConsumer<Key, BiomePos> {

    private final Map<Key, List<BiomePos>> result = new HashMap<>();

    private final Predicate<Key> biomeFilter;
    private final long minimumBiomeDistanceSquared;

    BiomeLocationCollector(@NotNull Predicate<Key> biomeFilter, int minimumBiomeDistance) {
        this.biomeFilter = biomeFilter;
        this.minimumBiomeDistanceSquared = (long) minimumBiomeDistance * minimumBiomeDistance;
    }

    @Override
    public void accept(@NotNull Key biome, @NotNull BiomePos pos) {
        if (this.biomeFilter.test(biome)) {
            return;
        }

        List<BiomePos> posList = this.result.computeIfAbsent(biome, _ -> new ArrayList<>());

        for (BiomePos otherPos : posList) {
            if (distanceSquared2d(pos, otherPos) < this.minimumBiomeDistanceSquared) {
                return;
            }
        }

        posList.add(pos);
    }

    @NotNull Map<Key, List<BiomePos>> getResult() {
        return this.result;
    }

    private static long distanceSquared2d(@NotNull BiomePos pos1, @NotNull BiomePos pos2) {
        return square(pos1.x() - pos2.x()) + square(pos1.z() - pos2.z());
    }

    private static long square(long num) {
        return num * num;
    }
}
