package dev.brainstem.mixin.jobsite;

import dev.brainstem.jobsite.JobSiteHolders;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Both private setMemoryInternal bodies: the funnel for setMemory, setMemoryWithExpiry, the Optional overload and
 * loading saved memories (setMemoryInternal(MemoryMap.Value) and brain refresh). Lithium wraps the call sites, not
 * these bodies.
 */
@Mixin(Brain.class)
public abstract class BrainMixin {
    @Inject(method = "setMemoryInternal(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;Ljava/lang/Object;J)V", at = @At("TAIL"), require = 1)
    private void bs$trackJobSite(MemoryModuleType<?> type, Object value, long timeToLive, CallbackInfo ci) {
        if (type == MemoryModuleType.JOB_SITE && value instanceof GlobalPos pos) {
            JobSiteHolders.add(pos, (Brain<?>) (Object) this);
        }
    }

    @Inject(method = "setMemoryInternal(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;Ljava/lang/Object;)V", at = @At("TAIL"), require = 1)
    private void bs$trackJobSite(MemoryModuleType<?> type, Object value, CallbackInfo ci) {
        if (type == MemoryModuleType.JOB_SITE && value instanceof GlobalPos pos) {
            JobSiteHolders.add(pos, (Brain<?>) (Object) this);
        }
    }
}
