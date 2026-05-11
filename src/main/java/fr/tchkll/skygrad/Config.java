package fr.tchkll.skygrad;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    // -----------------------------------------------------------------------------------------------------------------
    // FLYING ISLANDS CONFIG
    // -----------------------------------------------------------------------------------------------------------------

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ISLAND_CENTER_Y = BUILDER
            .comment("Flying islands average y coordinate")
            .translation("skygrad.config.flyingIslandCenterY")
            .defineInRange("flyingIslandCenterY", 200, 0, 400);

    public static final ModConfigSpec.DoubleValue ISLAND_VARIATION_Y = BUILDER
            .comment("Flying islands average y coordinate randomness")
            .translation("skygrad.config.flyingIslandVariationY")
            .defineInRange("flyingIslandVariationY", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ISLAND_HM1_THRESHOLD = BUILDER
            .comment("Flying islands coverage")
            .translation("skygrad.config.flyingIslandCoverage")
            .defineInRange("flyingIslandCoverage", 0.5, -1.0, 1.0);

    public static final ModConfigSpec.DoubleValue ISLAND_FACTOR_BUFF = BUILDER
            .comment("Flying islands island flatness")
            .translation("skygrad.config.flyingIslandFlatness")
            .defineInRange("flyingIslandFlatness", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.IntValue ISLAND_NOISE_SCALE1 = BUILDER
            .comment("Flying islands average distance")
            .translation("skygrad.config.flyingIslandAverageDistance")
            .defineInRange("flyingIslandAverageDistance", 96, 0, 1000);

    public static final ModConfigSpec.IntValue ISLAND_NOISE_SCALE2 = BUILDER
            .comment("Flying islands hills steepness")
            .translation("skygrad.config.flyingIslandHillsSteepness")
            .defineInRange("flyingIslandHillsSteepness", 16, 0, 1000);

    public static final ModConfigSpec.IntValue ISLAND_HM1_MAX = BUILDER
            .comment("Flying islands thickness")
            .translation("skygrad.config.flyingIslandThickness")
            .defineInRange("flyingIslandThickness", 32, 0, 1000);

    public static final ModConfigSpec.IntValue ISLAND_HM2_MAX = BUILDER
            .comment("Flying islands hills height")
            .translation("skygrad.config.flyingIslandHillsHeight")
            .defineInRange("flyingIslandHillsHeight", 8, 0, 1000);

    public static final ModConfigSpec.IntValue ISLAND_HM3_FACTOR = BUILDER
            .comment("Flying islands height irregularity")
            .translation("skygrad.config.flyingIslandHeightIrregularity")
            .defineInRange("flyingIslandHeightIrregularity", 8, 0, 1000);

    public static final ModConfigSpec.IntValue ISLAND_ALTITUDE_NOISE_MULTIPLIER = BUILDER
            .comment("Flying islands altitude variation. ")
            .translation("skygrad.config.flyingIslandAltitudeVariation")
            .defineInRange("flyingIslandAltitudeVariation", 256, 0, 1000);

    public static final ModConfigSpec.IntValue ISLAND_BASE_ALTITUDE_AMPLITUDE = BUILDER
            .comment("Flying islands altitude amplitude. The central altitude of each")
            .translation("skygrad.config.flyingIslandAltitudeAmplitude")
            .defineInRange("flyingIslandAltitudeAmplitude", 20, 0, 200);

    // -----------------------------------------------------------------------------------------------------------------
    // CASTLE CONFIG
    // -----------------------------------------------------------------------------------------------------------------

    public static final ModConfigSpec.IntValue CASTLE_SIZE = BUILDER
            .comment("Castle radius")
            .translation("skygrad.config.castleSize")
            .defineInRange("castleSize", 60, 10, 200);

    public static final ModConfigSpec.IntValue CASTLE_WALL_HEIGHT = BUILDER
            .comment("Castle wall height")
            .translation("skygrad.config.castleWallHeight")
            .defineInRange("castleWallHeight", 4, 2, 60);

    public static final ModConfigSpec.IntValue CASTLE_TOWER_HEIGHT = BUILDER
            .comment("Castle towers height")
            .translation("skygrad.config.castleTowerHeight")
            .defineInRange("castleTowerHeight", 10, 2, 60);

    public static final ModConfigSpec.IntValue CASTLE_ISLAND_DEPTH = BUILDER
            .comment("Castle island depth")
            .translation("skygrad.config.castleIslandDepth")
            .defineInRange("castleIslandDepth", 22, 0, 100);

    public static final ModConfigSpec.DoubleValue CASTLE_ISLAND_SIZE = BUILDER
            .comment("Castle island size multiplier")
            .translation("skygrad.config.castleIslandSize")
            .defineInRange("castleIslandSize", 1.3, 1.2, 10.0);

    public static final ModConfigSpec.IntValue CASTLE_MINIMUM_TOWER_COUNT = BUILDER
            .comment("Castle minimum tower count")
            .translation("skygrad.config.castleMinimumTowerCount")
            .defineInRange("castleMinimumTowerCount", 3, 3, 100);

    public static final ModConfigSpec.IntValue CASTLE_MAXIMUM_TOWER_COUNT = BUILDER
            .comment("Castle maximum tower count")
            .translation("skygrad.config.castleMaximumTowerCount")
            .defineInRange("castleMaximumTowerCount", 7, 3, 100);

    public static final ModConfigSpec.IntValue CASTLE_ISLAND_DECAY = BUILDER
            .comment("Castle island decay")
            .translation("skygrad.config.castleIslandDecay")
            .defineInRange("castleIslandDecay", 20, 0, 100);

    // -----------------------------------------------------------------------------------------------------------------
    // FORTRESS CONFIG
    // -----------------------------------------------------------------------------------------------------------------

    public static final ModConfigSpec.IntValue FORTRESS_SIZE = BUILDER
            .comment("Fortress radius")
            .translation("skygrad.config.fortressSize")
            .defineInRange("fortressSize", 120, 10, 200);

    public static final ModConfigSpec.IntValue FORTRESS_WALL_HEIGHT = BUILDER
            .comment("Fortress wall height")
            .translation("skygrad.config.fortressWallHeight")
            .defineInRange("fortressWallHeight", 12, 2, 60);

    public static final ModConfigSpec.IntValue FORTRESS_TOWER_HEIGHT = BUILDER
            .comment("Fortress towers height")
            .translation("skygrad.config.fortressTowerHeight")
            .defineInRange("fortressTowerHeight", 16, 2, 60);

    public static final ModConfigSpec.IntValue FORTRESS_ISLAND_DEPTH = BUILDER
            .comment("Fortress island depth")
            .translation("skygrad.config.fortressIslandDepth")
            .defineInRange("fortressIslandDepth", 32, 0, 100);

    public static final ModConfigSpec.DoubleValue FORTRESS_ISLAND_SIZE = BUILDER
            .comment("Fortress island size multiplier")
            .translation("skygrad.config.fortressIslandSize")
            .defineInRange("fortressIslandSize", 1.3, 1.2, 10.0);

    public static final ModConfigSpec.IntValue FORTRESS_MINIMUM_TOWER_COUNT = BUILDER
            .comment("Fortress minimum tower count")
            .translation("skygrad.config.fortressMinimumTowerCount")
            .defineInRange("fortressMinimumTowerCount", 12, 3, 100);

    public static final ModConfigSpec.IntValue FORTRESS_MAXIMUM_TOWER_COUNT = BUILDER
            .comment("Fortress maximum tower count")
            .translation("skygrad.config.fortressMaximumTowerCount")
            .defineInRange("fortressMaximumTowerCount", 28, 3, 100);

    public static final ModConfigSpec.IntValue FORTRESS_ISLAND_DECAY = BUILDER
            .comment("Fortress island decay")
            .translation("skygrad.config.fortressIslandDecay")
            .defineInRange("fortressIslandDecay", 10, 0, 100);

    static final ModConfigSpec SPEC = BUILDER.build();
}
