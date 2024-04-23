package net.okocraft.biomelocations.data;

import net.okocraft.biomelocations.util.BlockPos;
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

    private static int toRadius(@NotNull WorldBorder border) {
        if (Double.compare(border.getSize(), border.getMaxSize()) == 0) {
            return 5000;
        } else {
            return (int) (border.getSize() / 2);
        }
    }
}
