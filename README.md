# CreativeSpectateFix

A small client-side Fabric mod that aims to fix the first-person view on **TrainCarts simulated platform POV**.

This mod fixes the creative spectator issue found in TrainCarts' platform feature. It keeps the player's hand in the POV synced to mouse movement, instead of drifting away from the viewport.

## The problem

When you ride a TrainCarts simulated platform, the camera is driven by a spectated fake player. Vanilla Minecraft still draws your first-person hand and HUD from your real player, so the two drift apart. After a few turns, the hand and held item stop following your mouse and no longer match the viewport.

## What this mod fixes

- **Hand and item stay in sync.** Your arm and held item follow the camera, so they stay locked to the viewport however much you turn. This works in all game modes, not just creative.
- **The hotbar displays properly.** On the platform POV the hotbar used to show up empty, because the game was drawing the fake camera player's inventory. It now shows your real hotbar, selected slot and scrolling. Health, hunger and armor bars follow your real player too.
- **Normal spectating is untouched.** Vanilla spectator mode behaves as usual.

## Requirements

- Minecraft **26.3**
- Fabric Loader **0.20.1** or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- TrainCarts on the server

The mod is **client-side only**. Nothing needs to be installed on the server.

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 26.3.
2. Download the latest `tcplatformfix-<version>.jar` from the Releases page.
3. Put the jar in your `mods` folder.

## Known issues

- There is a small delay between moving the mouse and the camera responding. This comes from TrainCarts' server-driven camera updates.
- Held-item sway is turned off while on the platform, so the hand stays locked to the view.

## Building

```
./gradlew build
```

On Windows, use `gradlew build`. The jar is written to `build/libs/`.

## Reporting problems

Please open an issue and include your Minecraft version, TrainCarts version, and `latest.log`.

## License

Add your license here.
