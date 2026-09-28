package dev.brainstem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Brainstem {
    public static final Logger LOG = LoggerFactory.getLogger("Brainstem");

    private Brainstem() {
    }

    public static String statsText() {
        StringBuilder sb = new StringBuilder("Brainstem verify=").append(Verify.mode().name().toLowerCase()).append(Module.Stats.ON ? "" : " (stats off: set \"stats\": true)");
        for (Module m : Module.values()) {
            sb.append("\n ").append(m.key).append(m.loaded ? (m.on ? " [on]" : " [off]") : " [not loaded]");
            for (int i = 0; i < m.names.length; i++) {
                sb.append(' ').append(m.names[i]).append('=').append(m.c[i]);
            }
        }
        return sb.toString();
    }

    public static void reset() {
        for (Module m : Module.values()) {
            java.util.Arrays.fill(m.c, 0L);
        }
    }
}
