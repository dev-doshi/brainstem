package dev.brainstem.mixin.gate;

import dev.brainstem.gate.BsNode;
import dev.brainstem.gate.GateShapes;
import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Constant.tryTrigger just returns its value: pure, no conditions. */
@Mixin(targets = "net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Constant")
public abstract class ConstantMixin {
    @Inject(method = "<init>(Ljava/lang/Object;Ljava/util/function/Supplier;)V", at = @At("TAIL"), require = 1)
    private void bs$leaf(Object value, Supplier<String> debugString, CallbackInfo ci) {
        ((BsNode) this).bs$setShape(value != null, GateShapes.NO_TYPES, GateShapes.NO_STATUSES);
    }
}
