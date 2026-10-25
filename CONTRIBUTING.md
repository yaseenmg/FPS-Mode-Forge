# Contributing

Bug reports, ideas, translations and pull requests are welcome.

## Development setup
- JDK 17
- `./gradlew build` builds the jar into `build/libs/`
- `./gradlew runClient` launches a dev client

## Guidelines
- Keep the mod ID `fpsmode` stable.
- The mod must stay client-side only and fully reversible.
- Do not change render distance, simulation distance or chunk settings.
- Open an issue before large changes.

## Pull requests
1. Fork the repo and create a branch.
2. Test toggling with F8 on and off in Minecraft 1.20.1 / Forge 47.
3. Describe what changed and why.
