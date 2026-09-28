package dev.brainstem.tempt;

import dev.brainstem.Module;
import dev.brainstem.mixin.tempt.TargetingConditionsAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

/**
 * ServerLevel.getNearestPlayer(conditions, mob) for TemptGoal. TargetingConditions.test runs the (item-tag) selector
 * before the range check; every step is side-effect free, so a player that provably fails the range check can be
 * dropped without running the selector. Survivors go through the unmodified test, and the nearest is chosen with
 * vanilla's comparison, so the result is identical.
 * <p>
 * The range check fails when distSqr &gt; max(range * visibility, 2)^2. LivingEntity.getVisibilityPercent is clamped
 * to [0, 10], so beyond max(range * 10, 2) no player can pass; in between, the exact visibility is computed.
 */
public final class TemptScan {
    public static final int SCANS = 0, PLAYERS = 1, PREFILTERED = 2, VISIBILITY = 3, VERIFIED = 4, MISMATCHES = 5;
    private static final double MAX_VISIBILITY = 10.0;
    private static final double MIN_DISTANCE = 2.0;

    private TemptScan() {
    }

    public static Player nearest(ServerLevel level, TargetingConditions conditions, LivingEntity mob) {
        Module m = Module.TEMPT;
        m.inc(SCANS);
        TargetingConditionsAccessor acc = (TargetingConditionsAccessor) conditions;
        double range = acc.bs$range();
        boolean invisible = acc.bs$testInvisible();
        boolean prune = range > 0.0;
        double outer = Math.max(range * (invisible ? MAX_VISIBILITY : 1.0), MIN_DISTANCE);
        double outerSqr = outer * outer;
        double innerSqr = range * range;
        double x = mob.getX(), y = mob.getY(), z = mob.getZ();
        double best = -1.0;
        Player result = null;
        for (ServerPlayer p : level.players()) {
            m.inc(PLAYERS);
            if (prune) {
                // Same expression as TargetingConditions.test.
                double d = mob.distanceToSqr(p.getX(), p.getY(), p.getZ());
                if (d > outerSqr) {
                    m.inc(PREFILTERED);
                    continue;
                }
                if (invisible && d > innerSqr) {
                    m.inc(VISIBILITY);
                    double visible = Math.max(range * p.getVisibilityPercent(level, mob), MIN_DISTANCE);
                    if (d > visible * visible) {
                        m.inc(PREFILTERED);
                        continue;
                    }
                }
            }
            if (conditions.test(level, mob, p)) {
                double dist = p.distanceToSqr(x, y, z);
                if (best == -1.0 || dist < best) {
                    best = dist;
                    result = p;
                }
            }
        }
        return result;
    }
}
