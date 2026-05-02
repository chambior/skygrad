package fr.tchkll.skygrad;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public class FlyingIslandFeature extends Feature<NoneFeatureConfiguration> {

    private static final int   CENTER_Y       = 200;
    private static final double HM1_THRESHOLD  = 0.5;
    private static final double FACTOR_BUFF  = 0.4;
    private static final double NOISE_SCALE1   = 1.0 / 96.0; // ~96 blocs entre les îles
    private static final double NOISE_SCALE2   = 1.0 / 16.0; // ~96 blocs entre les îles
    private static final double HM1_MAX = 32;
    private static final double HM2_MAX = 8;
    private static final double HM3_FACTOR  = 8;

    public FlyingIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        long seed = level.getSeed();

        // Perlin noise maps
        ImprovedNoise noise1 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xDEADBEEFL));
        ImprovedNoise noise2 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xCAFEBABEL));
        ImprovedNoise noise3 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xDEDEBABAL));

        // Coordonnées absolues du coin bas-gauche du chunk
        BlockPos origin = ctx.origin();
        int baseX = (origin.getX() >> 4) << 4;
        int baseZ = (origin.getZ() >> 4) << 4;

        System.out.println("[Skygrad] Generating chunk " + baseX/16 + ", " + baseZ/16);

        boolean placed = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = baseX + dx;
                int z = baseZ + dz;

                double hm1 = noise1.noise(x * NOISE_SCALE1, 0, z * NOISE_SCALE1);
                if (hm1 <= HM1_THRESHOLD) continue;

                double yMax1 = CENTER_Y + (hm1 - HM1_THRESHOLD) * HM1_MAX + 1;
                double yMin1 = CENTER_Y - (hm1 - HM1_THRESHOLD) * HM1_MAX;

                double factor1 = Math.clamp((hm1 - HM1_THRESHOLD) / (1 - FACTOR_BUFF - HM1_THRESHOLD), 0, 1);

                double hm2 = noise2.noise(x * NOISE_SCALE2, 0, z * NOISE_SCALE2);

                double yMax2 = factor1 * (hm2 + 1) * HM2_MAX;
                double yMin2 = factor1 * -(hm2 + 1) * HM2_MAX;

                double offset = noise3.noise(x * NOISE_SCALE2, 0, z * NOISE_SCALE2);

                int yMin = (int) Math.round(yMin1 + yMin2 + offset * HM3_FACTOR) + 1;
                int yMax = (int) Math.round(yMax1 + yMax2 + offset * HM3_FACTOR);

                for (int y = yMin; y <= yMax; y++) {
                    BlockState block;

                    if (y == yMax) {
                        block = Blocks.GRASS_BLOCK.defaultBlockState();
                    } else if (y >= yMax - 3) {
                        block = Blocks.DIRT.defaultBlockState();
                    } else {
                        block = Blocks.STONE.defaultBlockState();
                    }

                    level.setBlock(new BlockPos(x, y, z), block, 2);
                }

                level.setBlock(new BlockPos(x, yMin - 1, z), Blocks.STONE.defaultBlockState(), 2);
            }
        }

        return placed;
    }
}