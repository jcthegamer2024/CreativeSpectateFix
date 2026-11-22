package org.sylvarion.creativespectatefix.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.sylvarion.creativespectatefix.client.CameraRotationPrediction;

@Mixin(LivingEntity.class)
public abstract class LivingEntityViewRotationMixin {
    @Inject(method = "getViewYRot(F)F", at = @At("RETURN"), cancellable = true)
    private void creativespectatefix$applyPredictedCameraYaw(float partialTick, CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self == Minecraft.getInstance().getCameraEntity()) {
            cir.setReturnValue(cir.getReturnValue() + CameraRotationPrediction.yawOffset(self));
        }
    }

    @Inject(method = "lerpHeadTo", at = @At("TAIL"))
    private void creativespectatefix$acknowledgeCameraHeadRotation(float yaw, int steps, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        CameraRotationPrediction.acknowledgeHeadRotation((LivingEntity) (Object) this);
    }
}
