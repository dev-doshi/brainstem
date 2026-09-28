package dev.brainstem;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Lockstep differential checking: an optimised path is re-run (or cross-checked) against vanilla.
 * Any mismatch is logged once per site with full context and switches the module off (fail-safe).
 */
public final class Verify {
    public enum Mode { OFF, SAMPLE, ALL }

    private static int tick;
    private static final Set<String> LOGGED = new HashSet<>();

    private Verify() {
    }

    public static Mode mode() {
        return BsConfig.verify;
    }

    public static void setMode(Mode m) {
        BsConfig.verify = m;
    }

    /** Whether this call should be checked. Server thread only. */
    public static boolean sample() {
        Mode m = BsConfig.verify;
        if (m == Mode.OFF) {
            return false;
        }
        return m == Mode.ALL || (++tick & (BsConfig.verifySampleRate - 1)) == 0;
    }

    public static void mismatch(Module module, int counter, String site, Supplier<String> context) {
        module.incAlways(counter);
        if (module.on) {
            module.on = false;
            Brainstem.LOG.error("[Brainstem] verify mismatch in {} at {} - module DISABLED for this session", module.key, site);
        }
        if (LOGGED.add(module.key + '/' + site)) {
            Brainstem.LOG.error("[Brainstem] mismatch context ({}): {}", site, context.get());
        }
    }
}
