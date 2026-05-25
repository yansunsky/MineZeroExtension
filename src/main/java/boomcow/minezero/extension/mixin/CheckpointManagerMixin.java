package boomcow.minezero.extension.mixin;

import boomcow.minezero.checkpoint.CheckpointManager;
import boomcow.minezero.extension.PersistentDataHelper;
import boomcow.minezero.extension.SBPBackpackHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin：钩入 MineZero 的检查点管理器的 setCheckpoint / restoreCheckpoint 方法。
 * <p>
 * 注入策略：
 * <ul>
 *   <li>{@code setCheckpoint(TAIL)} — 在 MineZero 保存完所有玩家和世界数据后，
 *       立即捕获 BackpackStorage 的快照，确保快照与检查点一致。</li>
 *   <li>{@code restoreCheckpoint(HEAD)} — 在 MineZero 开始恢复玩家背包之前，
 *       先将 BackpackStorage 回档到检查点状态，确保后续恢复的背包 ItemStack
 *       （及其 UUID 引用）指向正确的回档内容。</li>
 * </ul>
 *
 * @see SBPBackpackHelper#captureSnapshot(ServerPlayer)
 * @see SBPBackpackHelper#applySnapshot(ServerPlayer)
 */
@Mixin(value = CheckpointManager.class, remap = false)
public abstract class CheckpointManagerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        LOGGER.info("[MineZeroExtension] CheckpointManagerMixin STATIC INIT — Mixin class loaded!");
    }

    /**
     * 在 setCheckpoint 完成后捕获 BackpackStorage 快照。
     * <p>
     * TAIL 注入确保 MineZero 已经完成所有状态保存（玩家、世界、实体等），
     * 此时 BackpackStorage 中的状态与检查点一致。
     */
    @Inject(method = "setCheckpoint", at = @At("TAIL"), remap = false)
    private static void minezeroSbp$onSetCheckpointTail(ServerPlayer anchorPlayer, CallbackInfo ci) {
        SBPBackpackHelper.captureSnapshot(anchorPlayer);
        if (anchorPlayer != null && anchorPlayer.getServer() != null) {
            for (ServerPlayer p : anchorPlayer.getServer().getPlayerList().getPlayers()) {
                PersistentDataHelper.capture(p);
            }
        }
    }

    /**
     * 在 restoreCheckpoint 开始前恢复 BackpackStorage 快照。
     * <p>
     * HEAD 注入必须在任何恢复逻辑之前执行，因为：
     * <ol>
     *   <li>MineZero 会清除玩家背包（playerInventory.clearContent()）</li>
     *   <li>然后从 NBT 恢复 ItemStack（包含 STORAGE_UUID）</li>
     *   <li>如果 BackpackStorage 内容不同步，恢复后的背包将显示错误内容</li>
     * </ol>
     * 因此 BackpackStorage 必须在第 2 步之前恢复到检查点状态。
     */
    @Inject(method = "restoreCheckpoint", at = @At("HEAD"), remap = false)
    private static void minezeroSbp$onRestoreCheckpointHead(ServerPlayer anchorPlayer, CallbackInfo ci) {
        SBPBackpackHelper.applySnapshot(anchorPlayer);
    }

    /** 在 restoreCheckpoint 完成后，应用所有玩家的 ForgeData 快照 */
    @Inject(method = "restoreCheckpoint", at = @At("RETURN"), remap = false)
    private static void minezeroForge$onRestoreCheckpointReturn(ServerPlayer anchorPlayer, CallbackInfo ci) {
        if (anchorPlayer != null && anchorPlayer.getServer() != null) {
            for (ServerPlayer p : anchorPlayer.getServer().getPlayerList().getPlayers()) {
                PersistentDataHelper.apply(p);
            }
        }
    }
}
