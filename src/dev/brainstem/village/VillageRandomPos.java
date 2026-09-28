package dev.brainstem.village;

import dev.brainstem.Module;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * LandRandomPos.getPos(mob, h, v, scorer) for MoveThroughVillageGoal, whose scorer is -inf unless
 * ServerLevel.isVillage(p). Calls the same vanilla helpers in the same order, so every RNG draw is identical;
 * candidates that provably cannot be in a village skip only the pure block checks and the scorer, which could
 * never select them (RandomPos.generateRandomPos keeps a candidate only when value &gt; best, best starting at -inf).
 */
public final class VillageRandomPos {
    public static final int CAN_USE = 0, WHOLE_SKIPS = 1, CANDIDATES = 2, PRUNED = 3, VERIFIED = 4, MISMATCHES = 5;
    private static final int ATTEMPTS = 10;

    private VillageRandomPos() {
    }

    public static Vec3 getPos(PathfinderMob mob, int horizontalDist, int verticalDist, ToDoubleFunction<BlockPos> scorer, VillageCenterIndex index) {
        Module m = Module.MTVG;
        m.inc(CAN_USE);
        RandomSource random = mob.getRandom();
        // Mirrors RandomPos.generateRandomPosTowardDirection: an extra offset strictly below xzDist/2 when homed.
        double reach = horizontalDist + (mob.hasHome() && horizontalDist > 1.0 ? horizontalDist / 2.0 : 0.0);
        int x0 = SectionPos.blockToSectionCoord(Mth.floor(mob.getX() - reach)) - 1;
        int x1 = SectionPos.blockToSectionCoord(Mth.floor(mob.getX() + reach)) + 1;
        int z0 = SectionPos.blockToSectionCoord(Mth.floor(mob.getZ() - reach)) - 1;
        int z1 = SectionPos.blockToSectionCoord(Mth.floor(mob.getZ() + reach)) + 1;
        int minY = SectionPos.blockToSectionCoord(Mth.floor(mob.getY() - verticalDist)) - 1;
        if (!index.anyAtOrAbove(x0, x1, z0, z1, minY)) {
            m.inc(WHOLE_SKIPS);
            // The draws of generateRandomDirection and generateRandomPosTowardDirection, without building positions.
            int xz = 2 * horizontalDist + 1, yb = 2 * verticalDist + 1;
            boolean homeOffset = mob.hasHome() && horizontalDist > 1.0;
            for (int i = 0; i < ATTEMPTS; i++) {
                random.nextInt(xz);
                random.nextInt(yb);
                random.nextInt(xz);
                if (homeOffset) {
                    random.nextDouble();
                    random.nextDouble();
                }
            }
            return null;
        }

        boolean restrict = GoalUtils.mobRestricted(mob, horizontalDist);
        double bestWeight = Double.NEGATIVE_INFINITY;
        BlockPos bestPos = null;
        for (int i = 0; i < ATTEMPTS; i++) {
            m.inc(CANDIDATES);
            BlockPos direction = RandomPos.generateRandomDirection(random, horizontalDist, verticalDist);
            BlockPos pos = RandomPos.generateRandomPosTowardDirection(mob, horizontalDist, random, direction);
            int sx = SectionPos.blockToSectionCoord(pos.getX());
            int sz = SectionPos.blockToSectionCoord(pos.getZ());
            // movePosUpOutOfSolid only raises Y, so no source at or above sy-1 means isVillage is false for the final pos.
            if (!index.anyAtOrAbove(sx - 1, sx + 1, sz - 1, sz + 1, SectionPos.blockToSectionCoord(pos.getY()) - 1)) {
                m.inc(PRUNED);
                continue;
            }
            if (GoalUtils.isOutsideLimits(pos, mob) || GoalUtils.isRestricted(restrict, mob, pos) || GoalUtils.isNotStable(mob.getNavigation(), pos)) {
                continue;
            }
            BlockPos moved = LandRandomPos.movePosUpOutOfSolid(mob, pos);
            if (moved == null) {
                continue;
            }
            double value = scorer.applyAsDouble(moved);
            if (value > bestWeight) {
                bestWeight = value;
                bestPos = moved;
            }
        }
        return bestPos != null ? Vec3.atBottomCenterOf(bestPos) : null;
    }
}
