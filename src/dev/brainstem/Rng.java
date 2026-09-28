package dev.brainstem;

import dev.brainstem.mixin.core.LegacyRandomSourceAccessor;
import dev.brainstem.mixin.core.MarsagliaPolarGaussianAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

/** Snapshot/restore of an entity RandomSource so vanilla and optimised code can run on the same draws. */
public record Rng(long seed, double nextNextGaussian, boolean haveNextNextGaussian) {
    /** null when the source is not a LegacyRandomSource (then verification is skipped). */
    public static Rng snapshot(RandomSource r) {
        if (r.getClass() != LegacyRandomSource.class) {
            return null;
        }
        LegacyRandomSourceAccessor a = (LegacyRandomSourceAccessor) r;
        MarsagliaPolarGaussianAccessor g = (MarsagliaPolarGaussianAccessor) a.bs$gaussian();
        return new Rng(a.bs$seed().get(), g.bs$nextNextGaussian(), g.bs$haveNextNextGaussian());
    }

    public void restore(RandomSource r) {
        LegacyRandomSourceAccessor a = (LegacyRandomSourceAccessor) r;
        MarsagliaPolarGaussianAccessor g = (MarsagliaPolarGaussianAccessor) a.bs$gaussian();
        a.bs$seed().set(this.seed);
        g.bs$setNextNextGaussian(this.nextNextGaussian);
        g.bs$setHaveNextNextGaussian(this.haveNextNextGaussian);
    }
}
