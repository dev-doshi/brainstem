package dev.brainstem.mixin.gate;

import dev.brainstem.gate.BsNode;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PureMemory.tryTrigger returns null exactly when Brain.checkMemory(memory, condition()) is false: an unregistered
 * slot gives null, Present/Absent return null on the wrong emptiness, Registered never fails otherwise. Its only
 * other effect is allocating the accessor.
 */
@Mixin(targets = "net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$PureMemory")
public abstract class PureMemoryMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryCondition;)V", at = @At("TAIL"), require = 1)
    private void bs$leaf(MemoryCondition<?, ?> condition, CallbackInfo ci) {
        Class<?> c = condition.getClass();
        if (c == MemoryCondition.Present.class || c == MemoryCondition.Absent.class || c == MemoryCondition.Registered.class) {
            ((BsNode) this).bs$setShape(true, new MemoryModuleType<?>[] {condition.memory()}, new MemoryStatus[] {condition.condition()});
        }
    }
}
