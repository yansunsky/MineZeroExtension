package boomcow.minezero.extension;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * MineZero Extension 配置文件。
 * <p>管理安全条件触发检查点的所有可配置参数。</p>
 */
public class ModConfigs {
    public static final ModConfigSpec COMMON_CONFIG_SPEC;
    static final SafeCheckpointConfig SAFE_CHECKPOINT;

    static {
        var pair = new ModConfigSpec.Builder().configure(SafeCheckpointConfig::new);
        COMMON_CONFIG_SPEC = pair.getRight();
        SAFE_CHECKPOINT = pair.getLeft();
    }

    public static class SafeCheckpointConfig {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.IntValue checkIntervalTicks;
        public final ModConfigSpec.IntValue enemySearchRadius;

        public final ModConfigSpec.DoubleValue overworldChance;
        public final ModConfigSpec.DoubleValue daytimeChance;
        public final ModConfigSpec.DoubleValue healthFullChance;
        public final ModConfigSpec.DoubleValue hungerFullChance;
        public final ModConfigSpec.DoubleValue noNegativeEffectsChance;
        public final ModConfigSpec.DoubleValue noHostileNearbyChance;

        SafeCheckpointConfig(ModConfigSpec.Builder builder) {
            builder.comment("Safe checkpoint trigger settings").push("safeCheckpoint");

            enabled = builder
                    .comment("Master switch for safe checkpoint trigger. Default: true")
                    .define("enabled", true);

            checkIntervalTicks = builder
                    .comment("How often (in ticks) to check safe conditions. Default: 100 (5 seconds)")
                    .defineInRange("checkIntervalTicks", 100, 20, 1200);

            enemySearchRadius = builder
                    .comment("Radius (in blocks) to search for hostile mobs. Default: 24")
                    .defineInRange("enemySearchRadius", 24, 4, 128);

            builder.comment("Probability gates for each condition (0.0 to 1.0).")
                   .comment("Each condition check, if the condition IS true, rolls this probability.")
                   .comment("Cumulative default target: ~1 save/minute when all conditions met.")
                   .push("chances");

            overworldChance = builder
                    .comment("Probability gate after passing overworld check. Default: 0.90")
                    .defineInRange("overworldChance", 0.90, 0.0, 1.0);

            daytimeChance = builder
                    .comment("Probability gate after passing daytime check. Default: 0.65")
                    .defineInRange("daytimeChance", 0.65, 0.0, 1.0);

            healthFullChance = builder
                    .comment("Probability gate after passing health-full check. Default: 0.70")
                    .defineInRange("healthFullChance", 0.70, 0.0, 1.0);

            hungerFullChance = builder
                    .comment("Probability gate after passing hunger-full check. Default: 0.40")
                    .defineInRange("hungerFullChance", 0.40, 0.0, 1.0);

            noNegativeEffectsChance = builder
                    .comment("Probability gate after passing no-negative-effects check. Default: 0.50")
                    .defineInRange("noNegativeEffectsChance", 0.50, 0.0, 1.0);

            noHostileNearbyChance = builder
                    .comment("Probability gate after passing no-hostile-nearby check. Default: 1.00")
                    .defineInRange("noHostileNearbyChance", 1.00, 0.0, 1.0);

            builder.pop();
            builder.pop();
        }
    }
}
