package dev.brainstem;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Skips the mixins of modules disabled in config/brainstem.json, so a disabled module costs nothing. */
public final class BsMixinPlugin implements IMixinConfigPlugin {
    private static final String PKG = "dev.brainstem.mixin.";

    @Override
    public void onLoad(String mixinPackage) {
        BsConfig.load();
        for (Module m : Module.values()) {
            m.loaded = BsConfig.enabledAtBoot(m);
        }
        // The reach memo shares the probe's call-site wrappers.
        Module.REACH_PROBE.loaded |= Module.REACH_MEMO.loaded;
        Brainstem.LOG.info("[Brainstem] modules: mtvg={} oneShotGate={} competitorScan={} temptScan={} reachProbe={} reachMemo={} verify={}",
            BsConfig.mtvg, BsConfig.oneShotGate, BsConfig.competitorScan, BsConfig.temptScan, BsConfig.reachProbe, BsConfig.reachMemo, BsConfig.verify);
    }

    static Module moduleOf(String mixinClassName) {
        if (!mixinClassName.startsWith(PKG)) {
            return null;
        }
        String sub = mixinClassName.substring(PKG.length(), mixinClassName.indexOf('.', PKG.length()));
        return switch (sub) {
            case "village" -> Module.MTVG;
            case "gate" -> Module.GATE;
            case "jobsite" -> Module.JOBSITE;
            case "tempt" -> Module.TEMPT;
            case "pathprobe" -> Module.REACH_PROBE;
            case "pathmemo" -> Module.REACH_MEMO;
            default -> null;
        };
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Module m = moduleOf(mixinClassName);
        return m == null || m.loaded;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
