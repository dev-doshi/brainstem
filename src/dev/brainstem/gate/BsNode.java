package dev.brainstem.gate;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Duck interface on every BehaviorBuilder node. The "prefix" is the ordered list of memory conditions that vanilla
 * evaluates, side-effect free, before the node's first impure step. Unknown nodes (not built by the combinators we
 * instrument) have no prefix and count as impure.
 */
public interface BsNode {
    boolean bs$known();

    boolean bs$pure();

    MemoryModuleType<?>[] bs$types();

    MemoryStatus[] bs$statuses();

    void bs$setShape(boolean pure, MemoryModuleType<?>[] types, MemoryStatus[] statuses);
}
