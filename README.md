# FPS Mode Toggle — Forge 1.20.1

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-blue)
![Forge 47+](https://img.shields.io/badge/Forge-47%2B-orange)
![Client side](https://img.shields.io/badge/Side-Client-lightgrey)

A client-side, fully reversible performance mod. Press **F8** to switch to a low-graphics profile and freeze animated textures. Press **F8** again to restore your original settings.

## What it does when enabled
- Graphics **Fast**, clouds **off**, entity shadows **off**, Ambient Occlusion **off**.
- Mipmap levels **0**, biome blend **0**, entity distance scaling **10%**.
- VSync **off**, framerate limit **unlimited**.
- Particles **Minimal**, and large explosion particle effects are removed. Explosion damage and world simulation are not changed.
- **Animated textures are frozen** in Minecraft and mod texture atlases by pausing their tickers directly. No Embeddium is needed and no resource reload happens, so there is no loading screen when you press F8.

## Render and chunk settings are never touched
This mod does **not** change render distance, simulation distance, chunk loading or any related setting. If you update from an older version that changed them, the saved original values are restored once, and the mod stops managing them.

## Restore and persistence
- Original values are saved to `config/fpsmode-original.properties`.
- If the game is closed while the mode is on, it is re-applied on the next launch.
- Press **F8** again to restore the saved settings and the animated textures.
- The key can be rebound in **Controls → FPS Mode**.

## Limitations
- Frozen textures only stop texture-frame animations. Entity movement, block entity models and mechanical parts such as Create gears keep animating.
- Some mod textures may look static while the mode is on.
- Explosion effects from other mods may still appear if they use other particle classes.
- It lowers rendering load. It does **not** change Java heap, chunk settings or server performance, so memory savings are modest.
- With VSync off, screen tearing is possible on some monitors.

## Installation
1. Minecraft Java 1.20.1, Forge 47.x, Java 17.
2. Put the jar in the `mods` folder and remove any older FPS Mode version.
3. Client only. Do not install it on a server.

## Building from source
```
./gradlew build
```
Requires JDK 17. The jar is written to `build/libs/`.

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).

## License
[MIT](LICENSE) © 2026 Yaseen
