package fr.tchkll.skygrad.items;

import fr.tchkll.skygrad.Skygrad;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

/**
 * Right-click to consume this item and receive a vanilla {@code filled_map}
 * with a marker pointing at the nearest structure tagged
 * {@code skygrad:on_fortress_maps}.  Mirrors how cartographer treasure-map
 * trades work — same call into {@link ServerLevel#findNearestMapStructure}.
 */
public class FlyingFortressMapItem extends Item {

    /** Datapack-driven structure tag — controls which structures these maps point at. */
    public static final TagKey<Structure> FORTRESS_MAP_TARGETS = TagKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Skygrad.MODID, "on_fortress_maps"));

    /** Search radius in chunks around the player. */
    private static final int SEARCH_RADIUS = 100;

    /** Map zoom level (0 = closest, 4 = farthest). 2 mirrors vanilla treasure maps. */
    private static final byte MAP_SCALE = 2;

    public FlyingFortressMapItem(Properties properties) {
        super(properties);
    }

    @Override @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack inHand = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(inHand);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos found = serverLevel.findNearestMapStructure(
                FORTRESS_MAP_TARGETS, player.blockPosition(), SEARCH_RADIUS, true);

        if (found == null) {
            player.displayClientMessage(
                    Component.translatable("item.skygrad.flying_fortress_map.not_found"),
                    true);
            return InteractionResultHolder.fail(inHand);
        }

        // Vanilla treasure-map equivalent: build a scale-2 explorer map and stamp on a marker.
        ItemStack explorerMap = MapItem.create(serverLevel,
                found.getX(), found.getZ(), MAP_SCALE, true, true);
        MapItem.renderBiomePreviewMap(serverLevel, explorerMap);
        MapItemSavedData.addTargetDecoration(explorerMap, found, "+", MapDecorationTypes.RED_X);
        explorerMap.set(DataComponents.CUSTOM_NAME,
                Component.translatable("filled_map.skygrad.flying_fortress"));

        if (!player.getAbilities().instabuild) {
            inHand.shrink(1);
        }
        if (!player.getInventory().add(explorerMap)) {
            player.drop(explorerMap, false);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                player.getSoundSource(), 1.0F, 1.0F);

        return InteractionResultHolder.consume(inHand);
    }
}
