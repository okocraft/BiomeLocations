package net.okocraft.biomelocations.data;

import com.github.siroshun09.biomefinder.wrapper.BlockPos;
import com.github.siroshun09.biomefinder.wrapper.biome.BiomeSource;
import com.github.siroshun09.biomefinder.wrapper.biome.MultiNoiseBiomeSourceWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record WorldInfo(@NotNull String name, @NotNull UUID uid, long seed, @NotNull World.Environment environment,
                        @NotNull BlockPos center, int radius) {

    @Contract("_ -> new")
    public static @NotNull WorldInfo create(@NotNull World world) {
        return new WorldInfo(
                world.getName(),
                world.getUID(),
                world.getSeed(),
                world.getEnvironment(),
                toCenter(world.getWorldBorder().getCenter()),
                toRadius(world.getWorldBorder())
        );
    }

    private static BlockPos toCenter(@NotNull Location location) {
        return new BlockPos(location.getBlockX(), 64, location.getBlockZ());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canCreateBiomeSource() {
        return this.environment == World.Environment.NORMAL || this.environment == World.Environment.NETHER;
    }

    public @NotNull BiomeSource createBiomeSource() {
        return switch (this.environment) {
            case NORMAL -> MultiNoiseBiomeSourceWrapper.overworld(this.seed);
            case NETHER -> MultiNoiseBiomeSourceWrapper.nether(this.seed);
            default -> throw new IllegalArgumentException("Cannot create a BiomeSource for " + this.environment.name());
        };
    }

    private static int toRadius(@NotNull WorldBorder border) {
        if (Double.compare(border.getSize(), border.getMaxSize()) == 0) {
            return 5000;
        } else {
            return (int) (border.getSize() / 2);
        }
    }
}
