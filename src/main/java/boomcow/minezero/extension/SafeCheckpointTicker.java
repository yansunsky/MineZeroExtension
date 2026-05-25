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
    /** 上次同步时 MineZero 规则是否被我们禁用 */
    private static boolean minezeroAutoDisabled = false;

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (level == null) return;

        // gamerule 可能尚未注册，必须 null-check
        var ourRule = level.getGameRules().getRule(ExtensionGameRules.SAFE_CHECKPOINT_ENABLED);
        boolean ourEnabled = ourRule != null && ourRule.get();

        var minezeroRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_ENABLED);
        boolean minezeroAutoEnabled = minezeroRule != null && minezeroRule.get();

        // 我们的规则启用 → 禁用 MineZero 原生自动检查点
        // 我们的规则禁用 → 恢复 MineZero 原生自动检查点
        if (ourEnabled && minezeroAutoEnabled && minezeroRule != null) {
            minezeroRule.set(false, event.getServer());
            minezeroAutoDisabled = true;
            LOGGER.info("[MExt Safe] MineZero autoCheckpointEnabled -> DISABLED");
        } else if (!ourEnabled && !minezeroAutoEnabled && minezeroAutoDisabled && minezeroRule != null) {
            minezeroRule.set(true, event.getServer());
            minezeroAutoDisabled = false;
            LOGGER.info("[MExt Safe] MineZero autoCheckpointEnabled -> RESTORED");
        }

        if (!ourEnabled) return;
        if (++tickCounter < ModConfigs.SAFE_CHECKPOINT.checkIntervalTicks.get()) return;
        tickCounter = 0;

        int total = event.getServer().getPlayerList().getPlayers().size();
        LOGGER.info("[MExt Safe] ===== Checking {} player(s) (interval={}t) =====", total,
                ModConfigs.SAFE_CHECKPOINT.checkIntervalTicks.get());

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (tryTrigger(player)) break;
        }
    }

    /** 对单个玩家执行级联概率检查 */
    private static boolean tryTrigger(ServerPlayer player) {
        var c = ModConfigs.SAFE_CHECKPOINT;
        String name = player.getName().getString();
        Level level = player.level();
        int radius = c.enemySearchRadius.get();

        // P1: 主世界
        if (level.dimension() != Level.OVERWORLD) {
            LOGGER.info("[MExt Safe] {} | STEP1 Overworld: FAIL (not in overworld)", name);
            return false;
        }
        LOGGER.info("[MExt Safe] {} | STEP1 Overworld: OK -> roll({}%)", name, pct(c.overworldChance.get()));
        if (!roll(c.overworldChance.get())) { LOGGER.info("[MExt Safe] {} | STEP1 Overworld: roll MISS", name); return false; }
        LOGGER.info("[MExt Safe] {} | STEP1 Overworld: roll HIT", name);

        // P2: 白天
        if (!level.isDay()) {
            LOGGER.info("[MExt Safe] {} | STEP2 Daytime: FAIL (not daytime)", name);
            return false;
        }
        LOGGER.info("[MExt Safe] {} | STEP2 Daytime: OK -> roll({}%)", name, pct(c.daytimeChance.get()));
        if (!roll(c.daytimeChance.get())) { LOGGER.info("[MExt Safe] {} | STEP2 Daytime: roll MISS", name); return false; }
        LOGGER.info("[MExt Safe] {} | STEP2 Daytime: roll HIT", name);

        // P3: 满血
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        if (hp < maxHp) {
            LOGGER.info("[MExt Safe] {} | STEP3 Health: FAIL ({}/{})", name, (int)hp, (int)maxHp);
            return false;
        }
        LOGGER.info("[MExt Safe] {} | STEP3 Health: OK ({}/{}) -> roll({}%)", name, (int)hp, (int)maxHp, pct(c.healthFullChance.get()));
        if (!roll(c.healthFullChance.get())) { LOGGER.info("[MExt Safe] {} | STEP3 Health: roll MISS", name); return false; }
        LOGGER.info("[MExt Safe] {} | STEP3 Health: roll HIT", name);

        // P4: 满饥饿
        int hunger = player.getFoodData().getFoodLevel();
        if (hunger < 20) {
            LOGGER.info("[MExt Safe] {} | STEP4 Hunger: FAIL ({}/20)", name, hunger);
            return false;
        }
        LOGGER.info("[MExt Safe] {} | STEP4 Hunger: OK ({}/20) -> roll({}%)", name, hunger, pct(c.hungerFullChance.get()));
        if (!roll(c.hungerFullChance.get())) { LOGGER.info("[MExt Safe] {} | STEP4 Hunger: roll MISS", name); return false; }
        LOGGER.info("[MExt Safe] {} | STEP4 Hunger: roll HIT", name);

        // P5: 无负面效果
        if (hasNegativeEffect(player)) {
            LOGGER.info("[MExt Safe] {} | STEP5 NoDebuff: FAIL (has negative effect)", name);
            return false;
        }
        LOGGER.info("[MExt Safe] {} | STEP5 NoDebuff: OK -> roll({}%)", name, pct(c.noNegativeEffectsChance.get()));
        if (!roll(c.noNegativeEffectsChance.get())) { LOGGER.info("[MExt Safe] {} | STEP5 NoDebuff: roll MISS", name); return false; }
        LOGGER.info("[MExt Safe] {} | STEP5 NoDebuff: roll HIT", name);

        // P6: 无敌对生物（最贵操作）
        boolean hostile = hasNearbyHostile(player, radius);
        if (hostile) {
            LOGGER.info("[MExt Safe] {} | STEP6 NoHostile: FAIL (hostile in {} blocks)", name, radius);
            return false;
        }
        LOGGER.info("[MExt Safe] {} | STEP6 NoHostile: OK -> roll({}%)", name, pct(c.noHostileNearbyChance.get()));
        if (!roll(c.noHostileNearbyChance.get())) { LOGGER.info("[MExt Safe] {} | STEP6 NoHostile: roll MISS", name); return false; }
        LOGGER.info("[MExt Safe] {} | STEP6 NoHostile: roll HIT", name);

        // ★ 全部通过
        LOGGER.info("[MExt Safe] *** {} ALL PASSED! Checkpoint saved. ***", name);
        for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
            p.displayClientMessage(
                    Component.literal("[MineZero Extension] Safe checkpoint triggered for " + name)
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    false);
        }
        CheckpointManager.setCheckpoint(player);
        return true;
    }

    private static int pct(double d) { return (int)(d * 100); }

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
