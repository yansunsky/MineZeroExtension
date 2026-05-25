package boomcow.minezero.extension;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.checkpoint.CheckpointManager;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
 * 安全条件触发检查点 — 级联概率系统，由 gamerule safeCheckpointEnabled 控制开关。
 * <p>条件链：主世界 → 白天 → 满血 → 满饥饿 → 无负面效果 → 无敌对生物</p>
 */
public class SafeCheckpointTicker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int tickCounter = 0;
    private static final Random random = new Random();
    private static boolean gameruleSynced = false;

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (level == null) return;

        boolean ourEnabled = level.getGameRules().getBoolean(ExtensionGameRules.SAFE_CHECKPOINT_ENABLED);

        if (ourEnabled) {
            disableMineZeroAuto(level, event.getServer());
        }

        if (!ourEnabled) return;
        if (++tickCounter < ModConfigs.SAFE_CHECKPOINT.checkIntervalTicks.get()) return;
        tickCounter = 0;

        LOGGER.info("[MExt Safe] Checking {} players...",
                event.getServer().getPlayerList().getPlayers().size());

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (tryTrigger(player)) break;
        }
    }

    /** 安全检查点启用时，关闭 MineZero 的 autoCheckpointEnabled */
    private static void disableMineZeroAuto(ServerLevel level, net.minecraft.server.MinecraftServer server) {
        if (gameruleSynced) return;
        var rule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_ENABLED);
        if (rule != null && rule.get()) {
            rule.set(false, server);
            gameruleSynced = true;
            LOGGER.info("[MExt Safe] MineZero autoCheckpointEnabled DISABLED (safe trigger is active)");
        }
    }

    private static boolean tryTrigger(ServerPlayer player) {
        var c = ModConfigs.SAFE_CHECKPOINT;
        String name = player.getName().getString();
        Level level = player.level();

        if (level.dimension() != Level.OVERWORLD) return false;
        if (!roll(c.overworldChance.get())) return false;

        if (!level.isDay()) return false;
        if (!roll(c.daytimeChance.get())) return false;

        if (player.getHealth() < player.getMaxHealth()) return false;
        if (!roll(c.healthFullChance.get())) return false;

        if (player.getFoodData().getFoodLevel() < 20) return false;
        if (!roll(c.hungerFullChance.get())) return false;

        if (hasNegativeEffect(player)) return false;
        if (!roll(c.noNegativeEffectsChance.get())) return false;

        if (hasNearbyHostile(player, c.enemySearchRadius.get())) return false;
        if (!roll(c.noHostileNearbyChance.get())) return false;

        LOGGER.info("[MExt Safe] ALL PASSED! Triggering checkpoint for {}", name);
        for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
            p.displayClientMessage(
                    Component.literal("[MineZero Extension] Safe checkpoint triggered for " + name)
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    false);
        }
        CheckpointManager.setCheckpoint(player);
        return true;
    }

    private static boolean roll(double chance) {
        return random.nextDouble() < chance;
    }

    private static boolean hasNegativeEffect(ServerPlayer player) {
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (!effect.getEffect().value().isBeneficial()) return true;
        }
        return false;
    }

    private static boolean hasNearbyHostile(ServerPlayer player, int radius) {
        var bb = player.getBoundingBox().inflate(radius);
        List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, bb, m -> m.isAlive());
        return !hostiles.isEmpty();
    }
}
