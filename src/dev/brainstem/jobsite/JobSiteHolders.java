package dev.brainstem.jobsite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * GlobalPos -> brains that may hold it as JOB_SITE. Invariant: a superset of the brains whose JOB_SITE slot currently
 * equals the key, because every write of a slot goes through Brain.setMemoryInternal, which feeds {@link #add}.
 * Entries are verified and pruned lazily on lookup; brains of removed entities disappear with GC.
 */
public final class JobSiteHolders {
    private static final Map<GlobalPos, Set<Brain<?>>> HOLDERS = new HashMap<>();

    private JobSiteHolders() {
    }

    public static synchronized void add(GlobalPos pos, Brain<?> brain) {
        HOLDERS.computeIfAbsent(pos, k -> Collections.newSetFromMap(new WeakHashMap<>(4))).add(brain);
    }

    /** Whether any brain other than {@code self} currently has JOB_SITE == pos. */
    public static synchronized boolean anyOther(GlobalPos pos, Brain<?> self) {
        Set<Brain<?>> set = HOLDERS.get(pos);
        if (set == null) {
            return false;
        }
        boolean found = false;
        for (Iterator<Brain<?>> it = set.iterator(); it.hasNext(); ) {
            Brain<?> b = it.next();
            if (b == self) {
                continue;
            }
            Optional<GlobalPos> current = b.getMemoryInternal(MemoryModuleType.JOB_SITE);
            if (current != null && current.isPresent() && current.get().equals(pos)) {
                found = true;
            } else {
                it.remove();
            }
        }
        if (set.isEmpty()) {
            HOLDERS.remove(pos);
        }
        return found;
    }

    public static synchronized int size() {
        return HOLDERS.size();
    }
}
