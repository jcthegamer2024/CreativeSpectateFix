package org.sylvarion.tcplatformfix.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonCameraArmsMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void tcplatformfix$useCameraAvatarForFirstPersonArms(
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            PlayerRenderState playerRenderState,
            FirstPersonHandsAndItemsRenderState handsAndItemsState,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity camera = minecraft.getCameraEntity();
        if (minecraft.player == null || camera == null || camera == minecraft.player) {
            return;
        }

        // Minecraft's world viewport uses this interpolated camera rotation, while the
        // hand renderer's view angles were extracted from the local player.
        Camera viewport = minecraft.gameRenderer.mainCamera();
        handsAndItemsState.viewXRot = viewport.xRot();
        handsAndItemsState.viewYRot = viewport.yRot();

        // The hand sway is (view - bob) * 0.1. The bob values still come from the local
        // player, so they must match the camera too, otherwise any gap between the camera
        // and local player angles turns into hand rotation.
        handsAndItemsState.xBob = viewport.xRot();
        handsAndItemsState.yBob = viewport.yRot();

        // Keep local swing/use flags, but make the rendered arm pose follow the active POV pitch.
        if (playerRenderState.avatarRenderState != null) {
            playerRenderState.avatarRenderState.xRot = viewport.xRot();
        }
    }
}