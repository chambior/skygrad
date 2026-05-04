package fr.tchkll.skygrad;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue CENTER_Y = BUILDER
            .comment("Flying islands average y coordinate")
            .defineInRange("flyingIslandCenterY", 200.0, 0.0, 400.0);

    public static final ModConfigSpec.DoubleValue VARIATION_Y = BUILDER
            .comment("Flying islands average y coordinate randomness")
            .defineInRange("flyingIslandVariationY", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue HM1_THRESHOLD = BUILDER
            .comment("Flying islands coverage")
            .defineInRange("flyingIslandCoverage", 0.5, -1.0, 1.0);

    public static final ModConfigSpec.DoubleValue FACTOR_BUFF = BUILDER
            .comment("Flying islands island flatness")
            .defineInRange("flyingIslandFlatness", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue NOISE_SCALE1 = BUILDER
            .comment("Flying islands average distance")
            .defineInRange("flyingIslandAverageDistance", 96, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue NOISE_SCALE2 = BUILDER
            .comment("Flying islands hills steepness")
            .defineInRange("flyingIslandHillsSteepness", 16, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue HM1_MAX = BUILDER
            .comment("Flying islands thickness")
            .defineInRange("flyingIslandThickness", 32, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue HM2_MAX = BUILDER
            .comment("Flying islands hills height")
            .defineInRange("flyingIslandHillsHeight", 8, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue HM3_FACTOR = BUILDER
            .comment("Flying islands height irregularity")
            .defineInRange("flyingIslandHeightIrregularity", 8, 0.0, 1000.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
