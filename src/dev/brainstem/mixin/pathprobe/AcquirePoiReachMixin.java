package dev.brainstem.mixin.pathprobe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import dev.brainstem.pathmemo.ReachMemo;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The one createPath call behind AcquirePoi, NearestBedSensor and SetClosestHomeAsWalkTarget. */
@Mixin(AcquirePoi.class)
public abstract class AcquirePoiReachMixin {
    @WrapOperation(
        method = "findPathToPois(Lnet/minecraft/world/entity/Mob;Ljava/util/Set;)Lnet/minecraft/world/level/pathfinder/Path;",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Ljava/util/Set;I)Lnet/minecraft/world/level/pathfinder/Path;"),
        require = 1)
    private static Path bs$reach(PathNavigation nav, Set<BlockPos> targets, int maxRange, Operation<Path> original,
        Mob body, Set<Pair<Holder<PoiType>, BlockPos>> pois) {
        return ReachMemo.createPath(body, nav, targets, maxRange, () -> original.call(nav, targets, maxRange));
    }
}
