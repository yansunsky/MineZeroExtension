# MineZero Extension

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.172-orange.svg)](https://neoforged.net/)

A MineZero addon that adds Sophisticated Backpacks compatibility and an intelligent condition-based auto-checkpoint system with cascading probability gates.

## Features

### 1. Sophisticated Backpacks Compatibility
Ensures backpack contents are correctly restored when MineZero triggers a Return by Death. Implemented via Mixin injection.

### 2. Safe Condition Checkpoint Trigger
Automatically creates checkpoints when the player is in a safe state—overworld, daytime, full health, full hunger, no negative effects, and no hostile mobs nearby—using cascading probability gates to avoid excessive saves. Disables MineZero's native `autoCheckpointEnabled` when active.

## Dependencies

| Mod | Requirement |
|-----|-------------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.0+ |
| [MineZero](https://github.com/AMPerez04/MineZero) | 1.0+ (required) |
| Sophisticated Backpacks | 3.20+ (optional) |

## Commands

| Command | Description |
|---------|-------------|
| `/gamerule safeCheckpointEnabled true\|false` | Enable/disable safe checkpoint trigger |
| `/minezeroextension debugmode true\|false` | Toggle broadcast notification on save |

## Configuration

`config/minezero_extension-common.toml`:

```toml
[safeCheckpoint]
checkIntervalTicks = 400           # 20 seconds
enemySearchRadius = 24

[safeCheckpoint.enabled]
overworld = true
daytime = true
healthFull = true
hungerFull = true
noNegativeEffects = true
noHostileNearby = true

[safeCheckpoint.chances]
overworldChance = 0.90
daytimeChance = 0.75
healthFullChance = 0.75
hungerFullChance = 0.55
noNegativeEffectsChance = 0.60
noHostileNearbyChance = 1.00
```

Cascade: 0.90 × 0.75 × 0.75 × 0.55 × 0.60 × 1.00 ≈ 0.167 → ~1 save per 2 minutes when all conditions are met.

## Build

```bash
./gradlew jar
```

## License

Apache License 2.0
