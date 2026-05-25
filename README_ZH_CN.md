# MineZero Extension

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.172-orange.svg)](https://neoforged.net/)

MineZero 拓展附属模组。

## 功能

### 1. Sophisticated Backpacks 兼容
MineZero 触发回归后，精妙背包内的物品正确回档。通过 Mixin 注入实现。

### 2. 安全条件触发检查点
当玩家处于安全状态（主世界、白天、满血、满饥饿、无负面效果、无敌对生物）时，通过**级联概率**自动创建检查点。启用后自动关闭 MineZero 原生的 `autoCheckpointEnabled`。

## 依赖

| 模组 | 要求 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.0+ |
| [MineZero](https://github.com/AMPerez04/MineZero) | 1.0+ (required) |
| Sophisticated Backpacks | 3.20+ (optional) |

## 命令

| 命令 | 说明 |
|------|------|
| `/gamerule safeCheckpointEnabled true\|false` | 启用/禁用安全检查点 |
| `/minezeroextension debugmode true\|false` | 开启/关闭触发通知广播 |

## 配置

`config/minezero_extension-common.toml`：

```toml
[safeCheckpoint]
checkIntervalTicks = 800           # 40秒
enemySearchRadius = 24

[safeCheckpoint.chances]
overworldChance = 0.90             # 90%
daytimeChance = 0.75               # 75%
healthFullChance = 0.75            # 75%
hungerFullChance = 0.55            # 55%
noNegativeEffectsChance = 0.60     # 60%
noHostileNearbyChance = 1.00       # 100%
```

累积: 0.90 × 0.75 × 0.75 × 0.55 × 0.60 × 1.00 ≈ 0.167 → 约 4~10 分钟 1 次。

## 构建

```bash
# lib/ 目录下需放置 MineZero + SophisticatedBackpacks 的 JAR
./gradlew jar
```

## 许可

Apache License 2.0
