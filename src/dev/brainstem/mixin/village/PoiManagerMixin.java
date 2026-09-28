package dev.brainstem.mixin.village;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.brainstem.village.BsPoiManager;
import dev.brainstem.village.VillageCenterIndex;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PoiManager.class)
public abstract class PoiManagerMixin implements BsPoiManager {
    @Unique
    private final VillageCenterIndex bs$index = new VillageCenterIndex();

    @Override
    public VillageCenterIndex bs$villageIndex() {
        return this.bs$index;
    }

    /** Every distance-tracker implementation learns a section's source state from this method's latest result. */
    @ModifyReturnValue(method = "isVillageCenter(J)Z", at = @At("RETURN"))
    private boolean bs$trackSource(boolean isCenter, long sectionPos) {
        this.bs$index.set(sectionPos, isCenter);
        return isCenter;
    }
}
