package fr.tchkll.skygrad;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-wide game-loop hooks.  No block-entity needed — these run for every
 * player on every tick, regardless of where they are or what's loaded.
 *
 * <p><b>Low-altitude wither aura:</b> any player below {@code Y_THRESHOLD}
 * who isn't currently regenerating receives Wither I.  Re-applied once per
 * second so the effect always overlaps itself; no flicker.</p>
 */
@EventBusSubscriber(modid = Skygrad.MODID)
@SuppressWarnings("unused")
public final class ServerEvents {

    private ServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        WitherFogController.tickPlayer(event.getEntity());
    }

    // ── elytra flight gate ────────────────────────────────────────────────────

    /** NBT key on a vanilla elytra that marks it as mod-crafted and flight-capable. */
    private static final String MECHANICAL_TAG = "skygrad_mechanical";
    /** {@link net.minecraft.world.entity.Entity}'s shared-flag index for fall-flying. */
    private static final int FALL_FLYING_FLAG  = 7;

    /**
     * Disables fall-flying on every plain {@code minecraft:elytra}.
     * An elytra crafted via the {@code mechanical_elytra} recipe carries a
     * {@code custom_data} flag named {@value #MECHANICAL_TAG} and is allowed
     * to glide; everything else is force-stopped each tick.
     *
     * <p>Uses the {@code Entity#setSharedFlag} access transformer to clear the
     * fall-flying bit directly — no event for "don't start fall flying" exists
     * in 1.21.1, so we react every tick instead.</p>
     */
    @SubscribeEvent
    public static void onPlayerTickPreElytra(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!player.isFallFlying()) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(Items.ELYTRA)) return;            // some other flight item — leave alone

        if (isMechanical(chest)) return;                // mod-crafted elytra — allow

        player.setSharedFlag(FALL_FLYING_FLAG, false);  // plain vanilla elytra — kill the glide
    }

    /** True iff the stack carries the {@code skygrad_mechanical} custom-data flag. */
    private static boolean isMechanical(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.getUnsafe().getBoolean(MECHANICAL_TAG);
    }
}
