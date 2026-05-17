package fr.tchkll.skygrad.registration;

import fr.tchkll.skygrad.Skygrad;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Constants for mod-defined loot tables.  Loot tables themselves live as JSON
 * under {@code data/skygrad/loot_table/...}; this class only holds the typed
 * keys used to reference them from code (e.g. when seeding a chest BE).
 */
public final class ModLootTables {

    private ModLootTables() {}

    /** Loot table for the chest sitting on top of the island heart. */
    public static final ResourceKey<LootTable> FLYING_CASTLE_CHEST = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Skygrad.MODID, "chests/flying_dungeon"));

    /** Loot table for the chest sitting on top of the island heart. */
    public static final ResourceKey<LootTable> FLYING_FORTRESS_CHEST = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Skygrad.MODID, "chests/flying_fortress"));
}
