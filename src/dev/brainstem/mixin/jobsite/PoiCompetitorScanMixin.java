package dev.brainstem.mixin.jobsite;

import dev.brainstem.Module;
import dev.brainstem.Verify;
import dev.brainstem.jobsite.JobSiteHolders;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PoiCompetitorScan;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The getType(pos).ifPresent consumer. Its stream can only call selectWinner (the one side effect) for a villager
 * whose JOB_SITE equals pos; if no other brain holds pos, the stream finds nothing and the consumer is a no-op.
 */
@Mixin(PoiCompetitorScan.class)
public abstract class PoiCompetitorScanMixin {
    private static final int SCANS = 0, FAST = 1, SLOW = 2, VERIFIED = 3, MISMATCHES = 4;

    @Inject(method = "lambda$create$3", at = @At("HEAD"), cancellable = true, require = 1)
    private static void bs$noCompetitor(
        BehaviorBuilder.Instance<?> i, MemoryAccessor<?, ?> nearestEntities, Villager body, GlobalPos pos, Holder<PoiType> poiType, CallbackInfo ci
    ) {
        Module m = Module.JOBSITE;
        if (!m.on) {
            return;
        }
        m.inc(SCANS);
        if (JobSiteHolders.anyOther(pos, body.getBrain())) {
            m.inc(SLOW);
            return;
        }
        m.inc(FAST);
        if (Verify.sample()) {
            m.inc(VERIFIED);
            int competitors = 0;
            for (LivingEntity v : body.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of())) {
                if (v instanceof Villager other && v != body && other.isAlive()) {
                    Optional<GlobalPos> site = other.getBrain().getMemory(MemoryModuleType.JOB_SITE);
                    if (site.isPresent() && pos.equals(site.get()) && other.getVillagerData().profession().value().heldJobSite().test(poiType)) {
                        competitors++;
                    }
                }
            }
            if (competitors > 0) {
                int n = competitors;
                Verify.mismatch(m, MISMATCHES, "competitors",
                    () -> body + " job site " + pos + ": index saw no other holder but " + n + " competitors exist");
                return;
            }
        }
        ci.cancel();
    }
}
