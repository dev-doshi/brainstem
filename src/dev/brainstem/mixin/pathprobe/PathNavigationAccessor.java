package dev.brainstem.mixin.pathprobe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PathNavigation.class)
public interface PathNavigationAccessor {
    @Accessor("targetPos")
    BlockPos bs$targetPos();

    @Accessor("targetPos")
    void bs$setTargetPos(BlockPos pos);

    @Accessor("reachRange")
    void bs$setReachRange(int reachRange);

    @Accessor("maxVisitedNodesMultiplier")
    float bs$visitedMultiplier();

    @Invoker("resetStuckTimeout")
    void bs$resetStuckTimeout();

    @Invoker("canUpdatePath")
    boolean bs$canUpdatePath();

    @Invoker("getMaxPathLength")
    float bs$maxPathLength();
}
