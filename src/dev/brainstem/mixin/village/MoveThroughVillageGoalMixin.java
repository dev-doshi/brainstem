package dev.brainstem.mixin.village;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.brainstem.Module;
import dev.brainstem.Rng;
import dev.brainstem.Verify;
import dev.brainstem.village.BsPoiManager;
import dev.brainstem.village.VillageCenterIndex;
import dev.brainstem.village.VillageRandomPos;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MoveThroughVillageGoal.class)
public abstract class MoveThroughVillageGoalMixin {
    @WrapOperation(
        method = "canUse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/util/LandRandomPos;getPos(Lnet/minecraft/world/entity/PathfinderMob;IILjava/util/function/ToDoubleFunction;)Lnet/minecraft/world/phys/Vec3;"),
        require = 1
    )
    private Vec3 bs$villageRandomPos(PathfinderMob mob, int h, int v, ToDoubleFunction<BlockPos> scorer, Operation<Vec3> original) {
        if (!Module.MTVG.on || !(mob.level() instanceof ServerLevel level)) {
            return original.call(mob, h, v, scorer);
        }
        VillageCenterIndex index = ((BsPoiManager) level.getPoiManager()).bs$villageIndex();
        if (!Verify.sample()) {
            return VillageRandomPos.getPos(mob, h, v, scorer, index);
        }
        Rng before = Rng.snapshot(mob.getRandom());
        if (before == null) {
            return original.call(mob, h, v, scorer);
        }
        Module.MTVG.inc(VillageRandomPos.VERIFIED);
        // Index vs tracker at the mob's own section: isVillage there implies an indexed source nearby.
        BlockPos bp = mob.blockPosition();
        int sx = SectionPos.blockToSectionCoord(bp.getX()), sy = SectionPos.blockToSectionCoord(bp.getY()), sz = SectionPos.blockToSectionCoord(bp.getZ());
        int levelHere = level.sectionsToVillage(SectionPos.of(bp));
        if (levelHere <= 1 && !index.anyAtOrAbove(sx - 1, sx + 1, sz - 1, sz + 1, sy - 1)) {
            Verify.mismatch(Module.MTVG, VillageRandomPos.MISMATCHES, "index",
                () -> "section " + SectionPos.of(bp) + " has tracker level " + levelHere + " but no indexed source nearby; index columns=" + index.size());
        }
        Vec3 optimised = VillageRandomPos.getPos(mob, h, v, scorer, index);
        Rng afterOptimised = Rng.snapshot(mob.getRandom());
        before.restore(mob.getRandom());
        Vec3 vanilla = original.call(mob, h, v, scorer);
        Rng afterVanilla = Rng.snapshot(mob.getRandom());
        if (!Objects.equals(optimised, vanilla) || !afterOptimised.equals(afterVanilla)) {
            Verify.mismatch(Module.MTVG, VillageRandomPos.MISMATCHES, "getPos",
                () -> mob + " at " + mob.position() + " home=" + mob.hasHome() + " optimised=" + optimised + " vanilla=" + vanilla
                    + " rngOpt=" + afterOptimised + " rngVanilla=" + afterVanilla);
        }
        return vanilla;
    }
}
