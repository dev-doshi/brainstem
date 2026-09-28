package dev.brainstem.pathmemo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

/**
 * Every input of PathNavigation.createPath(Set, int) other than block states: the mob state the node evaluator and
 * path finder read (see WalkNodeEvaluator / PathFinder), the navigation settings and the request. Block states are
 * covered separately by the chunk stamps of the search region.
 */
public final class ReachKey {
    private static final PathType[] TYPES = PathType.values();

    final Class<?> mobClass;
    final Class<?> evaluatorClass;
    final double x, y, z;
    final AABB box;
    final float bbWidth, bbHeight, maxUpStep, maxPathLength, visitedMultiplier;
    final int maxFall, maxRange, flags;
    final float[] malus;
    final Set<BlockPos> targets;
    final double bMinX, bMaxX, bMinZ, bMaxZ;
    final int hash;
    /** Search region in chunk coordinates. */
    public final int cx0, cz0, cx1, cz1;

    public ReachKey(Mob mob, PathNavigation nav, float maxPathLength, float visitedMultiplier, Set<BlockPos> targets, int maxRange) {
        NodeEvaluator ev = nav.getNodeEvaluator();
        this.mobClass = mob.getClass();
        this.evaluatorClass = ev.getClass();
        this.x = mob.getX();
        this.y = mob.getY();
        this.z = mob.getZ();
        this.box = mob.getBoundingBox();
        this.bbWidth = mob.getBbWidth();
        this.bbHeight = mob.getBbHeight();
        this.maxUpStep = mob.maxUpStep();
        this.maxPathLength = maxPathLength;
        this.visitedMultiplier = visitedMultiplier;
        this.maxFall = mob.getMaxFallDistance();
        this.maxRange = maxRange;
        this.flags = (mob.onGround() ? 1 : 0) | (mob.isInWater() ? 2 : 0) | (mob.isInFloatableFluid() ? 4 : 0)
            | (ev.canPassDoors() ? 8 : 0) | (ev.canOpenDoors() ? 16 : 0) | (ev.canFloat() ? 32 : 0) | (ev.canWalkOverFences() ? 64 : 0)
            | (mob.isPassenger() ? 128 : 0);
        this.malus = new float[TYPES.length];
        for (int i = 0; i < TYPES.length; i++) {
            this.malus[i] = mob.getPathfindingMalus(TYPES[i]);
        }
        this.targets = new HashSet<>(targets);
        WorldBorder b = mob.level().getWorldBorder();
        this.bMinX = b.getMinX();
        this.bMaxX = b.getMaxX();
        this.bMinZ = b.getMinZ();
        this.bMaxZ = b.getMaxZ();
        BlockPos from = mob.blockPosition();
        int radius = (int) (maxPathLength + 8);
        this.cx0 = (from.getX() - radius) >> 4;
        this.cz0 = (from.getZ() - radius) >> 4;
        this.cx1 = (from.getX() + radius) >> 4;
        this.cz1 = (from.getZ() + radius) >> 4;
        int h = Double.hashCode(this.x);
        h = 31 * h + Double.hashCode(this.y);
        h = 31 * h + Double.hashCode(this.z);
        h = 31 * h + this.flags;
        h = 31 * h + maxRange;
        h = 31 * h + this.targets.hashCode();
        this.hash = h;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ReachKey k && k.hash == this.hash
            && k.mobClass == this.mobClass && k.evaluatorClass == this.evaluatorClass
            && Double.compare(k.x, this.x) == 0 && Double.compare(k.y, this.y) == 0 && Double.compare(k.z, this.z) == 0
            && k.box.equals(this.box) && k.flags == this.flags && k.maxRange == this.maxRange && k.maxFall == this.maxFall
            && Float.compare(k.bbWidth, this.bbWidth) == 0 && Float.compare(k.bbHeight, this.bbHeight) == 0
            && Float.compare(k.maxUpStep, this.maxUpStep) == 0 && Float.compare(k.maxPathLength, this.maxPathLength) == 0
            && Float.compare(k.visitedMultiplier, this.visitedMultiplier) == 0
            && k.bMinX == this.bMinX && k.bMaxX == this.bMaxX && k.bMinZ == this.bMinZ && k.bMaxZ == this.bMaxZ
            && Arrays.equals(k.malus, this.malus) && k.targets.equals(this.targets);
    }

    @Override
    public String toString() {
        return Objects.toString(this.mobClass.getSimpleName()) + "@" + this.x + "," + this.y + "," + this.z + " targets=" + this.targets
            + " range=" + this.maxRange + " flags=" + this.flags;
    }
}
