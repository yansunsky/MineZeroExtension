package boomcow.minezero.extension;

import boomcow.minezero.checkpoint.CheckpointManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

/**
 * 安全条件触发检查点 — 级联概率系统。
 * <p>
 * 每隔 {@code checkIntervalTicks} 对所有在线玩家执行安全检查。
 * 每条条件通过后进行一次概率掷骰，全部通过才触发 setCheckpoint。
 * </p>
 * <p>条件链：主世界 → 白天 → 满血 → 满饥饿 → 无负面效果 → 无敌对生物</p>
 * <p>级联概率默认值（全满足时约 1次/分钟）：
 * 0.90 × 0.65 × 0.70 × 0.40 × 0.50 × 1.00 ≈ 0.082</p>
 */
public class SafeCheckpointTicker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int tickCounter = 0;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        var config = ModConfigs.SAFE_CHECKPOINT;
        if (!config.enabled.get()) return;
        if (++tickCounter < config.checkIntervalTicks.get()) return;
        tickCounter = 0;

        ServerLevel level = event.getServer().overworld();
        if (level == null) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (tryTrigger(player, config)) break; // 每轮只触发一个玩家
        }
    }

    /**
     * 对单个玩家执行级联概率检查。
     * @return true 如果触发了 setCheckpoint
     */
    private static boolean tryTrigger(ServerPlayer player, ModConfigs.SafeCheckpointConfig c) {
        Level level = player.level();
        int searchRadius = c.enemySearchRadius.get();

        // P1: 位于主世界？
        if (level.dimension() != Level.OVERWORLD) return false;
        if (!roll(c.overworldChance.get())) return false;

        // P2: 白天？
        if (!level.isDay()) return false;
        if (!roll(c.daytimeChance.get())) return false;

        // P3: 满血？
        if (player.getHealth() < player.getMaxHealth()) return false;
        if (!roll(c.healthFullChance.get())) return false;

        // P4: 满饥饿值？
        if (player.getFoodData().getFoodLevel() < 20) return false;
        if (!roll(c.hungerFullChance.get())) return false;

        // P5: 无负面效果？
        if (hasNegativeEffect(player)) return false;
        if (!roll(c.noNegativeEffectsChance.get())) return false;

        // P6: 无敌对生物？（100% 概率门——仅条件判断，最贵操作）
        if (hasNearbyHostile(player, searchRadius)) return false;
        if (!roll(c.noHostileNearbyChance.get())) return false;

        // 全部通过！
        LOGGER.info("[MExt Safe] All conditions passed, triggering checkpoint for {}",
                player.getName().getString());
        CheckpointManager.setCheckpoint(player);
        return true;
    }

    /** 概率掷骰 */
    private static boolean roll(double chance) {
        return random.nextDouble() < chance;
    }

    /** 玩家是否有负面药水效果 */
    private static boolean hasNegativeEffect(ServerPlayer player) {
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (!effect.getEffect().value().isBeneficial()) {
                return true;
            }
        }
        return false;
    }

    /** 玩家搜索半径内是否有敌对生物 */
    private static boolean hasNearbyHostile(ServerPlayer player, int radius) {
        var bb = player.getBoundingBox().inflate(radius);
        List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, bb, m -> m.isAlive());
        return !hostiles.isEmpty();
    }
}
