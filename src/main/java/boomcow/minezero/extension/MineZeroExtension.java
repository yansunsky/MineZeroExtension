package boomcow.minezero.extension;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

/**
 * MineZero Extension — 为 MineZero 提供扩展功能的附属模组。
 * <p>功能：SBP 兼容层 | 安全条件触发检查点 | /minezeroextension debugmode 命令</p>
 */
@Mod("minezero_extension")
public class MineZeroExtension {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MineZeroExtension(IEventBus modEventBus, ModContainer modContainer) {
        var dummy = ExtensionGameRules.SAFE_CHECKPOINT_ENABLED;

        LOGGER.info("[MineZeroExtension] Initializing...");
        LOGGER.info("[MineZeroExtension] Gamerule: /gamerule safeCheckpointEnabled");
        LOGGER.info("[MineZeroExtension] Debug: /minezeroextension debugmode true|false");

        NeoForge.EVENT_BUS.register(SafeCheckpointTicker.class);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfigs.COMMON_CONFIG_SPEC);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("minezeroextension")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("debugmode")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean val = BoolArgumentType.getBool(ctx, "enabled");
                                            SafeCheckpointTicker.debugMode = val;
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Debug mode " + (val ? "enabled" : "disabled")),
                                                    true);
                                            return 1;
                                        }))
                        )
        );
    }
}
