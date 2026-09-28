package dev.brainstem.pathmemo;

import dev.brainstem.Module;
import dev.brainstem.Verify;
import dev.brainstem.mixin.pathprobe.PathNavigationAccessor;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * The AcquirePoi.findPathToPois funnel (AcquirePoi, NearestBedSensor, SetClosestHomeAsWalkTarget), which all only ask
 * "does the path reach". Probe mode measures how often a mob repeats a request whose inputs (ReachKey) and search
 * region (chunk stamps) are unchanged; memo mode answers such repeats of non-reaching results from the previous run,
 * replaying createPath's navigation side effects.
 */
public final class ReachMemo {
    private static final int P_CALLS = 0, P_EARLY = 1, P_NULL = 2, P_PARTIAL = 3, P_REACHING = 4, P_REPEAT = 5, P_SAME = 6,
        P_NS = 7, P_REPEAT_NS = 8, P_REPEAT_NONREACH_NS = 9;
    private static final int M_CALLS = 0, M_HITS = 1, M_STORES = 2, M_INVALIDATED = 3, M_SAVED = 4, M_VERIFIED = 5, M_MISMATCHES = 6;
    private static final int NULL = 0, PARTIAL = 1, REACHING = 2;

    private static final Map<Mob, Entry> LAST = new WeakHashMap<>();

    private record Entry(ReachKey key, long[] epochs, int kind, Path path, long ns) {
    }

    private ReachMemo() {
    }

    public interface Call {
        Path run();
    }

    public static Path createPath(Mob mob, PathNavigation nav, Set<BlockPos> targets, int maxRange, Call vanilla) {
        Module probe = Module.REACH_PROBE, memo = Module.REACH_MEMO;
        boolean probeOn = probe.on, memoOn = memo.on;
        if (!probeOn && !memoOn || !(mob.level() instanceof ServerLevel level)) {
            return vanilla.run();
        }
        PathNavigationAccessor acc = (PathNavigationAccessor) nav;
        // createPath's early returns: nothing is searched, so there is nothing to save.
        if (targets.isEmpty() || mob.getY() < level.getMinY() || !acc.bs$canUpdatePath()
            || nav.getPath() != null && !nav.getPath().isDone() && targets.contains(acc.bs$targetPos())) {
            if (probeOn) {
                probe.inc(P_CALLS);
                probe.inc(P_EARLY);
            }
            return vanilla.run();
        }
        ReachKey key = new ReachKey(mob, nav, acc.bs$maxPathLength(), acc.bs$visitedMultiplier(), targets, maxRange);
        long[] epochs = ChunkEpochs.snapshot(level, key.cx0, key.cz0, key.cx1, key.cz1);
        Entry prev = LAST.get(mob);
        boolean repeat = prev != null && prev.key.equals(key) && Arrays.equals(prev.epochs, epochs);
        if (memoOn && eligible(mob, nav)) {
            memo.inc(M_CALLS);
            if (repeat && prev.kind != REACHING) {
                memo.inc(M_HITS);
                memo.add(M_SAVED, prev.ns);
                if (Verify.sample()) {
                    Path real = vanilla.run();
                    memo.inc(M_VERIFIED);
                    if (!sameOutcome(prev, real)) {
                        Entry p = prev;
                        Verify.mismatch(memo, M_MISMATCHES, "ReachMemo.createPath",
                            () -> key + " memo=" + describe(p.path) + " vanilla=" + describe(real));
                        LAST.remove(mob);
                    }
                    return real;
                }
                if (prev.path != null && prev.path.getTarget() != null) {
                    acc.bs$setTargetPos(prev.path.getTarget());
                    acc.bs$setReachRange(maxRange);
                    acc.bs$resetStuckTimeout();
                }
                return prev.path == null ? null : prev.path.copy();
            }
            if (prev != null && prev.key.equals(key)) {
                memo.inc(M_INVALIDATED);
            }
        }
        long t0 = System.nanoTime();
        Path path = vanilla.run();
        long ns = System.nanoTime() - t0;
        int kind = path == null ? NULL : path.canReach() ? REACHING : PARTIAL;
        if (probeOn) {
            probe.inc(P_CALLS);
            probe.inc(kind == NULL ? P_NULL : kind == PARTIAL ? P_PARTIAL : P_REACHING);
            probe.add(P_NS, ns);
            if (repeat) {
                probe.inc(P_REPEAT);
                probe.add(P_REPEAT_NS, ns);
                if (kind != REACHING) {
                    probe.add(P_REPEAT_NONREACH_NS, ns);
                }
                if (prev.kind == kind) {
                    probe.inc(P_SAME);
                }
            }
        }
        if (memoOn && kind != REACHING) {
            memo.inc(M_STORES);
        }
        LAST.put(mob, new Entry(key, epochs, kind, kind == REACHING || path == null ? null : path.copy(), ns));
        return path;
    }

    /** Villagers on the plain walk evaluator: no RNG (unlike FlyNodeEvaluator) and no pathfinding start/done hooks. */
    private static boolean eligible(Mob mob, PathNavigation nav) {
        return mob instanceof Villager && nav.getNodeEvaluator().getClass() == WalkNodeEvaluator.class;
    }

    private static boolean sameOutcome(Entry e, Path real) {
        if (e.path == null) {
            return real == null;
        }
        return real != null && !real.canReach() && Objects.equals(real.getTarget(), e.path.getTarget())
            && real.getNodeCount() == e.path.getNodeCount() && Objects.equals(real.getEndNode(), e.path.getEndNode());
    }

    private static String describe(Path p) {
        return p == null ? "null" : "Path[target=" + p.getTarget() + " nodes=" + p.getNodeCount() + " reach=" + p.canReach() + "]";
    }

    public static int size() {
        return LAST.size();
    }
}
