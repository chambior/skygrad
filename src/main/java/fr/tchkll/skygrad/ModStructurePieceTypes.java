package fr.tchkll.skygrad;

import fr.tchkll.skygrad.structure.FlyingDungeonPiece;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructurePieceTypes {

    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PIECE, Skygrad.MODID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType>
            FLYING_DUNGEON_PIECE = PIECE_TYPES.register("flying_dungeon_piece",
            () -> FlyingDungeonPiece::new);   // ← pointe vers le constructeur de désérialisation

    public static void register(IEventBus bus) {
        PIECE_TYPES.register(bus);
    }
}