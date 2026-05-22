package fr.tchkll.skygrad;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {

    @Override @NotNull
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(
                "mymod",
                "jei_plugin"
        );
    }

    @Override @ParametersAreNonnullByDefault
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

        var itemsToRemove = new ArrayList<Item>();

        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_ingot")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_block")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_nugget")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:heavy_plate")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_sword")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_pickaxe")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_axe")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_hoe")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:steel_shovel")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:molten_steel")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:molten_steel_bucket")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfmg:copper_wire")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:steel_ingot")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:steel_block")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:steel_scrap")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:heavy_plate")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:cast_iron_nugget")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:cast_iron_ingot")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createbigcannons:cast_iron_block")));
        itemsToRemove.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse("farmersdelight:wheat_dough")));

        for(var item : itemsToRemove)
        {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(
                    VanillaTypes.ITEM_STACK,
                    List.of(new ItemStack(item))
            );
        }
    }
}