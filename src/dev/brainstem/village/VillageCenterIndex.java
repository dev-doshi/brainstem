package dev.brainstem.village;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

/**
 * Mirror of the village distance tracker's <em>sources</em>: the sections for which PoiManager.isVillageCenter last
 * returned true. Both the vanilla SectionTracker and Moonrise's propagator derive a section's level-0 state only from
 * that method, and a section's level is &lt;= 1 (ServerLevel.isVillage) only if a source lies within Chebyshev
 * distance 1 of it. So "no source in columns x±1, z±1 at section Y &gt;= y-1" proves isVillage is false for every
 * position at or above that section.
 */
public final class VillageCenterIndex {
    /** SectionPos.asLong(x, bank, z) -> bit (y &amp; 63) set for each source section with y &gt;&gt; 6 == bank. */
    private final Long2LongOpenHashMap banks = new Long2LongOpenHashMap();
    /** ChunkPos.pack(x, z) -> highest source section Y in that column; absent when the column has none. */
    private final Long2IntOpenHashMap columnMax = new Long2IntOpenHashMap();
    private int minBank = Integer.MAX_VALUE;
    private int maxBank = Integer.MIN_VALUE;

    public VillageCenterIndex() {
        this.columnMax.defaultReturnValue(Integer.MIN_VALUE);
    }

    public synchronized void set(long sectionNode, boolean source) {
        int sx = SectionPos.x(sectionNode);
        int sy = SectionPos.y(sectionNode);
        int sz = SectionPos.z(sectionNode);
        int bank = sy >> 6;
        long bit = 1L << (sy & 63);
        long bankKey = SectionPos.asLong(sx, bank, sz);
        long old = this.banks.get(bankKey);
        long now = source ? old | bit : old & ~bit;
        if (old == now) {
            return;
        }
        if (now == 0L) {
            this.banks.remove(bankKey);
        } else {
            this.banks.put(bankKey, now);
        }
        long col = ChunkPos.pack(sx, sz);
        int max = this.columnMax.get(col);
        if (source) {
            this.minBank = Math.min(this.minBank, bank);
            this.maxBank = Math.max(this.maxBank, bank);
            if (sy > max) {
                this.columnMax.put(col, sy);
            }
        } else if (sy == max) {
            for (int b = this.maxBank; b >= this.minBank; b--) {
                long bits = this.banks.get(SectionPos.asLong(sx, b, sz));
                if (bits != 0L) {
                    this.columnMax.put(col, (b << 6) + 63 - Long.numberOfLeadingZeros(bits));
                    return;
                }
            }
            this.columnMax.remove(col);
        }
    }

    /** True if any source section lies in columns [x0,x1] x [z0,z1] at section Y &gt;= minY. */
    public synchronized boolean anyAtOrAbove(int x0, int x1, int z0, int z1, int minY) {
        if (this.columnMax.isEmpty()) {
            return false;
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (this.columnMax.get(ChunkPos.pack(x, z)) >= minY) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized int size() {
        return this.columnMax.size();
    }
}
