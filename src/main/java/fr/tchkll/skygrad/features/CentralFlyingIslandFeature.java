package fr.tchkll.skygrad.features;

import com.mojang.serialization.Codec;
import fr.tchkll.skygrad.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public class CentralFlyingIslandFeature extends Feature<NoneFeatureConfiguration> {

    public CentralFlyingIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private double gauss(int x, double s) {
        return Math.exp(- x * x / 57800.0 / s);
    }

    private double gauss(int x, int z, double s) {
        return gauss(x, s) * gauss(z, s);
    }

    private double hm1Modifier(double x) {
        double k = 0.04;
        double w = 300;

        return 1.0 / (1 + Math.exp(-(k * (x + w)))) / (1 + Math.exp(k * (x - w)));
    }

    private double hm1Modifier(int x, int z) {
        return hm1Modifier(Math.sqrt(x * x + z * z));
    }

    private static class ComputeResult
    {
        boolean nothing = false;
        int yMin;
        int yMax;
        double factor1;

        public ComputeResult()
        {
            this.nothing = true;
        }

        public ComputeResult(int yMin, int yMax, double factor1)
        {
            this.yMin = yMin;
            this.yMax = yMax;
            this.factor1 = factor1;
        }
    }

    private ComputeResult compute(int x, int z, ImprovedNoise noise1,
                                  ImprovedNoise noise2, ImprovedNoise noise3,
                                  ImprovedNoise noise4, RiverNoise rivers)
    {

        double g_noise = gauss(x, z, 1);
        double g_amplitude = gauss(x, z, 3);

        double g_noise_reducer_1 = Math.clamp(g_noise * 10, 1, 100);
        double g_noise_reducer_2 = Math.clamp(g_noise * 2, 1, 100);

        double g_amplitude_reducer_1 = Math.clamp((1 - g_amplitude), 0, 1);
        double g_amplitude_reducer_2 = Math.clamp((1 - g_amplitude), 0, 1);

        double hm1 = g_noise + noise1.noise(x * (1.0 / (Config.ISLAND_NOISE_SCALE1.get() + g_noise_reducer_1)), 0,
                z * (1.0 / (Config.ISLAND_NOISE_SCALE1.get() + g_noise_reducer_1)));

        var hm1mod = hm1Modifier(x,z);
        hm1 *= hm1mod;

        if (hm1 <= Config.ISLAND_HM1_THRESHOLD.get()) return new ComputeResult();

        double yMax1 = Config.ISLAND_CENTER_Y.get() + (hm1 - Config.ISLAND_HM1_THRESHOLD.get()) * Config.ISLAND_HM1_MAX.get() + 1;
        double yMin1 = Config.ISLAND_CENTER_Y.get() - (hm1 - Config.ISLAND_HM1_THRESHOLD.get()) * Config.ISLAND_HM1_MAX.get();

        double factor1 = Math.clamp((hm1 - Config.ISLAND_HM1_THRESHOLD.get())
                / (1 - Config.ISLAND_FACTOR_BUFF.get() - Config.ISLAND_HM1_THRESHOLD.get()), 0, 1);

        double hm2 = noise2.noise(x * (1.0 / (Config.ISLAND_NOISE_SCALE2.get() * g_noise_reducer_2)), 0,
                z * (1.0 / (Config.ISLAND_NOISE_SCALE2.get() * g_noise_reducer_2)));

        double yMax2 = factor1 *  (hm2 + 1) * Config.ISLAND_HM2_MAX.get() * g_amplitude_reducer_2;
        double yMin2 = factor1 * -(hm2 + 1) * Config.ISLAND_HM2_MAX.get() * g_amplitude_reducer_2;

        double offset3 = 0.4 * noise3.noise(x * (1.0 / (Config.ISLAND_NOISE_SCALE2.get() * g_noise_reducer_2)), 0,
                z * (1.0 / (Config.ISLAND_NOISE_SCALE2.get() * g_noise_reducer_2)));
        double offset4 = 0 * noise4.noise(x * (1.0 / (Config.ISLAND_ALTITUDE_NOISE_MULTIPLIER.get() * g_noise_reducer_1)), 0,
                z * (1.0 / (Config.ISLAND_ALTITUDE_NOISE_MULTIPLIER.get() * g_noise_reducer_1)));

        int yMin = (int) Math.round(yMin1 + yMin2 + offset3 * Config.ISLAND_HM3_FACTOR.get() + offset4 * Config.ISLAND_BASE_ALTITUDE_AMPLITUDE.get()) + 1;
        int yMax = (int) Math.round(yMax1 + yMax2 + offset3 * Config.ISLAND_HM3_FACTOR.get() + offset4 * Config.ISLAND_BASE_ALTITUDE_AMPLITUDE.get());

        return new ComputeResult(yMin, yMax, factor1);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        var level  = ctx.level();
        var random = ctx.random();
        var seed   = level.getSeed();

        var noise1 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xDEADBEEFL));
        var noise2 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xCAFEBABEL));
        var noise3 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xDEDEBABAL));
        var noise4 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xBABAFAFAL));
        var rivers = new RiverNoise(seed ^ 0xF1FA1ABBAFED5EEDL);

        BlockPos origin = ctx.origin();
        int baseX = (origin.getX() >> 4) << 4;
        int baseZ = (origin.getZ() >> 4) << 4;

        boolean generated = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = baseX + dx;
                int z = baseZ + dz;

                var cr = compute(x, z, noise1, noise2, noise3, noise4, rivers);

                if(cr.nothing) continue;

                var yMax = cr.yMax;
                var yMin = cr.yMin;
                var factor1 = cr.factor1; // How far from the island border between 0 and 1

                int RIVER_HEIGHT = Config.ISLAND_CENTER_Y.get();

                double river = rivers.riverValue(x, z) * factor1;
                boolean isBorder = factor1 < 0.3;

                yMin -= (int) (river * 15);
                yMax -= (int) (river * 20);

                for (int y = yMin; y <= yMax; y++) {
                    BlockState block;
                    if(yMax < RIVER_HEIGHT + 1 && y >= yMax - 3 && !isBorder) block = Blocks.CLAY.defaultBlockState();
                    else if (y == yMax)             block = Blocks.GRASS_BLOCK.defaultBlockState();
                    else if (y >= yMax - 3)         block = Blocks.DIRT.defaultBlockState();
                    else                            block = Blocks.STONE.defaultBlockState();
                    level.setBlock(new BlockPos(x, y, z), block, 2);
                }

                if(yMax < RIVER_HEIGHT && !isBorder)
                {
                    for(int y = yMax; y < RIVER_HEIGHT; y++)
                    {
                        level.setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), 2);
                    }
                }
                else
                {
                    decorateSurfaceWithLife(level, new BlockPos(x, yMax + 1, z), random, ctx.chunkGenerator());
                }

                level.setBlock(new BlockPos(x, yMin - 1, z), Blocks.STONE.defaultBlockState(), 2);

                generated = true;
            }
        }

        return generated;
    }

    // -------------------------------------------------------------------------

    private void decorateSurfaceWithLife(WorldGenLevel level, BlockPos pos, RandomSource random, ChunkGenerator generator) {
        Holder<Biome> biome = level.getBiome(pos);

        if (random.nextFloat() < treeChance(biome)) {
            placeTree(level, pos, biome, random, generator);
            return;
        }

        if (random.nextFloat() < 0.35f) {
            placePlant(level, pos, biome, random);
        }
    }

    private float treeChance(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_JUNGLE))   return 0.022f;
        if (biome.is(BiomeTags.IS_FOREST))   return 0.014f;
        if (biome.is(BiomeTags.IS_TAIGA))    return 0.011f;
        if (biome.is(BiomeTags.IS_SAVANNA))  return 0.007f;
        if (biome.is(BiomeTags.IS_BADLANDS)) return 0.004f;
        return 0.022f; // plaines / défaut
    }

    private void placeTree(WorldGenLevel level, BlockPos pos,
                           Holder<Biome> biome, RandomSource random,
                           ChunkGenerator generator) {
        ResourceLocation key = pickTreeKey(biome, random);
        Registry<ConfiguredFeature<?, ?>> registry =
                level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);

        registry.getOptional(key).ifPresent(feature ->
                feature.place(level, generator, random, pos)
        );
    }

    private ResourceLocation pickTreeKey(Holder<Biome> biome, RandomSource random) {
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            return ResourceLocation.withDefaultNamespace("jungle_tree");
        }
        if (biome.is(BiomeTags.IS_TAIGA)) {
            return random.nextBoolean()
                    ? ResourceLocation.withDefaultNamespace("spruce")
                    : ResourceLocation.withDefaultNamespace("pine");
        }
        if (biome.is(BiomeTags.IS_SAVANNA)) {
            return ResourceLocation.withDefaultNamespace("acacia");
        }
        if (biome.is(BiomeTags.IS_FOREST)) {
            return random.nextBoolean()
                    ? ResourceLocation.withDefaultNamespace("birch")
                    : ResourceLocation.withDefaultNamespace("oak");
        }
        return ResourceLocation.withDefaultNamespace("oak");
    }

    private void placePlant(WorldGenLevel level, BlockPos pos,
                            Holder<Biome> biome, RandomSource random) {
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            if (random.nextBoolean()) {
                placeDouble(level, pos, Blocks.LARGE_FERN.defaultBlockState());
            } else {
                level.setBlock(pos, Blocks.FERN.defaultBlockState(), 2);
            }
            return;
        }
        if (biome.is(BiomeTags.IS_TAIGA)) {
            BlockState[] opts = {
                    Blocks.FERN.defaultBlockState(),
                    Blocks.SWEET_BERRY_BUSH.defaultBlockState(),
            };
            level.setBlock(pos, opts[random.nextInt(opts.length)], 2);
            return;
        }
        if (biome.is(BiomeTags.IS_SAVANNA)) {
            BlockState[] opts = {
                    Blocks.SHORT_GRASS.defaultBlockState(),
                    Blocks.DEAD_BUSH.defaultBlockState(),
            };
            level.setBlock(pos, opts[random.nextInt(opts.length)], 2);
            return;
        }
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            if (random.nextFloat() < 0.3f)
                level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 2);
            return;
        }

        // Plaines / défaut
        if(random.nextFloat() < 0.98f)
        {
            level.setBlock(pos, Blocks.SHORT_GRASS.defaultBlockState(), 2);
            return;
        }

        BlockState[] opts = {
                Blocks.DANDELION.defaultBlockState(),
                Blocks.POPPY.defaultBlockState(),
                Blocks.CORNFLOWER.defaultBlockState(),
                Blocks.AZURE_BLUET.defaultBlockState(),
                Blocks.OXEYE_DAISY.defaultBlockState(),
        };
        level.setBlock(pos, opts[random.nextInt(opts.length)], 2);
    }

    /** Place une plante double-hauteur (LARGE_FERN, TALL_GRASS, SUNFLOWER...) */
    private void placeDouble(WorldGenLevel level, BlockPos pos, BlockState base) {
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) return;
        level.setBlock(pos,        base.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(pos.above(), base.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 2);
    }
}