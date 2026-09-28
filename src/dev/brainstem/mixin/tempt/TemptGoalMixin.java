package dev.brainstem.mixin.tempt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.brainstem.Module;
import dev.brainstem.Verify;
import dev.brainstem.tempt.TemptScan;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TemptGoal.class)
public abstract class TemptGoalMixin {
    @WrapOperation(
        method = "canUse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getNearestPlayer(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/player/Player;"),
        require = 1
    )
    private Player bs$nearestTempter(ServerLevel level, TargetingConditions conditions, LivingEntity mob, Operation<Player> original) {
        if (!Module.TEMPT.on) {
            return original.call(level, conditions, mob);
        }
        Player fast = TemptScan.nearest(level, conditions, mob);
        if (Verify.sample()) {
            Module.TEMPT.inc(TemptScan.VERIFIED);
            Player vanilla = original.call(level, conditions, mob);
            if (vanilla != fast) {
                Verify.mismatch(Module.TEMPT, TemptScan.MISMATCHES, "nearest",
                    () -> mob + " fast=" + fast + " vanilla=" + vanilla);
            }
            return vanilla;
        }
        return fast;
    }
}
