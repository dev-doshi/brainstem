package dev.brainstem.mixin.core;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.MarsagliaPolarGaussian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LegacyRandomSource.class)
public interface LegacyRandomSourceAccessor {
    @Accessor("seed")
    AtomicLong bs$seed();

    @Accessor("gaussianSource")
    MarsagliaPolarGaussian bs$gaussian();
}
