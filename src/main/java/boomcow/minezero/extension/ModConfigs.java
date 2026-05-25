package boomcow.minezero.extension;

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
        public final ModConfigSpec.IntValue checkIntervalTicks;
        public final ModConfigSpec.IntValue enemySearchRadius;

        public final ModConfigSpec.DoubleValue overworldChance;
        public final ModConfigSpec.DoubleValue daytimeChance;
        public final ModConfigSpec.DoubleValue healthFullChance;
        public final ModConfigSpec.DoubleValue hungerFullChance;
        public final ModConfigSpec.DoubleValue noNegativeEffectsChance;
        public final ModConfigSpec.DoubleValue noHostileNearbyChance;

        SafeCheckpointConfig(ModConfigSpec.Builder builder) {
            builder.comment("Safe checkpoint trigger settings")
                   .comment("Enable/disable via gamerule: /gamerule safeCheckpointEnabled true|false")
                   .push("safeCheckpoint");

            checkIntervalTicks = builder
                    .comment("How often (in ticks) to check safe conditions. Default: 400 (20 seconds)")
                    .defineInRange("checkIntervalTicks", 400, 20, 1200);

            enemySearchRadius = builder
                    .comment("Radius (in blocks) to search for hostile mobs. Default: 24")
                    .defineInRange("enemySearchRadius", 24, 4, 128);

            builder.comment("Probability gates for each condition (0.0 to 1.0).")
                   .comment("20s interval → 3 checks/min → 6 checks/2min.")
                   .comment("Cumulative target: ~1 save/2min (0.167 per check).")
                   .push("chances");

            overworldChance = builder
                    .comment("Probability gate after passing overworld check. Default: 0.90")
                    .defineInRange("overworldChance", 0.90, 0.0, 1.0);

            daytimeChance = builder
                    .comment("Probability gate after passing daytime check. Default: 0.75")
                    .defineInRange("daytimeChance", 0.75, 0.0, 1.0);

            healthFullChance = builder
                    .comment("Probability gate after passing health-full check. Default: 0.75")
                    .defineInRange("healthFullChance", 0.75, 0.0, 1.0);

            hungerFullChance = builder
                    .comment("Probability gate after passing hunger-full check. Default: 0.55")
                    .defineInRange("hungerFullChance", 0.55, 0.0, 1.0);

            noNegativeEffectsChance = builder
                    .comment("Probability gate after passing no-negative-effects check. Default: 0.60")
                    .defineInRange("noNegativeEffectsChance", 0.60, 0.0, 1.0);

            noHostileNearbyChance = builder
                    .comment("Probability gate after passing no-hostile-nearby check. Default: 1.00")
                    .defineInRange("noHostileNearbyChance", 1.00, 0.0, 1.0);

            builder.pop();
            builder.pop();
        }
    }
}
