package net.okocraft.biomelocations.data;

import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.tuple.TupleCodec;
import dev.siroshun.codec4j.api.codec.tuple.TupleValueCodec;

public record BiomePos(int x, int z) {

    public static final Codec<BiomePos> CODEC = TupleCodec.create(
        BiomePos::new,
        TupleValueCodec.create(Codec.INT, BiomePos::x),
        TupleValueCodec.create(Codec.INT, BiomePos::z)
    );

}
