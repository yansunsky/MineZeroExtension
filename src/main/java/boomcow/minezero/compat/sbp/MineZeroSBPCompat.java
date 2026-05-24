package boomcow.minezero.compat.sbp;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * MineZero × SophisticatedBackpacks 兼容模组入口。
 * <p>
 * 本模组通过 Mixin 注入到 MineZero 的检查点管理器和持久化类中，
 * 在以下关键时机自动处理背包快照：</p>
 * <ul>
 *   <li>检查点保存后 → 捕获 BackpackStorage 快照</li>
 *   <li>回归恢复前 → 将快照写回 BackpackStorage</li>
 *   <li>世界存档/加载 → 持久化/恢复快照</li>
 * </ul>
 * <p>
 * Mixin 会自动应用（不需要手动配置），因为它们在
 * {@code minezero_sbp_compat.mixins.json} 中声明。
 * <p>
 * 依赖：
 * <ul>
 *   <li>minezero (required) — MineZero 模组</li>
 *   <li>sophisticatedbackpacks (required) — 精妙背包模组</li>
 * </ul>
 *
 * @author yansunsky
 */
@Mod("minezero_sbp_compat")
public class MineZeroSBPCompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MineZeroSBPCompat(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[MineZero-SBPCompat] Initialized. Backpack snapshot compatibility active.");
        LOGGER.info("[MineZero-SBPCompat] Mixins will hook into MineZero's checkpoint lifecycle.");
    }
}
