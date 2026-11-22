package org.sylvarion.tcplatformfix.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.sylvarion.tcplatformfix.client.CameraRotationPrediction;

@Mixin(Entity.class)
public abstract class EntityViewRotationMixin {
    @Inject(method = "getViewXRot(F)F", at = @At("RETURN"), cancellable = true)
    private void tcplatformfix$applyPredictedCameraPitch(float partialTick, CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity) (Object) this;
        if (self == Minecraft.getInstance().getCameraEntity()) {
            cir.setReturnValue(Mth.clamp(cir.getReturnValue() + CameraRotationPrediction.pitchOffset(self), -90.0f, 90.0f));
        }
    }

    @Inject(method = "moveOrInterpolateTo", at = @At("TAIL"))
    private void tcplatformfix$acknowledgeCameraBodyRotation(
            net.minecraft.world.entity.PositionPath path,
            float yRot,
            float xRot,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci
    ) {
        CameraRotationPrediction.acknowledgeBodyRotation((Entity) (Object) this);
    }

    @Inject(method = "snapTo(Lnet/minecraft/world/phys/Vec3;FF)V", at = @At("TAIL"))
    private void tcplatformfix$acknowledgeCameraBodySnap(
            net.minecraft.world.phys.Vec3 position,
            float yRot,
            float xRot,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci
    ) {
        CameraRotationPrediction.acknowledgeBodyRotation((Entity) (Object) this);
    }
}
