package fr.tchkll.skygrad.blockentity;

import fr.tchkll.skygrad.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class IslandHeartBlockEntity extends BlockEntity {

    private static final int  TOWER_RADIUS  = 10;  // offset horizontal des tours
    private static final int  TNT_HEIGHT    = 9;   // y relatif du TNT par rapport au cœur
    private static final int  WITHER_RADIUS = 50;
    private static final int  TICK_INTERVAL = 20;

    // Les 4 offsets (dx, dz) des tours par rapport au cœur
    private static final int[][] TOWER_OFFSETS = {
            { TOWER_RADIUS,  0},
            {-TOWER_RADIUS,  0},
            { 0,  TOWER_RADIUS},
            { 0, -TOWER_RADIUS}
    };

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
        for (int[] offset : TOWER_OFFSETS) {
            BlockPos tntPos = heartPos.offset(offset[0], TNT_HEIGHT, offset[1]);
            if (level.getBlockState(tntPos).is(Blocks.TNT)) return true;
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