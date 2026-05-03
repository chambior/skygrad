package fr.tchkll.skygrad.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BuildersBlock extends Block {

    public BuildersBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIAMOND)
                .strength(0.5f, 6f)
                .sound(SoundType.METAL)
        );
    }
}