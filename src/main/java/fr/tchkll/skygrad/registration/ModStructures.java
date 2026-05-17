package fr.tchkll.skygrad.registration;

import fr.tchkll.skygrad.Skygrad;
import fr.tchkll.skygrad.structure.FlyingDungeonStructure;
import fr.tchkll.skygrad.structure.FlyingFortressStructure;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_TYPE, Skygrad.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<FlyingDungeonStructure>>
            FLYING_DUNGEON_TYPE = STRUCTURE_TYPES.register("flying_dungeon",
            () -> () -> FlyingDungeonStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<FlyingFortressStructure>>
            FLYING_FORTRESS_TYPE = STRUCTURE_TYPES.register("flying_fortress",
            () -> () -> FlyingFortressStructure.CODEC);

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
    }
}