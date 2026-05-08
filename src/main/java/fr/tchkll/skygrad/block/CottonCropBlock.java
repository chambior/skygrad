package fr.tchkll.skygrad.block;

import fr.tchkll.skygrad.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/**
 * Standard wheat-style crop. {@link CropBlock} already provides:
 * <ul>
 *   <li>Growth via random ticks (slow on dirt, faster on hydrated farmland).</li>
 *   <li>Bone-meal acceleration through {@code BonemealableBlock}.</li>
 *   <li>Seed drops when broken before {@code AGE = 7}, full crop drop when mature
 *       — wired up by our loot table at {@code data/skygrad/loot_table/blocks/cotton.json}.</li>
 *   <li>Detection by Create's mechanical harvester and any other mod that
 *       checks {@code instanceof CropBlock}.</li>
 * </ul>
 *
 * <p>The only override needed is which item is the "seed" for replanting/dispenser logic.</p>
 */
public class CottonCropBlock extends CropBlock {

    public CottonCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.COTTON_SEEDS.get();
    }
}
