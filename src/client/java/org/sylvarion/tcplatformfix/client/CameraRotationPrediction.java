package org.sylvarion.tcplatformfix.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Keeps raw local mouse input visible while TrainCarts' spectated-camera update is in flight. */
public final class CameraRotationPrediction {
    /** A/B test switch: false = camera shows only TrainCarts' own rotation, no local prediction. */
    private static final boolean ENABLED = false;

    private static final long RECONCILIATION_NANOS = 150_000_000L;

    private static Entity cameraEntity;
    private static float yawOffset;
    private static float pitchOffset;
    private static float yawCorrection;
    private static float pitchCorrection;
    private static long yawCorrectionStarted;
    private static long pitchCorrectionStarted;

    private CameraRotationPrediction() {
    }

    public static void addMouseDelta(Entity camera, float yawDelta, float pitchDelta) {
        selectCamera(camera);
        if (!ENABLED || camera == null) {
            return;
        }
        yawOffset += yawDelta;
        pitchOffset = Mth.clamp(pitchOffset + pitchDelta, -180.0f, 180.0f);
    }

    public static float yawOffset(Entity entity) {
        if (!ENABLED) {
            return 0.0f;
        }
        selectCurrentCamera();
        return entity == cameraEntity ? yawOffset + correction(yawCorrection, yawCorrectionStarted) : 0.0f;
    }

    public static float pitchOffset(Entity entity) {
        if (!ENABLED) {
            return 0.0f;
        }
        selectCurrentCamera();
        return entity == cameraEntity ? pitchOffset + correction(pitchCorrection, pitchCorrectionStarted) : 0.0f;
    }

    public static void acknowledgeHeadRotation(Entity entity) {
        if (!ENABLED) {
            return;
        }
        selectCurrentCamera();
        if (entity == cameraEntity) {
            yawCorrection = yawOffset(entity);
            yawCorrectionStarted = System.nanoTime();
            yawOffset = 0.0f;
        }
    }

    public static void acknowledgeBodyRotation(Entity entity) {
        if (!ENABLED) {
            return;
        }
        selectCurrentCamera();
        if (entity == cameraEntity) {
            pitchCorrection = pitchOffset(entity);
            pitchCorrectionStarted = System.nanoTime();
            pitchOffset = 0.0f;
        }
    }

    public static void onCameraChanged(Entity camera) {
        selectCamera(camera);
    }

    private static float correction(float amount, long started) {
        if (amount == 0.0f) {
            return 0.0f;
        }
        float progress = Mth.clamp((System.nanoTime() - started) / (float) RECONCILIATION_NANOS, 0.0f, 1.0f);
        float smoothProgress = progress * progress * (3.0f - 2.0f * progress);
        return amount * (1.0f - smoothProgress);
    }

    private static void selectCurrentCamera() {
        selectCamera(Minecraft.getInstance().getCameraEntity());
    }

    private static void selectCamera(Entity camera) {
        if (camera != cameraEntity) {
            cameraEntity = camera;
            yawOffset = 0.0f;
            pitchOffset = 0.0f;
            yawCorrection = 0.0f;
            pitchCorrection = 0.0f;
            yawCorrectionStarted = 0L;
            pitchCorrectionStarted = 0L;
        }
    }
}