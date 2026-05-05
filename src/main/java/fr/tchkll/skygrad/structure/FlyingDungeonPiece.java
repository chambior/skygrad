package fr.tchkll.skygrad.structure;

import fr.tchkll.skygrad.ModBlocks;
import fr.tchkll.skygrad.ModStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;

import fr.tchkll.skygrad.utils.algo.Pixel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlyingDungeonPiece extends StructurePiece {

    public  static final int SIZE        = 60;
    private static final int WALL_HEIGHT = 4;
    public  static final int TOWER_HEIGHT = 10;
    private static final int BBOX_RADIUS = SIZE / 2 + 4;
    private static final int MAIN_TOWER_HEIGHT = TOWER_HEIGHT * 2;
    /** Vertical headroom added to the bbox to cover both NBT structure caps. */
    private static final int CAP_HEADROOM      = 15;

    private final BlockPos center;

    public FlyingDungeonPiece(BlockPos center) {
        super(ModStructurePieceTypes.FLYING_DUNGEON_PIECE.get(), 0, makeBbox(center));
        this.center = center;
    }

    public FlyingDungeonPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructurePieceTypes.FLYING_DUNGEON_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
    }

    private static BoundingBox makeBbox(BlockPos c) {
        int r = BBOX_RADIUS;
        return new BoundingBox(
                c.getX() - r, c.getY() - 1, c.getZ() - r,
                c.getX() + r, c.getY() + MAIN_TOWER_HEIGHT + 2 + CAP_HEADROOM, c.getZ() + r);
    }

    // Deterministic random seeded from center so every chunk call produces the same towers.
    private List<Pixel> getTowers() {
        long seed = (long) center.getX() * 341873128712L + (long) center.getZ() * 132897987541L;
        return generateTowers(SIZE, RandomSource.create(seed));
    }

    // Mirrors generate_towers(size) from the Python POC.
    public static List<Pixel> generateTowers(int size, RandomSource rng) {
        int numTowers = 3 + rng.nextInt(5); // 3..7
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

    // Mirrors line(x1,y1,x2,y2) from the Python POC.
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

    private static List<List<Pixel>> bigTowerLayer()
    {
        var blocks = new ArrayList<List<Pixel>>();

        blocks.add(Arrays.asList(
            new Pixel(-4,-1),
            new Pixel(-4,+1),
            new Pixel(-3,-2),
            new Pixel(-3,+2),
            new Pixel(-1,-4),
            new Pixel(+1,-4),
            new Pixel(-2,-3),
            new Pixel(+2,-3),
            new Pixel(+4,-1),
            new Pixel(+4,+1),
            new Pixel(+3,-2),
            new Pixel(+3,+2),
            new Pixel(-1,+4),
            new Pixel(+1,+4),
            new Pixel(-2,+3),
            new Pixel(+2,+3)
        )); // Stonebrick

        blocks.add(Arrays.asList(
            new Pixel(+4,0),
            new Pixel(-4,0),
            new Pixel(0,+4),
            new Pixel(0,-4),
            new Pixel(+3,+3),
            new Pixel(+3,-3),
            new Pixel(-3,+3),
            new Pixel(-3,-3)
        )); // Deepslate tiles

        return blocks;
    }


    private static List<List<Pixel>> smallTowerLayer()
    {
        var blocks = new ArrayList<List<Pixel>>();

        blocks.add(Arrays.asList(
                new Pixel(-2,-1),
                new Pixel(-2,+1),
                new Pixel(+2,-1),
                new Pixel(+2,+1),
                new Pixel(-1,-2),
                new Pixel(+1,-2),
                new Pixel(-1,+2),
                new Pixel(+1,+2)
        )); // Stonebrick

        blocks.add(Arrays.asList(
                new Pixel(+2, 0),
                new Pixel(-2, 0),
                new Pixel(0, +2),
                new Pixel(0, -2)
        ));// Deepslate tiles

        return blocks;
    }

//    private static void placeMainTowerTop(WorldGenLevel level, BoundingBox box, int x, int y, int z)
//    {
//        var flags = 2;
//
//        var block = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
//        var slab = Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
//
//        var stair_n = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_s = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_e = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_w = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.BOTTOM);
//
//        var stair_tn = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.TOP);
//        var stair_ts = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.TOP);
//        var stair_te = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.TOP);
//        var stair_tw = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.TOP);
//
//        var stair_1n = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_1s = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_1e = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_1w = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//
//        var stair_3n = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_3s = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_3e = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//        var stair_3w = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT).setValue(StairBlock.HALF, Half.BOTTOM);
//
//        for(int i = -5; i < +5; i++) for(int j = -5; j < +5; j++)
//        {
//            level.setBlock(new BlockPos(x + i, y, z + i), block, flags);
//        }
//
//        for(int i = -2; i < +2; i++)
//        {
//            level.setBlock(new BlockPos(x + i, y + 1, z + 5), block, flags);
//            level.setBlock(new BlockPos(x + i, y + 1, z - 5), block, flags);
//            level.setBlock(new BlockPos(x + 5, y + 1, z + i), block, flags);
//            level.setBlock(new BlockPos(x - 5, y + 1, z + i), block, flags);
//        }
//
//        level.setBlock(new BlockPos(x + 5, y + 2, z), block, flags);
//        level.setBlock(new BlockPos(x + 5, y + 3, z), slab, flags);
//        level.setBlock(new BlockPos(x - 5, y + 2, z), block, flags);
//        level.setBlock(new BlockPos(x - 5, y + 3, z), slab, flags);
//        level.setBlock(new BlockPos(x, y + 2, z + 5), block, flags);
//        level.setBlock(new BlockPos(x, y + 3, z + 5), slab, flags);
//        level.setBlock(new BlockPos(x, y + 2, z - 5), block, flags);
//        level.setBlock(new BlockPos(x, y + 3, z - 5), slab, flags);
//    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        List<Pixel> towers = getTowers();

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
            // mirroring the green-pixel logic from the Python POC.
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
                for (int dy = 0; dy <= WALL_HEIGHT; dy++) {
                    BlockPos wallPos = new BlockPos(cx + wp.x(), cy + dy, cz + wp.z());
                    if (box.isInside(wallPos))
                        level.setBlock(wallPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }

                for (int dy = 0; dy <= WALL_HEIGHT + 1; dy++) {
                    BlockPos innerPos = new BlockPos(cx + wp.x() + offX, cy + dy, cz + wp.z() + offZ);
                    if (box.isInside(innerPos))
                        level.setBlock(innerPos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
            prev = t;
        }

        var smallTowerLayer = smallTowerLayer();

        // Load small tower cap template once, place on every tower
        StructureTemplate smallTowerTop = null;
        try (InputStream is = FlyingDungeonPiece.class.getResourceAsStream(
                "/data/skygrad/structures/castle_small_tower_top.nbt")) {
            if (is == null) {
                System.out.println("[Skygrad] castle_small_tower_top.nbt not found on classpath");
            } else {
                CompoundTag nbt = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                smallTowerTop = new StructureTemplate();
                smallTowerTop.load(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), nbt);
            }
        } catch (IOException e) {
            System.out.println("[Skygrad] Failed to load castle_small_tower_top.nbt: " + e);
        }

        // — Tower columns + cap —
        for (Pixel t : towers) {

            for(int y = 1; y <= TOWER_HEIGHT + 1; y++)
            {
                for(Pixel stonebrick: smallTowerLayer.get(0))
                {
                    BlockPos pos = new BlockPos(cx + stonebrick.x()+ t.x(), cy + y, cz + stonebrick.z() + t.z());

                    if(box.isInside(pos))
                        level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }

                for(Pixel stonebrick: smallTowerLayer.get(1))
                {
                    BlockPos pos = new BlockPos(cx + stonebrick.x()+ t.x(), cy + y, cz + stonebrick.z() + t.z());

                    if(box.isInside(pos))
                        level.setBlock(pos, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }

            if (smallTowerTop != null) {
                Vec3i size = smallTowerTop.getSize();
                BlockPos origin = new BlockPos(
                    cx + t.x() - size.getX() / 2,
                    cy + TOWER_HEIGHT + 1,
                    cz + t.z() - size.getZ() / 2
                );
                smallTowerTop.placeInWorld(level, origin, origin, new StructurePlaceSettings(), random, 2);
            }
        }

        // — Centre: island heart —
        BlockPos heartPos = new BlockPos(cx, cy + 1, cz);
        if (box.isInside(heartPos))
            level.setBlock(heartPos, ModBlocks.ISLAND_HEART_BLOCK.get().defaultBlockState(), 2);

        // Island tower shaft
        var towerLayer = bigTowerLayer();

        for(int y = 1; y <= MAIN_TOWER_HEIGHT + 1; y++)
        {
            for(Pixel stonebrick: towerLayer.get(0))
            {
                BlockPos pos = new BlockPos(cx + stonebrick.x(), cy + y, cz + stonebrick.z());

                if(box.isInside(pos))
                    level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }

            for(Pixel stonebrick: towerLayer.get(1))
            {
                BlockPos pos = new BlockPos(cx + stonebrick.x(), cy + y, cz + stonebrick.z());

                if(box.isInside(pos))
                    level.setBlock(pos, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
            }
        }

        // Island tower cap: load .nbt directly from the mod classpath,
        // bypassing StructureTemplateManager which doesn't see mod datapacks at gen time.
        try (InputStream is = FlyingDungeonPiece.class.getResourceAsStream(
                "/data/skygrad/structures/castle_main_tower_top.nbt")) {
            if (is == null) {
                System.out.println("[Skygrad] castle_main_tower_top.nbt not found on classpath");
            } else {
                CompoundTag nbt = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                StructureTemplate template = new StructureTemplate();
                template.load(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), nbt);
                Vec3i size = template.getSize();
                BlockPos origin = new BlockPos(
                    cx - size.getX() / 2,
                    cy + MAIN_TOWER_HEIGHT + 2,
                    cz - size.getZ() / 2
                );
                template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), random, 2);
                System.out.println("[Skygrad] Placed tower cap at " + origin + " size=" + size);
            }
        } catch (IOException e) {
            System.out.println("[Skygrad] Failed to load castle_main_tower_top.nbt: " + e);
        }
    }
}
