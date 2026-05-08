package fr.tchkll.skygrad.block;

import fr.tchkll.skygrad.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.jetbrains.annotations.NotNull;

public class CottonCropBlock extends CropBlock {

    public CottonCropBlock(Properties properties) {
        super(properties);
    }

    @Override @NotNull
    protected ItemLike getBaseSeedId() {
        return ModItems.COTTON_SEEDS.get();
    }
}
