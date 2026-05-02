package fr.tchkll.skygrad;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public class FlyingIslandFeature extends Feature<NoneFeatureConfiguration> {

    private static final int   CENTER_Y       = 200;
    private static final float HM1_THRESHOLD  = 200f;
    private static final double NOISE_SCALE   = 1.0 / 96.0; // ~96 blocs entre les îles

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

                float hm1 = toHeightmap(noise1.noise(x * NOISE_SCALE, 0, z * NOISE_SCALE));
                if (hm1 <= HM1_THRESHOLD) continue;

                float height1 = (hm1 - HM1_THRESHOLD + 1) / (255 - HM1_THRESHOLD);

                float hm2 = toHeightmap(noise2.noise(x * NOISE_SCALE, 0, z * NOISE_SCALE));
                float height2 = (int)(hm2 / 16f);

                int yMin = (int) (CENTER_Y - height1 * height2);
                int yMax = (int) (CENTER_Y + height1 * height2);

                for (int y = yMin; y <= yMax; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), 2);
                    placed = true;
                }
            }
        }

        return placed;
    }

    /** Mappe la sortie d'ImprovedNoise (≈[-1,1]) vers [0, 255] */
    private static float toHeightmap(double noise) {
        return (float)((noise + 1.0) * 127.5);
    }
}