package dev.brainstem.mixin.pathprobe;

import dev.brainstem.pathmemo.BsChunkEpoch;
import dev.brainstem.pathmemo.ChunkEpochs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements BsChunkEpoch {
    @Unique
    private volatile long bs$epoch = ChunkEpochs.next();

    @Override
    public long bs$epoch() {
        return this.bs$epoch;
    }

    /** setBlockState returns the previous state iff it changed something. */
    @Inject(method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("RETURN"))
    private void bs$bump(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null) {
            this.bs$epoch = ChunkEpochs.next();
        }
    }
}
