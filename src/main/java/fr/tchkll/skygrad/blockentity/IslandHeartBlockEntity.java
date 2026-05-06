package fr.tchkll.skygrad.blockentity;

import fr.tchkll.skygrad.Config;
import fr.tchkll.skygrad.ModBlockEntities;
import fr.tchkll.skygrad.ModBlocks;
import fr.tchkll.skygrad.structure.FlyingDungeonPiece;
import fr.tchkll.skygrad.utils.algo.Pixel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class IslandHeartBlockEntity extends BlockEntity {

    private static final int WITHER_RADIUS = 50;
    private static final int TICK_INTERVAL = 20;

    public IslandHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ISLAND_HEART_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos,
                                  BlockState state, IslandHeartBlockEntity be) {
        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        if (anyTntAlive(level, pos)) {
            applyWitherToNearbyPlayers(level, pos);
        }
    }

    private static boolean anyTntAlive(Level level, BlockPos heartPos) {
        // Heart is at cy+1. Sentinel is at cy + TOWER_HEIGHT + 2, so offset from heart = TOWER_HEIGHT + 1.
        long seed = (long) heartPos.getX() * 341873128712L + (long) heartPos.getZ() * 132897987541L;
        List<Pixel> towers = FlyingDungeonPiece.generateTowers(Config.CASTLE_SIZE.get(), RandomSource.create(seed));

        int sentinelY = heartPos.getY() + Config.CASTLE_TOWER_HEIGHT.get() + 1;
        for (Pixel t : towers) {
            BlockPos sentinelPos = new BlockPos(heartPos.getX() + t.x(), sentinelY, heartPos.getZ() + t.z());
            if (level.getBlockState(sentinelPos).is(ModBlocks.TOWER_SENTINEL_BLOCK.get())) return true;
        }
        return false;
    }

    private static void applyWitherToNearbyPlayers(Level level, BlockPos pos) {
        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                new net.minecraft.world.phys.AABB(pos).inflate(WITHER_RADIUS)
        );

        for (Player player : players) {
            if (player.isCreative() || player.isSpectator()) continue;

            player.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    TICK_INTERVAL + 5, // durée légèrement supérieure à l'intervalle
                    3,                 // amplifier 3 = niveau 4
                    false,             // pas d'ambiance
                    true               // particules visibles
            ));
        }
    }
}