# MineZero Extension

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.172-orange.svg)](https://neoforged.net/)

MineZero 拓展附属模组。在 MineZero 基础上提供额外功能。

## 功能

### 1. Sophisticated Backpacks 兼容
MineZero 触发回归后，精妙背包内的物品正确回档。通过 Mixin 注入实现。

### 2. 安全条件触发检查点
当玩家处于安全状态（主世界、白天、满血、满饥饿、无负面效果、无敌对生物）时，通过**级联概率**自动创建检查点。避免频繁保存的性能开销。

每条件通过后掷骰一次，默认累积概率约 **1 次/分钟**，可通过配置文件调整。

## 依赖

| 模组 | 要求 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.0+ |
| [MineZero](https://github.com/AMPerez04/MineZero) | 1.0+ (required) |
| Sophisticated Backpacks | 3.20+ (optional) |

## 配置

`config/minezero_extension-common.toml`：

```toml
[safeCheckpoint]
enabled = true
checkIntervalTicks = 100           # 5秒
enemySearchRadius = 24             # 敌对生物搜索半径

[safeCheckpoint.chances]
overworldChance = 0.90             # 90%
daytimeChance = 0.65               # 65%
healthFullChance = 0.70            # 70%
hungerFullChance = 0.40            # 40%
noNegativeEffectsChance = 0.50     # 50%
noHostileNearbyChance = 1.00       # 100% (仅条件判断)
```

累积: 0.90 × 0.65 × 0.70 × 0.40 × 0.50 × 1.00 ≈ 0.082 → 约 1 次/分钟。

## 构建

```bash
# lib/ 目录下需放置 MineZero + SophisticatedBackpacks 的 JAR
./gradlew jar
```

## 许可

Apache License 2.0
