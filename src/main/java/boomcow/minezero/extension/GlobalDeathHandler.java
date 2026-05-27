package boomcow.minezero.extension;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 全局死亡触发 — 任意玩家死亡都触发死亡回归。
 * <p>
 * 由 gamerule {@code globalDeathTrigger} 控制，默认关闭。
 * 当开启后，非锚玩家死亡也会被 MineZero 回归（前提是检查点中有该玩家的存档数据）。
 * </p>
 * <p>与 {@code DeathEventHandler} 同优先级（HIGHEST），
 * 锚玩家仍由 MineZero 原生处理，我们只处理非锚玩家。</p>
 */
public class GlobalDeathHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ExtensionGameRules.isGlobalDeathTriggerEnabled(player.serverLevel())) return;

        var data = CheckpointData.get(player.serverLevel());
        // 锚玩家交给 MineZero 原生 handler
        if (data.getAnchorPlayerUUID() != null && player.getUUID().equals(data.getAnchorPlayerUUID())) return;

        // 非锚玩家：有存档才触发回归
        if (data.getPlayerData(player.getUUID(), player.serverLevel().registryAccess()) == null) return;

        event.setCanceled(true);
        ServerPlayer sp = player;
        player.getServer().execute(() -> CheckpointManager.restoreCheckpoint(sp));
    }
}
