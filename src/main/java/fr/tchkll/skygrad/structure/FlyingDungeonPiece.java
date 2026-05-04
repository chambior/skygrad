package fr.tchkll.skygrad.structure;

import fr.tchkll.skygrad.ModBlocks;
import fr.tchkll.skygrad.ModStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;

public class FlyingDungeonPiece extends StructurePiece {

    private static final int SIZE        = 40;
    private static final int WALL_HEIGHT = 4;
    private static final int TOWER_HEIGHT = 8;
    private static final int BBOX_RADIUS = SIZE / 2 + 4;

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
                c.getX() + r, c.getY() + TOWER_HEIGHT + 2, c.getZ() + r);
    }

    // Deterministic random seeded from center so every chunk call produces the same towers.
    private int[][] getTowers() {
        long seed = (long) center.getX() * 341873128712L + (long) center.getZ() * 132897987541L;
        return generateTowers(SIZE, RandomSource.create(seed));
    }

    // Mirrors generate_towers(size) from the Python POC.
    private static int[][] generateTowers(int size, RandomSource rng) {
        int numTowers = 3 + rng.nextInt(5); // 3..7
        int[][] towers = new int[numTowers][2];
        for (int t = 0; t < numTowers; t++) {
            int minAngle = (int) (360.0 * (t + 0.25) / numTowers);
            int maxAngle = (int) (360.0 * (t + 0.75) / numTowers);
            int minR = size / 4;
            int maxR = size / 3;
            int r = minR + rng.nextInt(maxR - minR + 1);
            int omega = minAngle + rng.nextInt(maxAngle - minAngle + 1);
            towers[t][0] = (int) (r * Math.cos(Math.toRadians(omega)));
            towers[t][1] = (int) (r * Math.sin(Math.toRadians(omega)));
        }
        return towers;
    }

    // Mirrors fill_polygon(points) from the Python POC (scanline fill).
    // pts[][0] = dx, pts[][1] = dz
    private static List<int[]> fillPolygon(int[][] pts) {
        List<int[]> pixels = new ArrayList<>();
        if (pts.length < 3) return pixels;

        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (int[] p : pts) {
            if (p[1] < minZ) minZ = p[1];
            if (p[1] > maxZ) maxZ = p[1];
        }

        for (int z = minZ; z <= maxZ; z++) {
            List<Double> xs = new ArrayList<>();
            for (int i = 0; i < pts.length; i++) {
                int x1 = pts[i][0],                       z1 = pts[i][1];
                int x2 = pts[(i + 1) % pts.length][0],    z2 = pts[(i + 1) % pts.length][1];
                if (z1 == z2) continue;
                if (z < Math.min(z1, z2) || z >= Math.max(z1, z2)) continue;
                xs.add(x1 + (double) (z - z1) * (x2 - x1) / (z2 - z1));
            }
            xs.sort(Double::compareTo);
            for (int i = 0; i + 1 < xs.size(); i += 2) {
                int xStart = (int) Math.ceil(xs.get(i));
                int xEnd   = (int) Math.floor(xs.get(i + 1));
                for (int x = xStart; x <= xEnd; x++)
                    pixels.add(new int[]{x, z});
            }
        }
        return pixels;
    }

    // Mirrors line(x1,y1,x2,y2) from the Python POC.
    private static List<int[]> drawLine(int x1, int z1, int x2, int z2) {
        List<int[]> pts = new ArrayList<>();
        int dx = x2 - x1, dz = z2 - z1;
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) { pts.add(new int[]{x1, z1}); return pts; }
        double xi = (double) dx / steps, zi = (double) dz / steps;
        double x = x1, z = z1;
        for (int s = 0; s <= steps; s++) {
            pts.add(new int[]{(int) Math.round(x), (int) Math.round(z)});
            x += xi; z += zi;
        }
        return pts;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        int[][] towers = getTowers();

        // — Floor: polygon interior with stone bricks —
        for (int[] p : fillPolygon(towers)) {
            BlockPos pos = new BlockPos(cx + p[0], cy, cz + p[1]);
            if (box.isInside(pos))
                level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }

        // — Walls between towers —
        int[] prev = towers[towers.length - 1];
        for (int[] t : towers) {
            double wallAngle = Math.toDegrees(Math.atan2(t[1] - prev[1], t[0] - prev[0]));

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

            for (int[] wp : drawLine(prev[0], prev[1], t[0], t[1])) {
                for (int dy = 0; dy <= WALL_HEIGHT; dy++) {
                    BlockPos wallPos = new BlockPos(cx + wp[0], cy + dy, cz + wp[1]);
                    if (box.isInside(wallPos))
                        level.setBlock(wallPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);

                    BlockPos innerPos = new BlockPos(cx + wp[0] + offX, cy + dy, cz + wp[1] + offZ);
                    if (box.isInside(innerPos))
                        level.setBlock(innerPos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
            prev = t;
        }

        // — Tower columns: obsidian + TNT cap —
        for (int[] t : towers) {
            for (int dy = 0; dy <= TOWER_HEIGHT; dy++) {
                BlockPos pos = new BlockPos(cx + t[0], cy + dy, cz + t[1]);
                if (box.isInside(pos))
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
            }
            BlockPos tntPos = new BlockPos(cx + t[0], cy + TOWER_HEIGHT + 1, cz + t[1]);
            if (box.isInside(tntPos))
                level.setBlock(tntPos, Blocks.TNT.defaultBlockState(), 2);
        }

        // — Centre: island heart —
        BlockPos heartPos = new BlockPos(cx, cy, cz);
        if (box.isInside(heartPos))
            level.setBlock(heartPos, ModBlocks.ISLAND_HEART_BLOCK.get().defaultBlockState(), 2);
    }
}
