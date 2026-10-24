# FPS Mode Toggle — Forge 1.20.1

A client-side, reversible performance mod. Press **F8** to switch to a low-graphics profile and freeze animated textures. Press **F8** again to restore your original settings.

## Features
- Fast graphics, clouds off, entity shadows off, ambient occlusion off.
- Mipmap levels and biome blend set to zero; entity distance scaling set to 10%.
- VSync off, frame limit raised, particles set to Minimal.
- Large explosion particle effects are removed; explosion damage and world simulation are unchanged.
- Animated textures are paused directly without resource reloads.
- Render distance, simulation distance, chunk loading and related settings are not changed.
- Original settings are backed up in `config/fpsmode-original.properties`.

## Install
Minecraft Java 1.20.1, Forge 47.x, Java 17. Put the built JAR in your client `mods` folder. Do not install on a server.

## Build
Run `./gradlew build` with JDK 17. The JAR is written to `build/libs/`.

## License
MIT — see [LICENSE](LICENSE).