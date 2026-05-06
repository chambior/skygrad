package fr.tchkll.skygrad;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    // ISLANDS CONFIG
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ISLAND_CENTER_Y = BUILDER
            .comment("Flying islands average y coordinate")
            .defineInRange("flyingIslandCenterY", 200.0, 0.0, 400.0);

    public static final ModConfigSpec.DoubleValue ISLAND_VARIATION_Y = BUILDER
            .comment("Flying islands average y coordinate randomness")
            .defineInRange("flyingIslandVariationY", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ISLAND_HM1_THRESHOLD = BUILDER
            .comment("Flying islands coverage")
            .defineInRange("flyingIslandCoverage", 0.5, -1.0, 1.0);

    public static final ModConfigSpec.DoubleValue ISLAND_FACTOR_BUFF = BUILDER
            .comment("Flying islands island flatness")
            .defineInRange("flyingIslandFlatness", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ISLAND_NOISE_SCALE1 = BUILDER
            .comment("Flying islands average distance")
            .defineInRange("flyingIslandAverageDistance", 96, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ISLAND_NOISE_SCALE2 = BUILDER
            .comment("Flying islands hills steepness")
            .defineInRange("flyingIslandHillsSteepness", 16, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ISLAND_HM1_MAX = BUILDER
            .comment("Flying islands thickness")
            .defineInRange("flyingIslandThickness", 32, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ISLAND_HM2_MAX = BUILDER
            .comment("Flying islands hills height")
            .defineInRange("flyingIslandHillsHeight", 8, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ISLAND_HM3_FACTOR = BUILDER
            .comment("Flying islands height irregularity")
            .defineInRange("flyingIslandHeightIrregularity", 8, 0.0, 1000.0);


    // CASTLE CONFIG
    public static final ModConfigSpec.IntValue CASTLE_SIZE = BUILDER
            .comment("Castle radius")
            .defineInRange("castleSize", 60, 10, 200);

    public static final ModConfigSpec.IntValue CASTLE_WALL_HEIGHT = BUILDER
            .comment("Castle wall height")
            .defineInRange("castleWallHeight", 4, 2, 60);

    public static final ModConfigSpec.IntValue CASTLE_TOWER_HEIGHT = BUILDER
            .comment("Castle towers height")
            .defineInRange("castleTowerHeight", 10, 2, 60);

    public static final ModConfigSpec.IntValue CASTLE_ISLAND_DEPTH = BUILDER
            .comment("Castle island depth")
            .defineInRange("castleIslandDepth", 22, 0, 100);

    public static final ModConfigSpec.DoubleValue CASTLE_ISLAND_SIZE = BUILDER
            .comment("Castle island size multiplier")
            .defineInRange("castleIslandSize", 1.3, 1.2, 10.0);

    public static final ModConfigSpec.IntValue CASTLE_MINIMUM_TOWER_COUNT = BUILDER
            .comment("Castle minimum tower count")
            .defineInRange("castleMinimumTowerCount", 3, 3, 100);

    public static final ModConfigSpec.IntValue CASTLE_MAXIMUM_TOWER_COUNT = BUILDER
            .comment("Castle maximum tower count")
            .defineInRange("castleMaximumTowerCount", 7, 3, 100);

    public static final ModConfigSpec.IntValue CASTLE_ISLAND_DECAY = BUILDER
            .comment("Castle island decay")
            .defineInRange("castleIslandDecay", 20, 0, 100);

    static final ModConfigSpec SPEC = BUILDER.build();
}
