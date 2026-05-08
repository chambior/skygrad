package fr.tchkll.skygrad.structure;

import fr.tchkll.skygrad.Config;
import fr.tchkll.skygrad.ModBlocks;
import fr.tchkll.skygrad.ModLootTables;
import fr.tchkll.skygrad.ModStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;

import fr.tchkll.skygrad.utils.algo.Pixel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlyingDungeonPiece extends StructurePiece {

    private final BlockPos center;

    public FlyingDungeonPiece(BlockPos center) {
        super(ModStructurePieceTypes.FLYING_DUNGEON_PIECE.get(), 0, makeBbox(center));
        this.center = center;
    }

    @SuppressWarnings("unused")
    public FlyingDungeonPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructurePieceTypes.FLYING_DUNGEON_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
    }

    private static int BBOX_RADIUS() { return Config.CASTLE_SIZE.get() / 2 + 4; }
    private static int MAIN_TOWER_HEIGHT() { return Config.CASTLE_TOWER_HEIGHT.get() * 2; }

    @Override @ParametersAreNonnullByDefault
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
    }

    private static BoundingBox makeBbox(BlockPos c) {
        int r = BBOX_RADIUS();
        return new BoundingBox(
                c.getX() - r, c.getY() - Config.CASTLE_ISLAND_DEPTH.get() - 3, c.getZ() - r,
                c.getX() + r, c.getY() + MAIN_TOWER_HEIGHT() + 2, c.getZ() + r);
    }

    // Deterministic random seeded from center so every chunk call produces the same towers.
    private List<Pixel> getTowers() {
        long seed = (long) center.getX() * 341873128712L + (long) center.getZ() * 132897987541L;
        return generateTowers(Config.CASTLE_SIZE.get(), RandomSource.create(seed));
    }

    // Mirrors generate_towers(size) from the Python POC.
    public static List<Pixel> generateTowers(int size, RandomSource rng) {
        int numTowers = Config.CASTLE_MINIMUM_TOWER_COUNT.get() +
                rng.nextInt(Config.CASTLE_MAXIMUM_TOWER_COUNT.get() - Config.CASTLE_MINIMUM_TOWER_COUNT.get() + 1);

        List<Pixel> towers = new ArrayList<>();
        for (int t = 0; t < numTowers; t++) {
            int minAngle = (int) (360.0 * (t + 0.25) / numTowers);
            int maxAngle = (int) (360.0 * (t + 0.75) / numTowers);
            int minR = size / 4;
            int maxR = size / 3;
            int r = minR + rng.nextInt(maxR - minR + 1);
            int omega = minAngle + rng.nextInt(maxAngle - minAngle + 1);
            towers.add(new Pixel(
                    (int) (r * Math.cos(Math.toRadians(omega))),
                    (int) (r * Math.sin(Math.toRadians(omega)))));
        }
        return towers;
    }

    // Mirrors fill_polygon(points) from the Python POC (scanline fill).
    private static List<Pixel> fillPolygon(List<Pixel> pts) {
        List<Pixel> pixels = new ArrayList<>();
        if (pts.size() < 3) return pixels;

        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (Pixel p : pts) {
            if (p.z() < minZ) minZ = p.z();
            if (p.z() > maxZ) maxZ = p.z();
        }

        for (int z = minZ; z <= maxZ; z++) {
            List<Double> xs = new ArrayList<>();
            for (int i = 0; i < pts.size(); i++) {
                Pixel a = pts.get(i), b = pts.get((i + 1) % pts.size());
                if (a.z() == b.z()) continue;
                if (z < Math.min(a.z(), b.z()) || z >= Math.max(a.z(), b.z())) continue;
                xs.add(a.x() + (double) (z - a.z()) * (b.x() - a.x()) / (b.z() - a.z()));
            }
            xs.sort(Double::compareTo);
            for (int i = 0; i + 1 < xs.size(); i += 2) {
                int xStart = (int) Math.ceil(xs.get(i));
                int xEnd   = (int) Math.floor(xs.get(i + 1));
                for (int x = xStart; x <= xEnd; x++)
                    pixels.add(new Pixel(x, z));
            }
        }
        return pixels;
    }

    /// Draws a line from x1;z1 to x2;z2, returns the corresponding pixel list
    private static List<Pixel> drawLine(int x1, int z1, int x2, int z2) {
        List<Pixel> pts = new ArrayList<>();
        int dx = x2 - x1, dz = z2 - z1;
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) { pts.add(new Pixel(x1, z1)); return pts; }
        double xi = (double) dx / steps, zi = (double) dz / steps;
        double x = x1, z = z1;
        for (int s = 0; s <= steps; s++) {
            pts.add(new Pixel((int) Math.round(x), (int) Math.round(z)));
            x += xi; z += zi;
        }
        return pts;
    }

    /// Returns a list of list of pixels
    /// corresponding to one big tower layer different materials
    private static List<List<Pixel>> bigTowerLayer()
    {
        var blocks = new ArrayList<List<Pixel>>();

        blocks.add(Arrays.asList(
            new Pixel(-4,-1), new Pixel(-4,+1), new Pixel(-3,-2), new Pixel(-3,+2),
            new Pixel(-1,-4), new Pixel(+1,-4), new Pixel(-2,-3), new Pixel(+2,-3),
            new Pixel(+4,-1), new Pixel(+4,+1), new Pixel(+3,-2), new Pixel(+3,+2),
            new Pixel(-1,+4), new Pixel(+1,+4), new Pixel(-2,+3), new Pixel(+2,+3)
        )); // Stonebrick

        blocks.add(Arrays.asList(
            new Pixel(+4,0), new Pixel(-4,0), new Pixel(0,+4), new Pixel(0,-4),
            new Pixel(+3,+3), new Pixel(+3,-3), new Pixel(-3,+3), new Pixel(-3,-3)
        )); // Deepslate tiles

        return blocks;
    }


    private static List<List<Pixel>> smallTowerLayer()
    {
        var blocks = new ArrayList<List<Pixel>>();

        blocks.add(Arrays.asList(
                new Pixel(-2,-1), new Pixel(-2,+1), new Pixel(+2,-1), new Pixel(+2,+1),
                new Pixel(-1,-2), new Pixel(+1,-2), new Pixel(-1,+2), new Pixel(+1,+2),
                new Pixel(-1,-1), new Pixel(-1,+0), new Pixel(-1,+1), new Pixel(+0,-1),
                new Pixel(+0,+0), new Pixel(+0,+1), new Pixel(+1,-1), new Pixel(+1,+0),
                new Pixel(+1,+1)
        )); // Stonebrick

        blocks.add(Arrays.asList(
                new Pixel(+2, 0), new Pixel(-2, 0), new Pixel(0, +2), new Pixel(0, -2)
        )); // Deepslate tiles

        return blocks;
    }

    /**
     * Generates a flying island beneath the castle floor (cy - 1 downward).
     * Shape: the tower polygon is scaled per layer so the island silhouette
     * mirrors the castle's star outline and tapers to a point at the bottom.
     * An inner/outer polygon pair produces ragged edges; mixed block palette
     * gives a natural rocky look.  Fully deterministic — own seed derived from
     * center position so every chunk's postProcess call agrees.
     */
    private void generateIsland(WorldGenLevel level, BoundingBox box,
                                int cx, int cy, int cz, List<Pixel> towers) {
        long seed = (long) cx * 341873128712L + (long) cz * 132897987541L + 7919L;
        RandomSource rng = RandomSource.create(seed);

        for (int dy = 1; dy <= Config.CASTLE_ISLAND_DEPTH.get(); dy++) {
            double t = (double) dy / Config.CASTLE_ISLAND_DEPTH.get();

            double scale = Config.CASTLE_ISLAND_SIZE.get() * Math.pow(1.0 - t, 1.25);
            if (scale < 0.1) break;

            List<Pixel> outerFill = fillPolygon(scaledPolygon(towers, scale));
            Set<Pixel> innerSet  = new HashSet<>(fillPolygon(scaledPolygon(towers, scale * 0.82)));

            double blockProbability = (double) (100 - Config.CASTLE_ISLAND_DECAY.get()) / 100.0f;

            for (Pixel p : outerFill) {
                if (!innerSet.contains(p) && rng.nextFloat() > blockProbability) continue;
                BlockPos pos = new BlockPos(cx + p.x(), cy - dy, cz + p.z());
                if (box.isInside(pos)) level.setBlock(pos, islandBlock(dy, rng), 2);
            }
        }
    }

    /** Scales every vertex of {@code src} by {@code scale}, rounding to nearest block. */
    private static List<Pixel> scaledPolygon(List<Pixel> src, double scale) {
        List<Pixel> out = new ArrayList<>(src.size());
        for (Pixel p : src)
            out.add(new Pixel((int) Math.round(p.x() * scale),
                              (int) Math.round(p.z() * scale)));
        return out;
    }

    /**
     * Picks an island block for depth {@code dy}.
     * Consumes exactly 0 rng calls for dy ≤ 2, exactly 1 for dy > 2,
     * so callers can reason about rng consumption.
     */
    private static BlockState islandBlock(int dy, RandomSource rng) {
        if (dy <= 2) return Blocks.STONE_BRICKS.defaultBlockState();
        int roll = rng.nextInt(12);
        if (roll == 0)      return Blocks.GRAVEL.defaultBlockState();
        if (roll <= 2)      return Blocks.COBBLESTONE.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    @Override @ParametersAreNonnullByDefault
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        List<Pixel> towers = getTowers();

        // — Flying island beneath the castle —
        generateIsland(level, box, cx, cy, cz, towers);

        // — Floor: polygon interior with stone bricks —
        for (Pixel p : fillPolygon(towers)) {
            BlockPos pos = new BlockPos(cx + p.x(), cy, cz + p.z());
            if (box.isInside(pos))
                level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }

        // — Walls between towers —
        Pixel prev = towers.getLast();
        for (Pixel t : towers) {
            double wallAngle = Math.toDegrees(Math.atan2(t.z() - prev.z(), t.x() - prev.x()));

            // Inner-face offset direction (90° clockwise from wall direction),
            int offX, offZ;
            if (wallAngle >= 45 && wallAngle <= 135) {
                offX = 1;  offZ = 0;  // east
            } else if (wallAngle > 135 || wallAngle < -135) {
                offX = 0;  offZ = 1;  // south
            } else if (wallAngle >= -135 && wallAngle <= -45) {
                offX = -1; offZ = 0;  // west
            } else {
                offX = 0;  offZ = -1; // north
            }

            for (Pixel wp : drawLine(prev.x(), prev.z(), t.x(), t.z())) {
                for (int dy = 0; dy <= Config.CASTLE_WALL_HEIGHT.get(); dy++) {
                    BlockPos wallPos = new BlockPos(cx + wp.x(), cy + dy, cz + wp.z());
                    if (box.isInside(wallPos)) level.setBlock(wallPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }

                for (int dy = 0; dy <= Config.CASTLE_WALL_HEIGHT.get() + 1; dy++) {
                    BlockPos innerPos = new BlockPos(cx + wp.x() + offX, cy + dy, cz + wp.z() + offZ);
                    if (box.isInside(innerPos)) level.setBlock(innerPos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
            prev = t;
        }

        var smallTowerLayer = smallTowerLayer();

        // Load small tower cap template once, place on every tower
        StructureTemplate smallTowerTop = new StructureTemplate();
        try (InputStream is = FlyingDungeonPiece.class.getResourceAsStream("/data/skygrad/structures/castle_small_tower_top.nbt")) {
            if (is == null) System.out.println("[Skygrad] castle_small_tower_top.nbt not found on classpath");
            else {
                CompoundTag nbt = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                smallTowerTop.load(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), nbt);
            }
        } catch (IOException e) {
            System.out.println("[Skygrad] Failed to load castle_small_tower_top.nbt: " + e);
            return;
        }

        // — Tower columns + cap —
        for (Pixel t : towers) {
            for(int y = 0; y <= Config.CASTLE_TOWER_HEIGHT.get() + 1; y++) {
                for (Pixel stonebrick : smallTowerLayer.get(0)) {
                    BlockPos pos = new BlockPos(cx + stonebrick.x() + t.x(), cy + y, cz + stonebrick.z() + t.z());
                    if (box.isInside(pos)) level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }

                for (Pixel stonebrick : smallTowerLayer.get(1)) {
                    BlockPos pos = new BlockPos(cx + stonebrick.x() + t.x(), cy + y, cz + stonebrick.z() + t.z());
                    if (box.isInside(pos)) level.setBlock(pos, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }

            Vec3i size = smallTowerTop.getSize();
            BlockPos origin = new BlockPos(cx + t.x() - size.getX() / 2, cy + Config.CASTLE_TOWER_HEIGHT.get() + 1, cz + t.z() - size.getZ() / 2);
            smallTowerTop.placeInWorld(level, origin, origin,
                    new StructurePlaceSettings().setBoundingBox(box), random, 2);
        }

        BlockPos heartPos = new BlockPos(cx, cy + 1, cz);
        if (box.isInside(heartPos)) level.setBlock(heartPos, ModBlocks.ISLAND_HEART_BLOCK.get().defaultBlockState(), 2);

        // Loot chest sitting directly on top of the heart, inside the central tower shaft.
        // createChest is the vanilla helper that places a CHEST, sets its block-entity loot
        // table, picks a sensible facing, and respects the box guard for chunk-by-chunk gen.
        BlockPos chestPos = new BlockPos(cx, cy + 2, cz);
        this.createChest(level, box, random, chestPos, ModLootTables.FLYING_CASTLE_CHEST, null);

        var towerLayer = bigTowerLayer();

        for(int y = 1; y <= MAIN_TOWER_HEIGHT() + 1; y++)
        {
            for(Pixel stonebrick: towerLayer.get(0))
            {
                BlockPos pos = new BlockPos(cx + stonebrick.x(), cy + y, cz + stonebrick.z());
                if(box.isInside(pos)) level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }

            for(Pixel stonebrick: towerLayer.get(1))
            {
                BlockPos pos = new BlockPos(cx + stonebrick.x(), cy + y, cz + stonebrick.z());
                if(box.isInside(pos)) level.setBlock(pos, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
            }
        }

        // Island tower cap: load .nbt directly from the mod classpath,
        // bypassing StructureTemplateManager which doesn't see mod datapacks at gen time.
        try (InputStream is = FlyingDungeonPiece.class.getResourceAsStream("/data/skygrad/structures/castle_main_tower_top.nbt")) {
            if (is == null) System.out.println("[Skygrad] castle_main_tower_top.nbt not found on classpath");
            else {
                StructureTemplate template = new StructureTemplate();
                CompoundTag nbt = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                template.load(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), nbt);

                Vec3i size = template.getSize();
                BlockPos origin = new BlockPos(cx - size.getX() / 2, cy + MAIN_TOWER_HEIGHT() + 2, cz - size.getZ() / 2);
                template.placeInWorld(level, origin, origin,
                        new StructurePlaceSettings().setBoundingBox(box), random, 2);
            }
        } catch (IOException e) {
            System.out.println("[Skygrad] Failed to load castle_main_tower_top.nbt: " + e);
        }
    }
}
