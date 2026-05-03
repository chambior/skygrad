package fr.tchkll.skygrad.block;

import fr.tchkll.skygrad.Skygrad;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Skygrad.MODID)
public class BuildersBlockTickHandler {

    private static final int  RADIUS       = 20;
    private static final int  TICK_INTERVAL = 20; // vérifier 1x/seconde

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        for (ServerPlayer player : level.players()) {
            boolean nearBlock = isNearBuildersBlock(level, player.blockPosition());
            applyFlight(player, nearBlock);
        }
    }

    private static boolean isNearBuildersBlock(ServerLevel level, BlockPos playerPos) {
        // Scan AABB cubique autour du joueur
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    // Filtre sphérique (distance euclidienne)
                    if (dx*dx + dy*dy + dz*dz > RADIUS * RADIUS) continue;

                    mutable.set(
                            playerPos.getX() + dx,
                            playerPos.getY() + dy,
                            playerPos.getZ() + dz
                    );

                    BlockState state = level.getBlockState(mutable);
                    if (state.is(ModBlocks.BUILDERS_BLOCK.get())) return true;
                }
            }
        }
        return false;
    }

    private static void applyFlight(ServerPlayer player, boolean canFly) {
        if (player.isCreative() || player.isSpectator()) return;

        boolean changed = false;

        if (canFly && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            changed = true;
        } else if (!canFly && player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            changed = true;
        }

        if (changed) {
            player.onUpdateAbilities();
        }
    }
}