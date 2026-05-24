# MineZero SBP Compat

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.172-orange.svg)](https://neoforged.net/)

MineZero × Sophisticated Backpacks 兼容模组。让 MineZero 的"死亡回归"机制正确回档精妙背包内的物品。

使用DeepSeekV4编写的项目，尚未进行人工代码审查，也没有进行多人游戏测试。可能存在潜在问题。

## 解决的问题

MineZero 触发回归后，精妙背包（Sophisticated Backpacks）内的物品不会回档。根本原因是 SBP 使用**双层存储模型**：ItemStack 仅存放 UUID 引用，实际内容保存在独立的全局存储中。MineZero 只保存了引用，未保存实际内容。

本模组通过 Mixin 注入，在检查点保存/恢复时自动捕获和恢复背包内容快照。

## 依赖

| 模组 | 版本要求 |
|------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.0+ |
| [MineZero](https://github.com/AMPerez04/MineZero) | 1.0+ |
| [Sophisticated Backpacks](https://github.com/P3pp3rF1y/SophisticatedBackpacks) | 3.20+ |

## 安装

将 `minezero_sbp_compat-1.0.0.jar` 放入 `mods/` 目录，与 MineZero 和 SophisticatedBackpacks 一起加载即可。

## 构建

```bash
# 需要 lib/ 目录下放置 MineZero 和 SophisticatedBackpacks 的 JAR
./gradlew jar
# 产物: build/libs/minezero_sbp_compat-1.0.0.jar
```

## 工作原理

```
setCheckpoint 完成
    ↓ [Mixin TAIL]
captureSnapshot() → 扫描所有在线玩家背包
                 → 从 BackpackStorage 读取每个背包的 UUID 内容
                 → 保存快照

锚点玩家死亡 → restoreCheckpoint 触发
    ↓ [Mixin HEAD]
applySnapshot()  → 将快照写回 BackpackStorage
                 → 恢复玩家背包 ItemStack
                 → 背包内容已回档 ✓
```

## 许可

Apache License 2.0。本模组不包含任何 MineZero 或 Sophisticated Backpacks 的代码，仅通过运行时 API 与它们互操作。
