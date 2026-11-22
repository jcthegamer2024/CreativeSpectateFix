# TCPlatformFix handoff

## Target and symptom

- Fabric client mod for Minecraft 26.3; Gradle wrapper is 9.8.0.
- Current artifact version: `0.6`.
- The issue occurs on TrainCarts' new simulated platform POV: first-person arm/hand can desync from the view after repeated turns. User reports it in multiple game modes. A small mouse-to-camera delay was also reported.
- Test instance: `C:\Users\Joel\AppData\Roaming\ModrinthApp\profiles\Sylvarion Optimised 1.0.0 (1)`. User authorized reading logs there, not writing/copying artifacts there.

## TrainCarts source inspected

- Exact commit: `ea35c4942de847894927aeac1d9f9d3dc3e4f5d4` (CI build 1783; surface/platform feature).
- Relevant code: `attachments/surface/CollisionSurfaceTracker.java`, `attachments/surface/SimulatedPlayer.java`, `controller/player/pmc/PlayerMovementControllerSpectated.java`, `controller/player/SpectatedFakePlayer.java`, and `controller/player/ViewRotationTrackerImpl.java`.
- The surface controller uses a TrainCarts spectated fake player. `PlayerMovementControllerSpectated` starts a `ViewRotationTracker`; `ViewRotationTrackerImpl` listens for incoming look packets and cancels them after tracking. Therefore TrainCarts has server-side tracking active, but that alone does not prove vanilla's local first-person arms follow the active camera entity.
- The developer's hypothesis is to use the spectated entity's POV arms instead of the local player's arms, with a behavior change while a separate camera entity is active. User clarifies the desync also happens across game modes, so do not gate the fix on creative mode.
- TrainCarts mirrors equipment to its fake player. Its fake camera is represented client-side by a player/avatar entity.
- A source-tree search across the full 783-file Java source set was used to map every relevant movement, camera, rotation-tracking, packet-listener, and fake-entity call site. Main tick chain: `TrainUpdateController` -> `TrainCartsAttachmentViewerMap.updateCollisionSurfaces()` -> `CollisionSurfaceTracker.update()` -> `updateSimulatedPlayer()` -> `PlayerMovementController.update()` -> `PlayerMovementControllerSpectated.syncPosition()`.
- The spectated controller runs each server simulation tick. It drains accumulated look deltas from the tracker, applies them to `viewRotation`, passes that through `ArmorStandRotationInterpolation.update()`, updates the fake player's transform, and calls `fakePlayer.syncPosition(true)`. It also updates the server player rotation from the unpredicted `viewRotation`.
- `ArmorStandRotationInterpolation` is not an armor-stand camera. It predicts yaw by 3x and pitch by 4/3 to compensate for client rotation interpolation. `SpectatedFakePlayer.FakeVirtualPlayer` explicitly uses `EntityType.PLAYER`; `PitchSwappedEntity` can switch between three fake-player entities to avoid camera glitches near pitch flips.
- Rotation packets are tracked in two clientbound paths: entity position/rotation updates go through Minecraft's `EntityPositionSyncPacket` and living-entity stepped interpolation; head yaw is separately sent and `ClientPacketListener.handleRotateMob` calls `lerpHeadTo(yaw, 3)`. `Camera` takes the active entity's view yaw/pitch for the viewport. This is the correct area for the remaining camera latency/desync investigation.
- The source found in `SpectatedFakePlayer` mirrors equipment and health to the fake player, but contains no mirroring of swing/use animation packets. Therefore using the fake avatar's entire render state for first-person arms can lose the local player's swing/use state; retain local animation state unless explicitly mirrored.

## Current implementation: version 0.9 (loaded in test profile; user says result is still not good)

- `MouseHandlerMixin` captures the local player's actual post-sensitivity yaw/pitch delta after each `MouseHandler.turnPlayer` call. If the active camera is a separate entity, it accumulates those deltas in `CameraRotationPrediction` rather than writing them into the fake entity.
- `EntityViewRotationMixin` adds the pending pitch offset when the camera entity's view pitch is read. `LivingEntityViewRotationMixin` adds pending yaw at `getViewYRot`, which is the path the 26.3 `Camera` uses for a living fake-player POV.
- Incoming body/head rotation updates now move pending offsets into a smooth 150 ms render-time correction instead of clearing them immediately. This targets the camera jitter reported with 0.8, where hard resets could fight Minecraft's entity interpolation. Repeated updates rebase the correction from its current effective value.
- `FirstPersonCameraArmsMixin` keeps the local player's avatar animation state (TrainCarts does not mirror swing/use packets), uses the active viewport camera's current-frame view angles for the hand transform, and aligns the local avatar arm pitch to the viewport pitch.
- The previous absolute-angle copy in 0.4 caused visible yaw/pitch glitching. The previous direct per-entity delta mutation did not resolve the symptom and could be overwritten by incoming fake-player updates. Do not restore either implementation.
- `build/libs/tcplatformfix-0.6.jar` packaged successfully but user reports it still desyncs. It changed avatar state only and left first-person view angles sourced from the local player.
- `build/libs/tcplatformfix-0.7.jar` loaded in the test profile (latest log at 22:50:38) without a TCPlatformFix mixin failure; user reports the hand remains desynced. The avatar-state-only route did not fix the issue.
- User tested 0.8 and reports that mouse-to-camera input delay improved, but looking around became jittery and the hand still desynced. This is the latest in-game result.
- Version 0.9 was loaded by the test profile at 23:17:34 (latest `launcher_log.txt` identifies `tcplatformfix 0.9`). No TCPlatformFix mixin injection errors appear in that startup log. User reports the result is still not good; no more precise 0.9 symptom breakdown was given. No tests were run.
- Minecraft 26.3 merged deobfuscated bytecode was inspected locally. `FirstPersonHandsAndItemsRenderer.submitHandsWithItems` transforms the first-person pose using `FirstPersonHandsAndItemsRenderState.viewXRot/viewYRot` and passes `AvatarRenderState.xRot` into the arm/item submission path. 0.9 writes both view angles from `GameRenderer.mainCamera()` and sets local avatar pitch to that viewport pitch. This confirms the mixin targets the vanilla first-person path, but does not prove the temporal mismatch is solved.
- Fabric API tick callbacks are not per-frame mouse-look callbacks; the hand-render change uses a focused Mixin because it targets a vanilla render path. Fabric API remains available for lifecycle/tick integration if needed.

## Previous results

- 0.1: user reports no fix. It called `Entity.turn()` with already sensitivity-scaled deltas; bytecode showed `Entity.turn()` applies another 0.15 factor.
- 0.2: removed extra sensitivity multiplication but changed body yaw only; user reports no fix.
- 0.3: added living head yaw delta; user reports no fix.
- 0.4: copied absolute local-player yaw/pitch onto camera. User reports yaw/pitch glitching, especially when looking vertically. Do not use.
- 0.5: reverted to delta rotation approach. Not established as a fix.
- Older inspected profile logs were for 0.1/0.2, not 0.6. Check a fresh startup log before drawing conclusions about 0.6 mixin application.

## Next step

Do not issue another speculative build until the remaining 0.9 failure is characterized. The profile log only proves 0.9 loaded without mixin errors; it cannot diagnose visible jitter/desync. Ask whether the 0.9 camera jitter remains and request a short recording if possible, since the mismatch is temporal. Then compare the local-player angles, fake camera entity interpolated head/body angles, active viewport angles, and hand render-state angles on the same frames. Do not change TrainCarts' 3x yaw and 4/3 pitch interpolation compensation blindly.
