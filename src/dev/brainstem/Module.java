package dev.brainstem;

/** One optimisation module: its config key, runtime switch and server-thread counters. */
public enum Module {
    MTVG("mtvg", "canUse", "wholeSkips", "candidates", "pruned", "verified", "mismatches"),
    GATE("oneShotGate", "builtGated", "builtTotal", "triggers", "gateFails", "passes", "verified", "mismatches"),
    JOBSITE("competitorScan", "scans", "fastPath", "slowPath", "verified", "mismatches"),
    TEMPT("temptScan", "scans", "players", "prefiltered", "visibilityChecks", "verified", "mismatches"),
    REACH_PROBE("reachProbe", "calls", "early", "nullResults", "partial", "reaching", "repeatable", "repeatSame", "totalNs", "repeatNs",
        "repeatNonReachNs"),
    REACH_MEMO("reachMemo", "calls", "memoHits", "stores", "invalidated", "savedNs", "verified", "mismatches");

    public final String key;
    public final String[] names;
    public final long[] c;
    /** Mixins for this module were applied at boot (config enabled at launch). */
    public boolean loaded;
    /** Runtime switch; only meaningful when {@link #loaded}. Written and read on the server thread. */
    public volatile boolean on;

    Module(String key, String... names) {
        this.key = key;
        this.names = names;
        this.c = new long[names.length];
    }

    public boolean active() {
        return this.on;
    }

    public void inc(int counter) {
        if (Stats.ON) {
            this.c[counter]++;
        }
    }

    public void add(int counter, long n) {
        if (Stats.ON) {
            this.c[counter] += n;
        }
    }

    /** Counts that must never be lost (verify mismatches), regardless of the stats switch. */
    public void incAlways(int counter) {
        this.c[counter]++;
    }

    /**
     * Counters are fixed at boot: with stats off the JIT folds every inc() away, which matters on the gate's
     * per-trigger path. Initialised on first use, after BsConfig.load().
     */
    public static final class Stats {
        public static final boolean ON = BsConfig.stats;

        private Stats() {
        }
    }

    public static Module byKey(String key) {
        for (Module m : values()) {
            if (m.key.equalsIgnoreCase(key) || m.name().equalsIgnoreCase(key)) {
                return m;
            }
        }
        return null;
    }
}
