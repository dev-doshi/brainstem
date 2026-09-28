package dev.brainstem.mixin.gate;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.kinds.App;
import dev.brainstem.gate.GateShapes;
import java.util.function.Function;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Each combinator's tryTrigger evaluates its argument nodes left to right, returning null at the first null, and
 * evaluates the function node last. DFU's defaults (ap, ap5+, apply2...) all route through these overrides.
 */
@Mixin(BehaviorBuilder.Instance.class)
public abstract class InstanceMixin {
    @ModifyReturnValue(method = "map", at = @At("RETURN"), require = 1)
    private BehaviorBuilder<?, ?> bs$map(BehaviorBuilder<?, ?> node, Function<?, ?> func, App<?, ?> ts) {
        GateShapes.combine(node, ts);
        return node;
    }

    @ModifyReturnValue(method = "ap2", at = @At("RETURN"), require = 1)
    private BehaviorBuilder<?, ?> bs$ap2(BehaviorBuilder<?, ?> node, App<?, ?> func, App<?, ?> a, App<?, ?> b) {
        GateShapes.combine(node, a, b, func);
        return node;
    }

    @ModifyReturnValue(method = "ap3", at = @At("RETURN"), require = 1)
    private BehaviorBuilder<?, ?> bs$ap3(BehaviorBuilder<?, ?> node, App<?, ?> func, App<?, ?> t1, App<?, ?> t2, App<?, ?> t3) {
        GateShapes.combine(node, t1, t2, t3, func);
        return node;
    }

    @ModifyReturnValue(method = "ap4", at = @At("RETURN"), require = 1)
    private BehaviorBuilder<?, ?> bs$ap4(BehaviorBuilder<?, ?> node, App<?, ?> func, App<?, ?> t1, App<?, ?> t2, App<?, ?> t3, App<?, ?> t4) {
        GateShapes.combine(node, t1, t2, t3, t4, func);
        return node;
    }

    /** lift1(function) returns a -> node; DFU's ap(func, arg), used by group-of-one, is lift1(func).apply(arg). */
    @ModifyReturnValue(method = "lambda$lift1$0", at = @At("RETURN"), require = 1)
    private App<?, ?> bs$lift1(App<?, ?> node, App<?, ?> function, App<?, ?> a) {
        GateShapes.combine(node, a, function);
        return node;
    }
}
