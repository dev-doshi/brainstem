package dev.brainstem.mixin.core;

import net.minecraft.world.level.levelgen.MarsagliaPolarGaussian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MarsagliaPolarGaussian.class)
public interface MarsagliaPolarGaussianAccessor {
    @Accessor("nextNextGaussian")
    double bs$nextNextGaussian();

    @Accessor("nextNextGaussian")
    void bs$setNextNextGaussian(double v);

    @Accessor("haveNextNextGaussian")
    boolean bs$haveNextNextGaussian();

    @Accessor("haveNextNextGaussian")
    void bs$setHaveNextNextGaussian(boolean v);
}
