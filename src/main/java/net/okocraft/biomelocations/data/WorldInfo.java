package net.okocraft.biomelocations.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record WorldInfo(@NotNull String name, @NotNull UUID uid,
                        int centerX, int centerZ, double radius) {

    @Contract("_ -> new")
    public static @NotNull WorldInfo create(@NotNull World world) {
        Location center = world.getWorldBorder().getCenter();
        return new WorldInfo(
            world.getName(),
            world.getUID(),
            center.getBlockX(),
            center.getBlockZ(),
            world.getWorldBorder().getSize() / 2
        );
    }
}
