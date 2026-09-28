package dev.brainstem.gate;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public final class GateShapes {
    public static final MemoryModuleType<?>[] NO_TYPES = new MemoryModuleType<?>[0];
    public static final MemoryStatus[] NO_STATUSES = new MemoryStatus[0];

    private GateShapes() {
    }

    /**
     * Shape of a combinator node whose children are evaluated in the given order, each returning null (so the
     * whole node returns null) on failure, before the node applies its function. The prefix runs up to and including
     * the first impure child's own prefix; the node itself is treated as impure, since the applied function is
     * arbitrary code.
     */
    public static void combine(Object node, Object... childrenInEvaluationOrder) {
        List<MemoryModuleType<?>> types = new ArrayList<>();
        List<MemoryStatus> statuses = new ArrayList<>();
        for (Object child : childrenInEvaluationOrder) {
            if (!(child instanceof BsNode n) || !n.bs$known()) {
                break;
            }
            MemoryModuleType<?>[] t = n.bs$types();
            MemoryStatus[] s = n.bs$statuses();
            for (int i = 0; i < t.length; i++) {
                types.add(t[i]);
                statuses.add(s[i]);
            }
            if (!n.bs$pure()) {
                break;
            }
        }
        ((BsNode) node).bs$setShape(false, types.toArray(NO_TYPES), statuses.toArray(NO_STATUSES));
    }
}
