package org.sylvarion.tcplatformfix.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public abstract class HUDCameraPlayerMixin {
    @Inject(method = "getCameraPlayer", at = @At("RETURN"), cancellable = true)
    private void tcplatformfix$showLocalPlayerHotbar(CallbackInfoReturnable<Player> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        Player cameraPlayer = cir.getReturnValue();
        Player local = minecraft.player;
        if (cameraPlayer == null || local == null || cameraPlayer == local || minecraft.gameMode == null) {
            return;
        }

        // Vanilla only lets the camera sit on another player while spectating. Outside
        // spectator mode this is TrainCarts' fake camera player, whose inventory is not
        // the real one (it only mirrors equipment), so draw the local player's HUD instead.
        if (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            cir.setReturnValue(local);
        }
    }
}