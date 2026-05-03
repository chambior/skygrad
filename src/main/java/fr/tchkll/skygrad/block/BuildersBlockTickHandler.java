package fr.tchkll.skygrad.block;

import fr.tchkll.skygrad.ModBlocks;
import fr.tchkll.skygrad.Skygrad;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;

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

    private static final ResourceLocation FLIGHT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("skygrad", "builders_block_flight");

    private static void applyFlight(ServerPlayer player, boolean canFly) {
        if (player.isCreative() || player.isSpectator()) return;

        var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute == null) return;

        boolean hasModifier = attribute.getModifier(FLIGHT_MODIFIER_ID) != null;

        if (canFly && !hasModifier) {
            // Ajoute le modificateur → valeur 1.0 > 0, le vol est accordé
            attribute.addTransientModifier(new AttributeModifier(
                    FLIGHT_MODIFIER_ID,
                    1.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
            player.onUpdateAbilities();

        } else if (!canFly && hasModifier) {
            attribute.removeModifier(FLIGHT_MODIFIER_ID);
            player.getAbilities().flying = false; // stoppe le vol en cours
            player.onUpdateAbilities();
        }
    }
}