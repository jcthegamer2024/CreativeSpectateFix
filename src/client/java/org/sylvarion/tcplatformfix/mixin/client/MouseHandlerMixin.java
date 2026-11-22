package org.sylvarion.tcplatformfix.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.sylvarion.tcplatformfix.client.CameraRotationPrediction;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Unique private float tcplatformfix$yawBeforeTurn;
    @Unique private float tcplatformfix$pitchBeforeTurn;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void tcplatformfix$captureViewBeforeMouseTurn(double frameTime, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            this.tcplatformfix$yawBeforeTurn = player.getYRot();
            this.tcplatformfix$pitchBeforeTurn = player.getXRot();
        }
    }

    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void tcplatformfix$predictSpectatedCameraRotation(double frameTime, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Entity camera = minecraft.getCameraEntity();
        if (player == null || camera == null || camera == player) {
            CameraRotationPrediction.onCameraChanged(camera);
            return;
        }

        float yawDelta = Mth.wrapDegrees(player.getYRot() - this.tcplatformfix$yawBeforeTurn);
        float pitchDelta = player.getXRot() - this.tcplatformfix$pitchBeforeTurn;
        if (yawDelta != 0.0f || pitchDelta != 0.0f) {
            CameraRotationPrediction.addMouseDelta(camera, yawDelta, pitchDelta);
        }
    }
}
