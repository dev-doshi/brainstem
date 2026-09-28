package dev.brainstem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** config/brainstem.json. Read once by the mixin plugin, before any target class loads. */
public final class BsConfig {
    public static final Path PATH = Paths.get("config", "brainstem.json");
    private static boolean loaded;

    public static boolean mtvg = true;
    public static boolean oneShotGate = true;
    public static boolean competitorScan = true;
    public static boolean temptScan = true;
    public static boolean reachProbe = false;
    public static boolean reachMemo = false;
    /** Per-module counters; forced on by verify or the reach probe/memo, whose output is the counters. */
    public static boolean stats = false;
    public static Verify.Mode verify = Verify.Mode.OFF;
    /** Power of two: in sample mode one call in this many is checked against vanilla. */
    public static int verifySampleRate = 64;

    private BsConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            if (Files.exists(PATH)) {
                JsonObject o = JsonParser.parseString(Files.readString(PATH)).getAsJsonObject();
                mtvg = bool(o, "mtvg", mtvg);
                oneShotGate = bool(o, "oneShotGate", oneShotGate);
                competitorScan = bool(o, "competitorScan", competitorScan);
                temptScan = bool(o, "temptScan", temptScan);
                reachProbe = bool(o, "reachProbe", reachProbe);
                reachMemo = bool(o, "reachMemo", reachMemo);
                stats = bool(o, "stats", stats);
                if (o.has("verify")) {
                    verify = Verify.Mode.valueOf(o.get("verify").getAsString().toUpperCase());
                }
                if (o.has("verifySampleRate")) {
                    verifySampleRate = Integer.highestOneBit(Math.max(1, o.get("verifySampleRate").getAsInt()));
                }
            } else {
                write();
            }
        } catch (Exception e) {
            Brainstem.LOG.error("[Brainstem] could not read {}, using defaults", PATH, e);
        }
        stats |= verify != Verify.Mode.OFF || reachProbe || reachMemo;
        Module.MTVG.on = mtvg;
        Module.GATE.on = oneShotGate;
        Module.JOBSITE.on = competitorScan;
        Module.TEMPT.on = temptScan;
        Module.REACH_PROBE.on = reachProbe;
        Module.REACH_MEMO.on = reachMemo;
    }

    public static boolean enabledAtBoot(Module m) {
        return switch (m) {
            case MTVG -> mtvg;
            case GATE -> oneShotGate;
            case JOBSITE -> competitorScan;
            case TEMPT -> temptScan;
            case REACH_PROBE -> reachProbe;
            case REACH_MEMO -> reachMemo;
        };
    }

    private static boolean bool(JsonObject o, String k, boolean def) {
        return o.has(k) ? o.get(k).getAsBoolean() : def;
    }

    private static void write() throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("mtvg", mtvg);
        o.addProperty("oneShotGate", oneShotGate);
        o.addProperty("competitorScan", competitorScan);
        o.addProperty("temptScan", temptScan);
        o.addProperty("reachProbe", reachProbe);
        o.addProperty("reachMemo", reachMemo);
        o.addProperty("stats", stats);
        o.addProperty("verify", verify.name().toLowerCase());
        o.addProperty("verifySampleRate", verifySampleRate);
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        Files.createDirectories(PATH.getParent());
        Files.writeString(PATH, g.toJson(o));
    }
}
