package dev.brainstem.mixin.gate;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.kinds.App;
import dev.brainstem.Module;
import dev.brainstem.gate.BsGate;
import dev.brainstem.gate.BsNode;
import java.util.function.Function;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BehaviorBuilder.class)
public abstract class BehaviorBuilderMixin implements BsNode {
    /** The root node handed to get() inside create(Function); create is re-entered on other threads during worldgen. */
    @Unique
    private static final ThreadLocal<Object> bs$root = new ThreadLocal<>();

    @Unique private boolean bs$known;
    @Unique private boolean bs$pure;
    @Unique private MemoryModuleType<?>[] bs$types;
    @Unique private MemoryStatus[] bs$statuses;

    @Override
    public boolean bs$known() {
        return this.bs$known;
    }

    @Override
    public boolean bs$pure() {
        return this.bs$pure;
    }

    @Override
    public MemoryModuleType<?>[] bs$types() {
        return this.bs$types;
    }

    @Override
    public MemoryStatus[] bs$statuses() {
        return this.bs$statuses;
    }

    @Override
    public void bs$setShape(boolean pure, MemoryModuleType<?>[] types, MemoryStatus[] statuses) {
        this.bs$types = types;
        this.bs$statuses = statuses;
        this.bs$pure = pure;
        this.bs$known = true;
    }

    @ModifyArg(
        method = "create(Ljava/util/function/Function;)Lnet/minecraft/world/entity/ai/behavior/OneShot;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder;get(Lcom/mojang/datafixers/kinds/App;)Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$TriggerWithResult;"),
        require = 1
    )
    private static App<?, ?> bs$captureRoot(App<?, ?> root) {
        bs$root.set(root);
        return root;
    }

    @ModifyReturnValue(method = "create(Ljava/util/function/Function;)Lnet/minecraft/world/entity/ai/behavior/OneShot;", at = @At("RETURN"), require = 1)
    private static OneShot<?> bs$attachPrefix(OneShot<?> oneShot, Function<?, ?> builder) {
        Object root = bs$root.get();
        bs$root.remove();
        if (root instanceof BsNode node && oneShot instanceof BsGate gate) {
            gate.bs$setPrefix(node);
            Module.GATE.inc(1);
            if (node.bs$known() && node.bs$types().length > 0) {
                Module.GATE.inc(0);
            }
        }
        return oneShot;
    }
}
