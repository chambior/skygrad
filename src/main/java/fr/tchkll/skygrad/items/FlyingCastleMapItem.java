package fr.tchkll.skygrad.items;

import fr.tchkll.skygrad.Skygrad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

/**
 * Right-click to consume this item and receive a vanilla {@code filled_map}
 * with a marker pointing at the nearest structure tagged
 * {@code skygrad:on_castle_maps}.  Mirrors how cartographer treasure-map
 * trades work — same call into {@link ServerLevel#findNearestMapStructure}.
 */
public class FlyingCastleMapItem extends Item {

    /** Datapack-driven structure tag — controls which structures these maps point at. */
    public static final TagKey<Structure> CASTLE_MAP_TARGETS = TagKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Skygrad.MODID, "on_castle_maps"));

    /** Structure set used for placement-formula fallback (when the chunk doesn't already record the structure). */
    private static final ResourceLocation FALLBACK_STRUCTURE_SET =
            ResourceLocation.fromNamespaceAndPath(Skygrad.MODID, "flying_dungeon");

    /** Search radius in chunks around the player. */
    private static final int SEARCH_RADIUS = 100;

    /** Map zoom level (0 = closest, 4 = farthest). 2 mirrors vanilla treasure maps. */
    private static final byte MAP_SCALE = 2;

    public FlyingCastleMapItem(Properties properties) {
        super(properties);
    }

    @Override @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack inHand = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(inHand);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos playerPos = player.blockPosition();

        Skygrad.LOGGER.info("[CastleMap] use() player={} pos={} dim={}",
                player.getName().getString(), playerPos, serverLevel.dimension().location());

        BlockPos found = serverLevel.findNearestMapStructure(
                CASTLE_MAP_TARGETS, playerPos, SEARCH_RADIUS, false);
        Skygrad.LOGGER.info("[CastleMap] findNearestMapStructure -> {}", found);

        if (found == null) {
            found = findNearestPlacementCandidate(serverLevel, playerPos, FALLBACK_STRUCTURE_SET, SEARCH_RADIUS);
            Skygrad.LOGGER.info("[CastleMap] placement-fallback -> {}", found);
        }

        if (found == null) {
            player.displayClientMessage(
                    Component.translatable("item.skygrad.flying_castle_map.not_found"),
                    true);
            return InteractionResultHolder.fail(inHand);
        }

        ItemStack explorerMap = MapItem.create(serverLevel,
                found.getX(), found.getZ(), MAP_SCALE, true, true);
        MapItem.renderBiomePreviewMap(serverLevel, explorerMap);
        MapItemSavedData.addTargetDecoration(explorerMap, found, "+", MapDecorationTypes.RED_X);
        explorerMap.set(DataComponents.CUSTOM_NAME,
                Component.translatable("filled_map.skygrad.flying_castle"));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                player.getSoundSource(), 1.0F, 1.0F);

        // ItemUtils.createFilledResult handles the consume-and-replace cleanly:
        // when the held stack hits 0 after the shrink it returns `explorerMap` as the
        // new in-hand stack instead of having vanilla clobber the hand slot with EMPTY.
        return InteractionResultHolder.sidedSuccess(
                ItemUtils.createFilledResult(inHand, player, explorerMap, false),
                level.isClientSide());
    }

    /**
     * Fallback when {@link ServerLevel#findNearestMapStructure} returns null — typical when chunks
     * were generated before this structure existed, so their reference map says "not present".
     * We compute potential structure chunks directly from the structure set's placement formula
     * and return the nearest one to the player. The candidate may sit in an unloaded chunk that
     * will generate the structure when the player arrives, or in an already-generated chunk that
     * predates the structure (in which case nothing is there — there is no way to retro-add it).
     */
    private static BlockPos findNearestPlacementCandidate(
            ServerLevel serverLevel, BlockPos origin, ResourceLocation structureSetId, int radius) {
        StructureSet set = serverLevel.registryAccess()
                .registryOrThrow(Registries.STRUCTURE_SET)
                .get(structureSetId);
        if (set == null) {
            Skygrad.LOGGER.warn("[CastleMap] structure set {} not found in registry", structureSetId);
            return null;
        }
        if (!(set.placement() instanceof RandomSpreadStructurePlacement rsp)) {
            Skygrad.LOGGER.warn("[CastleMap] structure set {} placement is not RandomSpread ({})",
                    structureSetId, set.placement().getClass().getSimpleName());
            return null;
        }

        long seed = serverLevel.getSeed();
        int spacing = rsp.spacing();
        int originChunkX = SectionPos.blockToSectionCoord(origin.getX());
        int originChunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        int originCellX = Math.floorDiv(originChunkX, spacing);
        int originCellZ = Math.floorDiv(originChunkZ, spacing);

        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos candidate = rsp.getPotentialStructureChunk(seed, originCellX + dx, originCellZ + dz);
                int bx = candidate.getMiddleBlockX();
                int bz = candidate.getMiddleBlockZ();
                double ddx = bx - origin.getX();
                double ddz = bz - origin.getZ();
                double distSq = ddx * ddx + ddz * ddz;
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = new BlockPos(bx, origin.getY(), bz);
                }
            }
        }
        return best;
    }
}
