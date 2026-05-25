package boomcow.minezero.extension;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * MineZero Extension — 为 MineZero 提供扩展功能的附属模组。
 * <p>当前功能：</p>
 * <ul>
 *   <li>Sophisticated Backpacks 兼容层 — 通过 Mixin 注入，使死亡回归正确回档背包内容</li>
 *   <li>安全条件触发检查点 — 当玩家处于安全状态时，根据级联概率自动创建检查点</li>
 * </ul>
 * <p>依赖：</p>
 * <ul>
 *   <li>minezero (required)</li>
 *   <li>sophisticatedbackpacks (optional) — SBP 兼容层仅在 SBP 加载时生效</li>
 * </ul>
 *
 * @author yansunsky
 */
@Mod("minezero_extension")
public class MineZeroExtension {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MineZeroExtension(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[MineZeroExtension] Initializing...");
        LOGGER.info("[MineZeroExtension] SBP compat mixins active");
        LOGGER.info("[MineZeroExtension] Safe checkpoint trigger active");

        NeoForge.EVENT_BUS.register(SafeCheckpointTicker.class);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfigs.COMMON_CONFIG_SPEC);
    }
}
