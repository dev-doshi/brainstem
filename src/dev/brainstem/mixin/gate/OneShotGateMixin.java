package dev.brainstem.mixin.gate;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.brainstem.Module;
import dev.brainstem.Verify;
import dev.brainstem.gate.BsGate;
import dev.brainstem.gate.BsNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The anonymous OneShot from BehaviorBuilder.create. If any condition of the root's side-effect-free prefix fails,
 * vanilla's tryTrigger would return null before doing anything else, so trigger returns false: answer that directly.
 */
@Mixin(targets = "net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$1")
public abstract class OneShotGateMixin implements BsGate {
    @Unique private static final int TRIGGERS = 2, FAILS = 3, PASSES = 4, VERIFIED = 5, MISMATCHES = 6;

    @Unique private MemoryModuleType<?>[] bs$types;
    @Unique private MemoryStatus[] bs$statuses;
    /** Verify: the gate failed but vanilla is being allowed to run; its tryTrigger must return null. */
    @Unique private boolean bs$expectNull;

    @Override
    public void bs$setPrefix(BsNode root) {
        if (root.bs$known() && root.bs$types().length > 0) {
            this.bs$types = root.bs$types();
            this.bs$statuses = root.bs$statuses();
        }
    }

    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)Z", at = @At("HEAD"), cancellable = true, require = 1)
    private void bs$gate(ServerLevel level, LivingEntity body, long timestamp, CallbackInfoReturnable<Boolean> cir) {
        MemoryModuleType<?>[] types = this.bs$types;
        if (types == null || !Module.GATE.on) {
            return;
        }
        Module m = Module.GATE;
        m.inc(TRIGGERS);
        MemoryStatus[] statuses = this.bs$statuses;
        Brain<?> brain = body.getBrain();
        for (int i = 0; i < types.length; i++) {
            if (!brain.checkMemory(types[i], statuses[i])) {
                m.inc(FAILS);
                if (Verify.sample()) {
                    m.inc(VERIFIED);
                    this.bs$expectNull = true;
                    return;
                }
                cir.setReturnValue(false);
                return;
            }
        }
        m.inc(PASSES);
    }

    @ModifyExpressionValue(
        method = "trigger(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$TriggerWithResult;tryTrigger(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)Ljava/lang/Object;"),
        require = 1
    )
    private Object bs$verify(Object trigger) {
        if (this.bs$expectNull) {
            this.bs$expectNull = false;
            if (trigger != null) {
                Verify.mismatch(Module.GATE, MISMATCHES, "trigger",
                    () -> "gate predicted failure but tryTrigger succeeded for " + this);
            }
        }
        return trigger;
    }
}
