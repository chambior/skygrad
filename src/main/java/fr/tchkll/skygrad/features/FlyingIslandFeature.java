package fr.tchkll.skygrad.features;

import com.mojang.serialization.Codec;
import fr.tchkll.skygrad.Config;
import fr.tchkll.skygrad.Skygrad;
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

import java.util.ArrayList;
import java.util.List;

public class FlyingIslandFeature extends Feature<NoneFeatureConfiguration> {

    public FlyingIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level  = ctx.level();
        RandomSource  random = ctx.random();
        long          seed   = level.getSeed();

        ImprovedNoise noise1 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xDEADBEEFL));
        ImprovedNoise noise2 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xCAFEBABEL));
        ImprovedNoise noise3 = new ImprovedNoise(new LegacyRandomSource(seed ^ 0xDEDEBABAL));

        BlockPos origin = ctx.origin();
        int baseX = (origin.getX() >> 4) << 4;
        int baseZ = (origin.getZ() >> 4) << 4;

        List<BlockPos> surfacePositions = new ArrayList<>();

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = baseX + dx;
                int z = baseZ + dz;

                double hm1 = noise1.noise(x * (1.0 / Config.NOISE_SCALE1.get()), 0, z * (1.0 / Config.NOISE_SCALE1.get()));

                if (hm1 <= Config.HM1_THRESHOLD.get()) continue;

                double yMax1 = Config.CENTER_Y.get() + (hm1 - Config.HM1_THRESHOLD.get()) * Config.HM1_MAX.get() + 1;
                double yMin1 = Config.CENTER_Y.get() - (hm1 - Config.HM1_THRESHOLD.get()) * Config.HM1_MAX.get();

                double factor1 = Math.clamp((hm1 - Config.HM1_THRESHOLD.get()) / (1 - Config.FACTOR_BUFF.get() - Config.HM1_THRESHOLD.get()), 0, 1);

                double hm2 = noise2.noise(x * (1.0 / Config.NOISE_SCALE2.get()), 0, z * (1.0 / Config.NOISE_SCALE2.get()));

                double yMax2 = factor1 *  (hm2 + 1) * Config.HM2_MAX.get();
                double yMin2 = factor1 * -(hm2 + 1) * Config.HM2_MAX.get();

                double offset = noise3.noise(x * (1.0 / Config.NOISE_SCALE2.get()), 0, z * (1.0 / Config.NOISE_SCALE2.get()));

                int yMin = (int) Math.round(yMin1 + yMin2 + offset * Config.HM3_FACTOR.get()) + 1;
                int yMax = (int) Math.round(yMax1 + yMax2 + offset * Config.HM3_FACTOR.get());

                for (int y = yMin; y <= yMax; y++) {
                    BlockState block;
                    if      (y == yMax)    block = Blocks.GRASS_BLOCK.defaultBlockState();
                    else if (y >= yMax - 3) block = Blocks.DIRT.defaultBlockState();
                    else                   block = Blocks.STONE.defaultBlockState();
                    level.setBlock(new BlockPos(x, y, z), block, 2);
                }

                level.setBlock(new BlockPos(x, yMin - 1, z), Blocks.STONE.defaultBlockState(), 2);

                // Bloc au-dessus de la surface = point de départ de la déco
                surfacePositions.add(new BlockPos(x, yMax + 1, z));
            }
        }

        for (BlockPos pos : surfacePositions) {
            decorateSurface(level, pos, random, ctx.chunkGenerator());
        }

        return !surfacePositions.isEmpty();
    }

    // -------------------------------------------------------------------------

    private void decorateSurface(WorldGenLevel level, BlockPos pos, RandomSource random, ChunkGenerator generator) {
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
        if (biome.is(BiomeTags.IS_JUNGLE))   return 0.045f;
        if (biome.is(BiomeTags.IS_FOREST))   return 0.028f;
        if (biome.is(BiomeTags.IS_TAIGA))    return 0.022f;
        if (biome.is(BiomeTags.IS_SAVANNA))  return 0.015f;
        if (biome.is(BiomeTags.IS_BADLANDS)) return 0.009f;
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