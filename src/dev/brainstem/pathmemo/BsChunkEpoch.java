package dev.brainstem.pathmemo;

/** Duck interface on LevelChunk: a stamp from {@link ChunkEpochs#next()} that changes on every block change. */
public interface BsChunkEpoch {
    long bs$epoch();
}
