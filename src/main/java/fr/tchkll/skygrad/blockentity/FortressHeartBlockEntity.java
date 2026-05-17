package fr.tchkll.skygrad.blockentity;

import fr.tchkll.skygrad.Config;
import fr.tchkll.skygrad.registration.ModBlockEntities;
import fr.tchkll.skygrad.registration.ModBlocks;
import fr.tchkll.skygrad.structure.FlyingFortressPiece;
import fr.tchkll.skygrad.utils.algo.Pixel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FortressHeartBlockEntity extends BlockEntity {

    /** Inner zone — players inside this radius receive {@code INNER_WITHER_AMPLIFIER}. */
    private static final int INNER_WITHER_RADIUS    = 30;
    /** Outer zone — players inside this radius (but outside the inner one) receive {@code OUTER_WITHER_AMPLIFIER}. */
    private static final int TNT_RADIUS    = 200;
    /** Outer zone — players inside this radius (but outside the inner one) receive {@code OUTER_WITHER_AMPLIFIER}. */
    private static final int OUTER_WITHER_RADIUS    = 70;
    /** 0-indexed amplifier: 5 → Wither VI. */
    private static final int INNER_WITHER_AMPLIFIER = 5;
    /** 0-indexed amplifier: 3 → Wither IV. */
    private static final int OUTER_WITHER_AMPLIFIER = 3;
    private static final int TICK_INTERVAL          = 20;

    public FortressHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORTRESS_HEART_BE.get(), pos, state);
    }

    @SuppressWarnings("unused")
    public static void serverTick(Level level, BlockPos pos,
                                  BlockState state, FortressHeartBlockEntity be) {
        if (level.getGameTime() % TICK_INTERVAL != 0) return;
        
        if (anyTntAlive(level, pos)) {
            applyWitherToNearbyPlayers(level, pos);
            fireTowardsNearbyPlayer(level, pos);
        }
    }

    private static ArmorStand createFakeOwner(ServerLevel level, BlockPos pos)
    {
        ArmorStand armorStand = new ArmorStand(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );

        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);

        level.addFreshEntity(armorStand);

        return armorStand;
    }

    private static boolean anyTntAlive(Level level, BlockPos heartPos) {
        // Heart is at cy+1. Sentinel is at cy + TOWER_HEIGHT + 2, so offset from heart = TOWER_HEIGHT + 1.
        long seed = (long) heartPos.getX() * 341873128712L + (long) heartPos.getZ() * 132897987541L;
        List<Pixel> towers = FlyingFortressPiece.generateTowers(Config.FORTRESS_SIZE.get(), RandomSource.create(seed));

        int sentinelY = heartPos.getY() + Config.FORTRESS_TOWER_HEIGHT.get() + 1;
        for (Pixel t : towers) {
            BlockPos sentinelPos = new BlockPos(heartPos.getX() + t.x(), sentinelY - 1, heartPos.getZ() + t.z());
            if (level.getBlockState(sentinelPos).is(ModBlocks.TOWER_SENTINEL_BLOCK.get())) return true;
        }
        return false;
    }

    public static void fireTnt(Level level, BlockPos pos, Player target)
    {
        if (level.isClientSide()) return;

        int modif = 0;
        double playerY = target.getY();
        if(playerY < pos.getY() + 40 - 25) modif = -85;

        BlockPos posCopy = new BlockPos(pos.getX(), pos.getY() + 40 + modif, pos.getZ());

        Vec3 startPos = Vec3.atCenterOf(posCopy);
        Vec3 targetPos = target.getEyePosition();

        double distance = targetPos.distanceTo(startPos);

        int fuse = 60;

        double speed = distance / (
                50.0D * (1.0D - Math.pow(0.98D, fuse))
        );

        Vec3 velocity = targetPos
                .subtract(startPos)
                .normalize()
                .scale(speed);

        PrimedTnt tnt = new PrimedTnt(
                level,
                startPos.x,
                startPos.y,
                startPos.z,
                null
        );

        tnt.setDeltaMovement(velocity);
        tnt.setFuse(fuse);
        tnt.setNoGravity(true);

        level.addFreshEntity(tnt);
    }

    private static void fireTowardsNearbyPlayer(Level level, BlockPos pos) {
        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                new net.minecraft.world.phys.AABB(pos).inflate(TNT_RADIUS)
        );

        for (Player player : players) {
            if (player.isSpectator()) continue;

            if(Math.random() < 0.9) continue;

            fireTnt(level, pos, player);
        }
    }

    private static void applyWitherToNearbyPlayers(Level level, BlockPos pos) {
        // Coarse AABB pre-filter — pulls every player within an axis-aligned cube of
        // half-side OUTER_WITHER_RADIUS around the heart.  Up to ~87 blocks at corners,
        // tightened below by spherical Vec3 distance.
        List<Player> players = level.getEntitiesOfClass(
            Player.class,
            new net.minecraft.world.phys.AABB(pos).inflate(OUTER_WITHER_RADIUS)
        );

        Vec3 heartCenter = Vec3.atCenterOf(pos);
        double innerSq = (double) INNER_WITHER_RADIUS * INNER_WITHER_RADIUS;
        double outerSq = (double) OUTER_WITHER_RADIUS * OUTER_WITHER_RADIUS;

        for (Player player : players) {
            if (player.isCreative() || player.isSpectator()) continue;

            double distSq = player.distanceToSqr(heartCenter);
            if (distSq <= innerSq) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.HARM,
                    1,
                    INNER_WITHER_AMPLIFIER,
                    false,
                    true
                ));
            } else if (distSq <= outerSq) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    TICK_INTERVAL + 5,
                    OUTER_WITHER_AMPLIFIER,
                    false,
                    true
                ));
            }
        }
    }
}