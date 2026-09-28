package dev.brainstem.pathmemo;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Chunk change stamps. Every LevelChunk object gets a fresh stamp when constructed (load / promotion to full) and a new
 * one whenever a block state in it changes, all drawn from one strictly increasing clock. A region is unchanged iff
 * every chunk in it has the same stamp (0 = not loaded, which the path region also treats as empty).
 */
public final class ChunkEpochs {
    private static final AtomicLong CLOCK = new AtomicLong();

    private ChunkEpochs() {
    }

    public static long next() {
        return CLOCK.incrementAndGet();
    }

    /** Stamps of chunks [cx0..cx1] x [cz0..cz1], row-major, exactly as PathNavigationRegion would capture them. */
    public static long[] snapshot(ServerLevel level, int cx0, int cz0, int cx1, int cz1) {
        long[] out = new long[(cx1 - cx0 + 1) * (cz1 - cz0 + 1)];
        var source = level.getChunkSource();
        int i = 0;
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                LevelChunk c = source.getChunkNow(cx, cz);
                out[i++] = c == null ? 0L : ((BsChunkEpoch) c).bs$epoch();
            }
        }
        return out;
    }
}
